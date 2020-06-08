package org.mos.vrfblk.utils

import java.util.concurrent.TimeUnit

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

import onight.oapi.scala.traits.OLog
import org.mos.mcore.model.Transaction.TransactionInfo

object TxCache extends OLog {

  val recentBlkTx: Cache[String, TransactionInfo] =
    CacheBuilder.newBuilder().expireAfterWrite(VConfig.MAX_WAIT_BLK_EPOCH_MS, TimeUnit.SECONDS)
      .maximumSize(VConfig.TX_MAX_CACHE_SIZE).build().asInstanceOf[Cache[String, TransactionInfo]]

  def cacheTxs(txs: java.util.List[TransactionInfo]): Unit = {
    val s = txs.size() - 1;
    for (i <- 0 to s) {
      val tx = txs.get(i);
      recentBlkTx.put(new String(tx.getHash.toByteArray()), tx);
    }
  }

  def getTx(txhash: String): TransactionInfo = {
    val ret = recentBlkTx.getIfPresent(txhash);
    if (ret != null) {
      ret
    } else {
      null
    }
  }
}