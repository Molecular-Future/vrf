package org.mos.vrfblk.action

import org.apache.felix.ipojo.annotations.Instantiate
import org.apache.felix.ipojo.annotations.Provides
import onight.tfw.ntrans.api.ActorService
import onight.tfw.proxy.IActor
import onight.tfw.otransio.api.session.CMDService
import onight.osgi.annotation.NActorProvider
import org.mos.p22p.utils.LogHelper
import onight.oapi.scala.commons.PBUtils
import onight.oapi.scala.commons.LService
import org.mos.p22p.action.PMNodeHelper
import onight.tfw.otransio.api.beans.FramePacket
import onight.tfw.async.CompleteHandler
import org.mos.p22p.utils.PacketIMHelper._

import onight.tfw.otransio.api.PacketHelper
import org.mos.p22p.exception.FBSException
import org.mos.vrfblk.Daos
import com.google.protobuf.ByteString
import scala.collection.JavaConversions._
import org.mos.vrfblk.PSMVRFNet
import org.mos.bcrand.model.Bcrand.PSSyncBlocks
import org.mos.bcrand.model.Bcrand.PRetSyncBlocks
import org.mos.vrfblk.tasks.VCtrl
import org.mos.bcrand.model.Bcrand.PCommand
import org.mos.vrfblk.utils.VConfig

@NActorProvider
@Instantiate
@Provides(specifications = Array(classOf[ActorService], classOf[IActor], classOf[CMDService]))
class PSBlockSync extends PSMVRFNet[PSSyncBlocks] {
  override def service = PSBlockSyncService
}

/**
 * 请求同步块
 */
object PSBlockSyncService extends LogHelper with PBUtils with LService[PSSyncBlocks] with PMNodeHelper {
  override def onPBPacket(pack: FramePacket, pbo: PSSyncBlocks, handler: CompleteHandler) = {
    val ret = PRetSyncBlocks.newBuilder();
    if (!VCtrl.isReady()) {
      ret.setRetCode(-1).setRetMessage("VRF Network Not READY")
      handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()))
    } else if (VConfig.AUTH_NODE_FILTER && !VCtrl.haveEnoughToken(pbo.getSignature)) {
      // TODO 判断是否有足够余额，只发给有足够余额的节点
      log.error("unauthorization to get block" + pbo.getSignature);
      ret.setRetCode(-1).setRetMessage("Unauthorization")
      handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()))
    } else if (Runtime.getRuntime.freeMemory() / 1024 / 1024 < VConfig.METRIC_SYNCTX_FREE_MEMEORY_MB) {
      ret.setRetCode(-2).setRetMessage("memory low")
      log.debug("ban sync block for low memory");
      handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()))
    } else {
      try {

        MDCSetBCUID(VCtrl.network())
        MDCSetMessageID(pbo.getMessageId)
        ret.setMessageId(pbo.getMessageId);
        ret.setRetCode(0).setRetMessage("SUCCESS")
        //同步块总大小(K)
        var totalSize = 0
        //同步block总共40M
        val maxTotalSize = 100 * 1024 * 1024

        for (
          id <- pbo.getStartId to pbo.getEndId if totalSize <= maxTotalSize
        ) {
          val b = VCtrl.loadFromBlock(id, pbo.getNeedBody)
          if (b != null) {
            b.map(bs => {
              if (bs != null && totalSize <= maxTotalSize) {
                totalSize = totalSize + bs.build().toByteArray().size;
                if (totalSize <= maxTotalSize) {
                  ret.addBlockHeaders(bs);
                } else {
                  log.info("package too large. size=" + totalSize + " blk=" + id)
                }
              }
            })
          }
        }
        pbo.getBlockIdxList.map { id =>
          val b = VCtrl.loadFromBlock(id, pbo.getNeedBody);
          if (b != null) {
            b.map(bs => {
              totalSize = totalSize + bs.build().toByteArray().size;
            })
            if (totalSize <= maxTotalSize) {
              b.map(bs => {
                if (bs != null) {
                  totalSize = totalSize + bs.build().toByteArray().size;
                  log.info("blk=" + id + " size=" + bs.build().toByteArray().size)
                  if (totalSize <= maxTotalSize) {
                    ret.addBlockHeaders(bs);
                  } else {
                    log.info("package too large. size=" + totalSize + " blk=" + id);
                  }
                }
              })
            } else {
              log.info("package too large. size=" + totalSize + " blk=" + id);
            }
          }
        }
      } catch {
        case e: FBSException => {
          ret.clear()
          ret.setRetCode(-2).setRetMessage(e.getMessage)
        }
        case t: Throwable => {
          log.error("error:", t);
          ret.clear()
          ret.setRetCode(-3)
        }
      } finally {
        handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()))
      }
    }
  }
  //  override def getCmds(): Array[String] = Array(PWCommand.LST.name())
  override def cmd: String = PCommand.SYN.name();
}
