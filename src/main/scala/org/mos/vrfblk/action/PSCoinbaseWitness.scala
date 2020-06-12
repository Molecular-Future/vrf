
package org.mos.vrfblk.action

import org.apache.felix.ipojo.annotations.Instantiate
import org.apache.felix.ipojo.annotations.Provides
import org.mos.bcrand.model.Bcrand.PCommand
import org.mos.bcrand.model.Bcrand.PSCoinbase
import org.mos.p22p.action.PMNodeHelper
import org.mos.p22p.utils.LogHelper
import org.mos.vrfblk.PSMVRFNet
import org.mos.vrfblk.tasks.BlockProcessor
import org.mos.vrfblk.tasks.VCtrl

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
import org.mos.vrfblk.msgproc.NotaryBlock
import org.mos.vrfblk.tasks.Initialize
import org.mos.vrfblk.tasks.NodeStateSwitcher
import org.mos.vrfblk.utils.VConfig
import org.fc.zippo.dispatcher.QueueOverflowException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.mos.vrfblk.tasks.CoinbaseWitnessProcessor

@NActorProvider
@Instantiate
@Provides(specifications = Array(classOf[ActorService], classOf[IActor], classOf[CMDService]))
class PSCoinbaseW extends PSMVRFNet[PSCoinbase] {
  override def service = PSCoinbaseWitness
}

//
// http://localhost:8000/fbs/xdn/pbget.do?bd=
object PSCoinbaseWitness extends LogHelper with PBUtils with LService[PSCoinbase] with PMNodeHelper {
  
  val witnessByBCUID = new ConcurrentHashMap[String,AtomicInteger]()
  //defer put!
  
  override def onPBPacket(pack: FramePacket, pbo: PSCoinbase, handler: CompleteHandler) = {
    //    log.debug("Mine Block From::" + pack.getFrom())
    if (!VCtrl.isReady()) {
      log.debug("VCtrl not ready");
      //     ! NodeStateSwitcher.offerMessage(new Initialize());
      handler.onFinished(PacketHelper.toPBReturn(pack, pbo))
    } else {
      //      if (VCtrl.curVN().getCurBlock + VConfig.MAX_SYNC_BLOCKS > pbo.getBlockHeight) {
      try {
        CoinbaseWitnessProcessor.offerMessage(new NotaryBlock(pbo));
      } catch {
        case qe: QueueOverflowException =>
          
      }
      //      }
      handler.onFinished(PacketHelper.toPBReturn(pack, pbo))
    }
  }

  //  override def getCmds(): Array[String] = Array(PWCommand.LST.name())
  override def cmd: String = PCommand.CBW.name();
}
