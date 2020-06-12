
package org.mos.vrfblk.action

import org.apache.felix.ipojo.annotations.Instantiate
import org.apache.felix.ipojo.annotations.Provides
import org.mos.bcrand.model.Bcrand.PCommand
import org.mos.bcrand.model.Bcrand.PSCoinbase
import org.mos.vrfblk.PSMVRFNet
import org.mos.vrfblk.tasks.BlockProcessor
import org.mos.vrfblk.tasks.VCtrl
import org.mos.p22p.action.PMNodeHelper
import org.mos.p22p.utils.LogHelper

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
import org.mos.bcrand.model.Bcrand.SyncChainKey
import org.mos.vrfblk.Daos
import org.mos.bcrand.model.Bcrand.RespSyncChainKey
import org.mos.bcrand.model.Bcrand.RespSyncChainKey
import java.util.Date
import org.mos.vrfblk.utils.VConfig
import org.mos.mcore.model.Chain.ChainKeyList
import org.mos.mcore.model.Chain.ChainKeyList

@NActorProvider
@Instantiate
@Provides(specifications = Array(classOf[ActorService], classOf[IActor], classOf[CMDService]))
class PDCoinbaseQ extends PSMVRFNet[SyncChainKey] {
  override def service = PDCoinbaseQuery
}

//
// http://localhost:8000/fbs/xdn/pbget.do?bd=
object PDCoinbaseQuery extends LogHelper with PBUtils with LService[SyncChainKey] with PMNodeHelper {
  override def onPBPacket(pack: FramePacket, pbo: SyncChainKey, handler: CompleteHandler) = {
    //    log.debug("Mine Block From::" + pack.getFrom())
    var oRespSyncChainKey = RespSyncChainKey.newBuilder();
    if (!VCtrl.isReady()) {
      log.debug("VCtrl not ready");
      handler.onFinished(PacketHelper.toPBReturn(pack, pbo))
    } else {
      var now:Date = new Date();
      var begin:Date = new Date((pbo.getTimestamp.toLong*1000l));
      var between:Long=(now.getTime()-begin.getTime())/1000;
      if (between > 60) {
        oRespSyncChainKey.clear();
      } else {
        var signTxt = pbo.getSignature();
        
        var hexPubKey = Daos.enc.signatureToKey(pbo.toBuilder().clearSignature().build().toByteArray(), Daos.enc.hexStrToBytes(signTxt));
        if(Daos.enc.verify(hexPubKey, pbo.toByteArray(), Daos.enc.hexStrToBytes(signTxt))) {
          var hexAddress = Daos.enc.signatureToAddress(pbo.toBuilder().clearSignature().build().toByteArray(), Daos.enc.hexStrToBytes(signTxt));
          if (VConfig.AUTH_NODE_FILTER && VCtrl.haveEnoughToken(Daos.enc.bytesToHexStr(hexAddress))) {
            var keyList = ChainKeyList.newBuilder();
            keyList.putAllChainKeys(Daos.accountHandler.getChainConfig.getChainKeys);
            
            Daos.enc.encrypt(Daos.enc.hexStrToBytes(pbo.getTmpPubKey) ,keyList.build().toByteArray());
          } else {
            oRespSyncChainKey.clear();
          }
        } else {
          oRespSyncChainKey.clear();
        }
      }
      
      handler.onFinished(PacketHelper.toPBReturn(pack, oRespSyncChainKey))
    }
  }

  //  override def getCmds(): Array[String] = Array(PWCommand.LST.name())
  override def cmd: String = PCommand.SCK.name();
}
