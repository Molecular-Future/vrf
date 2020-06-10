
package org.mos.vrfblk.action

import org.apache.felix.ipojo.annotations.Instantiate
import org.apache.felix.ipojo.annotations.Provides
import org.mos.bcrand.model.Bcrand.PCommand
import org.mos.bcrand.model.Bcrand.PSCoinbase
import org.mos.mcore.model.Block.BlockInfo;
import org.mos.p22p.action.PMNodeHelper
import org.mos.p22p.utils.LogHelper
import org.mos.vrfblk.PSMVRFNet
import org.mos.vrfblk.tasks.BlockProcessor
import org.mos.vrfblk.tasks.VCtrl

import org.mos.vrfblk.utils.RandFunction
import onight.oapi.scala.commons.LService
import onight.oapi.scala.commons.PBUtils
import onight.osgi.annotation.NActorProvider
import onight.tfw.async.CompleteHandler
import onight.tfw.otransio.api.PacketHelper
import onight.tfw.otransio.api.beans.FramePacket
import onight.tfw.ntrans.api.ActorService
import onight.tfw.proxy.IActor
import onight.tfw.otransio.api.session.CMDService
import org.mos.vrfblk.msgproc.ApplyBlock
import org.mos.vrfblk.tasks.NodeStateSwitcher
import org.mos.vrfblk.tasks.Initialize
import org.mos.vrfblk.Daos
import org.mos.vrfblk.tasks.BeaconGossip
import org.apache.commons.lang3.StringUtils
import org.mos.bcrand.model.Bcrand.VNodeState
import org.mos.vrfblk.utils.VConfig
import org.mos.vrfblk.Daos
import com.google.protobuf.ByteString
import org.mos.bcrand.model.Bcrand.VNode
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.LinkedBlockingQueue
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.mos.mcore.model.Block.BlockInfo
import java.util.Arrays.ArrayList
import java.util.ArrayList
import org.mos.p22p.utils.PacketIMHelper._

@NActorProvider
@Instantiate
@Provides(specifications = Array(classOf[ActorService], classOf[IActor], classOf[CMDService]))
class PSCoinbaseNew extends PSMVRFNet[PSCoinbase] {
  override def service = PSCoinbaseNewService
}

//
// http://localhost:8000/fbs/xdn/pbget.do?bd=
object PSCoinbaseNewService extends LogHelper with PBUtils with LService[PSCoinbase] with PMNodeHelper {

  val running = new AtomicBoolean(true);
  val queue = new LinkedBlockingQueue[PSCoinbase]
  val pendingBlockCache: Cache[Int, String] = CacheBuilder.newBuilder().expireAfterWrite(40, TimeUnit.SECONDS)
    .maximumSize(1000).build().asInstanceOf[Cache[Int, String]]

  val lastApplyBlockID = new AtomicInteger(0);

  object ApplyRunner extends Runnable {

    override def run() {
      running.set(true);
      Thread.currentThread().setName("PDCoinbase Runner");
      val waitApplyList = new ArrayList[PSCoinbase];
      while (running.get) {

        try { ////sort by queue.
          var h = queue.poll(5000, TimeUnit.MILLISECONDS);
          if (h != null) {
            if (lastApplyBlockID.get + 1 < h.getBlockHeight) { //不连续
              log.error(s"not connected,lastApplyBlockID=${lastApplyBlockID},newblockheight=${h.getBlockHeight}");
              waitApplyList.add(h)
            } else {
              if (h.getBlockHeight > lastApplyBlockID.get) {
                lastApplyBlockID.set(h.getBlockHeight);
              }
              bgApplyBlock(h);
              waitApplyList.forEach({ pendingh =>
                if (lastApplyBlockID.get + 1 == pendingh.getBlockHeight) {
                  h = null;
                }
              })

            }
          }

          if (h == null && waitApplyList.size > 0 || waitApplyList.size > VConfig.SYNC_SAFE_BLOCK_COUNT / 2 + 1) {
            queue.drainTo(waitApplyList);
            waitApplyList.sort((p1, p2) => p1.getBlockHeight - p2.getBlockHeight)
            waitApplyList.forEach(backh => {
              lastApplyBlockID.set(backh.getBlockHeight);
              bgApplyBlock(backh);
            })
            waitApplyList.clear();
          }

        } catch {
          case t: Throwable =>
            log.error("get error", t);
        } finally {
          try {
            Thread.sleep(10)
          } catch {
            case t: Throwable =>
          }
        }
      }
    }

  }
  new Thread(ApplyRunner).start();

  override def onPBPacket(pack: FramePacket, pbo: PSCoinbase, handler: CompleteHandler) = {
    log.info("Mine Block blk::" + pbo.getBlockHeight + ",from=" + pbo.getBcuid)
    if (!VCtrl.isReady()) {
      log.debug("VCtrl not ready");
      handler.onFinished(PacketHelper.toPBReturn(pack, pbo))
    } else if (queue.size() > VConfig.MAX_COINBASE_QUEUE_SIZE) {
      log.info("drop coinbase for queuesize too large:" + queue.size() + ",height=" + pbo.getBlockHeight + ",from=" + pack.getFrom());
    } else {
      queue.offer(pbo);
      handler.onFinished(PacketHelper.toPBReturn(pack, pbo))
    }
  }
  def bgApplyBlock(pbo: PSCoinbase) = {
    //    log.debug("Mine Block From::" + pack.getFrom())
    val block = BlockInfo.newBuilder().mergeFrom(pbo.getBlockEntry.getBlockHeader);
    if (VConfig.AUTH_NODE_FILTER && !VCtrl.haveEnoughToken(Daos.enc.bytesToHexStr(block.getMiner.getAddress.toByteArray()))) {
      // TODO 判断是否有足够余额，只发给有足够余额的节点
      log.error("unauthorization " + block.getMiner.getAddress + " " + pbo.getBlockEntry.getBlockhash);
    } else {
      MDCSetBCUID(VCtrl.network())
      MDCSetMessageID(pbo.getMessageId)
      //      log.debug("Get New Block:H=" + pbo.getBlockEntry.getBlockHeight + " from=" + pbo.getBcuid + ",BH=" + pbo.getBlockEntry.getBlockhash + ",beacon=" + block.getMiner.getTerm);
      // 校验beaconHash和区块hash是否匹配，排除异常区块
      val parentBlock = Daos.chainHelper.getBlockByHash(block.getHeader.getParentHash.toByteArray());
      if (VCtrl.coMinerByUID.contains(pbo.getBcuid)) {
        val bb = VCtrl.coMinerByUID.getOrElse(pbo.getBcuid, VNode.newBuilder().build()).toBuilder();
        bb.setCurBlock(block.getHeader.getHeight.intValue());
        bb.setCurBlockHash(Daos.enc.bytesToHexStr(block.getHeader.getHash.toByteArray()));
        VCtrl.addCoMiner(bb.build());
      }

      if (parentBlock == null) {
        if (VCtrl.curVN().getState != VNodeState.VN_INIT
          && VCtrl.curVN().getState != VNodeState.VN_SYNC_BLOCK
          && VCtrl.curVN().getCurBlock + VConfig.MAX_SYNC_BLOCKS > pbo.getBlockHeight) {
          BlockProcessor.offerBlock(new ApplyBlock(pbo)); //need to sync or gossip
        } else {
          log.info("Drop newBlock:H=" + pbo.getBlockEntry.getBlockHeight + " from=" + pbo.getBcuid + ",BH=" + pbo.getBlockEntry.getBlockhash + ",beacon=" + block.getMiner.getTerm);
        }
      } else {
        //        val nodebits = parentBlock.getMiner.getBits;
        val (hash, sign) = RandFunction.genRandHash(Daos.enc.bytesToHexStr(block.getHeader.getParentHash.toByteArray()), parentBlock.getMiner.getTerm, parentBlock.getMiner.getBits);
        if (hash.equals(block.getMiner.getTerm) || block.getHeader.getHeight == 1) {
          BlockProcessor.offerMessage(new ApplyBlock(pbo));
        } else {
          //if rollback
          if (StringUtils.isNotBlank(BeaconGossip.rollbackGossipNetBits)) {
            val (rollbackhash, rollblacksign) = RandFunction.genRandHash(Daos.enc.bytesToHexStr(parentBlock.getHeader.getHash.toByteArray()), parentBlock.getMiner.getTerm, BeaconGossip.rollbackGossipNetBits);
            if (rollbackhash.equals(block.getMiner.getTerm)) {
              log.info("rollback hash apply:rollbackhash=" + rollbackhash + ",blockheight=" + pbo.getBlockHeight);
              BlockProcessor.offerMessage(new ApplyBlock(pbo));
            } else {
              log.warn("beaconhash.rollback not equal:height=" + block.getHeader.getHeight + ":: BH=" + pbo.getBlockEntry.getBlockhash
                + " prvbh=" + Daos.enc.bytesToHexStr(block.getHeader.getParentHash.toByteArray()) + " dbprevbh=" + Daos.enc.bytesToHexStr(parentBlock.getHeader.getHash.toByteArray())
                + " termid=" + block.getMiner.getTerm + " ptermid=" + parentBlock.getMiner.getTerm
                + " need=" + rollbackhash + " get=" + pbo.getBeaconHash
                + " prevBeaconHash=" + pbo.getPrevBeaconHash + " BeaconBits=" + parentBlock.getMiner.getBits
                + ",rollbackseed=" + BeaconGossip.rollbackGossipNetBits)
            }
          } else {
            log.warn("beaconhash not equal:: BH=" + pbo.getBlockEntry.getBlockhash + " prvbh=" + Daos.enc.bytesToHexStr(block.getHeader.getParentHash.toByteArray()) + " termid=" + block.getMiner.getTerm + " ptermid=" + parentBlock.getMiner.getTerm + " need=" + hash + " get=" + pbo.getBeaconHash + " prevBeaconHash=" + pbo.getPrevBeaconHash + " BeaconBits=" + parentBlock.getMiner.getBits)
          }
        }
      }
    }
  }

  //  override def getCmds(): Array[String] = Array(PWCommand.LST.name())
  override def cmd: String = PCommand.CBN.name();
}
