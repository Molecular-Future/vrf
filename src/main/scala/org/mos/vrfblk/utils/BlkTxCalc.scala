package org.mos.vrfblk.utils

import java.util.concurrent.atomic.AtomicInteger

import onight.oapi.scala.traits.OLog


object BlkTxCalc extends OLog{

  var blockTx = VConfig.MAX_TNX_EACH_BLOCK

  def getBestBlockTxCount(termMaxBlk: Int): Int = {
    return blockTx;
  }
  
  val needChangeCounter = new AtomicInteger(0);

  def adjustTx(costMS: Long) = {
    if (costMS > VConfig.ADJUST_BLOCK_TX_MAX_TIMEMS) {
      if (blockTx > VConfig.MIN_TNX_EACH_BLOCK &&
          needChangeCounter.incrementAndGet()>VConfig.ADJUST_BLOCK_TX_CHECKTIMES) {
        blockTx = blockTx - VConfig.ADJUST_BLOCK_TX_STEP;
        blockTx = Math.max(blockTx, VConfig.MIN_TNX_EACH_BLOCK);
        log.error("slow down -- decrease tx count for each block to :"+blockTx);
        needChangeCounter.set(0)
      }
    } else if (costMS < VConfig.ADJUST_BLOCK_TX_MIN_TIMEMS) {
      if (blockTx < VConfig.MAX_TNX_EACH_BLOCK &&
          needChangeCounter.incrementAndGet()>VConfig.ADJUST_BLOCK_TX_CHECKTIMES) {
        blockTx = blockTx + VConfig.ADJUST_BLOCK_TX_STEP;
        log.error("speed up increase tx count for each block to :"+blockTx);
        needChangeCounter.set(0)
      }
    }
  }

}