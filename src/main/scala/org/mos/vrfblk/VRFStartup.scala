package org.mos.vrfblk

import java.util.concurrent.TimeUnit

import org.apache.felix.ipojo.annotations.Invalidate
import org.apache.felix.ipojo.annotations.Validate
import org.mos.p22p.utils.LogHelper

import com.google.protobuf.Message

import onight.osgi.annotation.NActorProvider
import onight.tfw.outils.serialize.UUIDGenerator
import org.mos.vrfblk.utils.VConfig
import org.mos.vrfblk.tasks.VCtrl
import org.mos.vrfblk.tasks.VRFController
import org.mos.vrfblk.tasks.BeaconGossip
import org.mos.vrfblk.tasks.BlockProcessor
import org.mos.vrfblk.tasks.NodeStateSwitcher
import org.mos.vrfblk.tasks.BlockSync
import org.mos.vrfblk.tasks.BeaconTask
import org.mos.bcrand.model.Bcrand.PSNodeGraceShutDown
import org.mos.vrfblk.tasks.TxSync
import org.mos.vrfblk.tasks.TransactionSync
import org.mos.mcore.tools.url.URLHelper
import org.mos.vrfblk.tasks.ChainKeySync
import org.mos.vrfblk.tasks.ChainKeySyncHelper
import org.mos.vrfblk.tasks.CoinbaseWitnessProcessor

@NActorProvider
class VRFStartup extends PSMVRFNet[Message] {

  override def getCmds: Array[String] = Array("SSS");

  @Validate
  def init() {

    //    System.setProperty("java.protocol.handler.pkgs", "org.fc.mos.url");
    log.debug("startup:");
    new Thread(new VRFBGLoader()).start()

    log.debug("tasks inited....[OK]");
  }

  @Invalidate
  def destory() {
    //    !!DCtrl.instance.isStop = true;
  }

}

class VRFBGLoader() extends Runnable with LogHelper {
  def run() = {
    URLHelper.init();
    while (!Daos.isDbReady() //        || MessageSender.sockSender.isInstanceOf[NonePackSender]
    ) {
      log.debug("Daos Or sockSender Not Ready..:pzp=" + Daos.pzp + ",dbready=" + Daos.isDbReady())
      Thread.sleep(1000);
    }

    var vrfnet = Daos.pzp.networkByID("vrf")

    while (vrfnet == null
      || vrfnet.node_bits().bitCount <= 0 || !vrfnet.inNetwork()) {
      vrfnet = Daos.pzp.networkByID("vrf")
      if (vrfnet != null) {
        MDCSetBCUID(vrfnet)
      }
      Thread.sleep(1000);
    }
    Daos.chainHelper.startBlockChain(vrfnet.root().bcuid, vrfnet.root().v_address, vrfnet.root().name)
    UUIDGenerator.setJVM(vrfnet.root().bcuid.substring(1))
    vrfnet.changeNodeVAddr(vrfnet.root().v_address);
    
    log.info("vrfnet.initOK:My Node=" + vrfnet.root() + ",CoAddr=" + vrfnet.root().v_address
      + ",vctrl.tick=" + Math.min(VConfig.TICK_DCTRL_MS, VConfig.BLK_EPOCH_MS)) // my node

    VCtrl.instance = VRFController(vrfnet);
    Array(BeaconGossip, BlockProcessor, NodeStateSwitcher, BlockSync,CoinbaseWitnessProcessor).map(f => {
      f.startup(Daos.ddc.getExecutorService("vrf"));
    })

    //    BeaconGossip.startup(Daos.ddc);
    VCtrl.instance.startup();

    Daos.ddc.scheduleWithFixedDelay(BeaconTask, VConfig.INITDELAY_GOSSIP_SEC,
      VConfig.TICK_GOSSIP_SEC, TimeUnit.SECONDS);
    val messageId = UUIDGenerator.generate();
    val body = PSNodeGraceShutDown.newBuilder().setReason("shutdown").build();

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() = {
        VCtrl.network().wallMessage("SOSVRF", Left(body), messageId, '9');
      }
    })

    TxSync.instance = TransactionSync(VCtrl.network());
    Daos.ddc.scheduleWithFixedDelay(TxSync.instance, VConfig.INITDELAY_GOSSIP_SEC,
      Math.min(VConfig.TICK_DCTRL_MS_TX, VConfig.TXS_EPOCH_MS), TimeUnit.MILLISECONDS)

    ChainKeySyncHelper.instance = ChainKeySync(VCtrl.network());
    Daos.ddc.scheduleWithFixedDelay(ChainKeySyncHelper.instance, VConfig.INITDELAY_GOSSIP_SEC,
      VConfig.CHAINKEY_EPOCH_MS, TimeUnit.MILLISECONDS)

    //    Scheduler.schedulerForDCtrl.scheduleWithFixedDelay(DCtrl.instance, DConfig.INITDELAY_DCTRL_SEC,
    //      Math.min(DConfig.TICK_DCTRL_MS, DConfig.BLK_EPOCH_MS), TimeUnit.MILLISECONDS)
    //    Daos.ddc.scheduleWithFixedDelay(VCtrl.instance, VConfig.INITDELAY_DCTRL_SEC,
    //      Math.min(VConfig.TICK_DCTRL_MS, VConfig.BLK_EPOCH_MS),TimeUnit.MILLISECONDS)

    //!!    TxSync.instance = TransactionSync(dposnet);

    //!!    Daos.ddc.scheduleWithFixedDelay(TxSync.instance, DConfig.INITDELAY_DCTRL_SEC,
    //!!      Math.min(DConfig.TICK_DCTRL_MS_TX, DConfig.TXS_EPOCH_MS),TimeUnit.MILLISECONDS)

  }
}