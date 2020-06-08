package org.mos.vrfblk.utils

import onight.tfw.outils.conf.PropHelper
import java.math.BigInteger

object VConfig {
  val prop: PropHelper = new PropHelper(null);

  val PROP_DOMAIN = "org.bc.vrf." //前缀

  val GOSSIP_TIMEOUT_SEC = prop.get(PROP_DOMAIN + "gossip.timeout.sec", 60); //2 seconds each block

  val BLOCK_MAKE_TIMEOUT_SEC = prop.get(PROP_DOMAIN + "block.timeout.sec", 60); //2 seconds each block
  
  val BLOCK_NOTARY_TIMEOUT_SEC = prop.get(PROP_DOMAIN + "block.notary.timeout.sec", 180); //2 seconds each block

  val SLICE_ID = prop.get(PROP_DOMAIN + "slice.id", 0); //2 seconds each block

  //gossip线程初始化等待时间
  val INITDELAY_GOSSIP_SEC = prop.get(PROP_DOMAIN + "initdelay.gossip.sec", 60);

  //gossip线程检查时间
  val TICK_GOSSIP_SEC = prop.get(PROP_DOMAIN + "tick.gossip.sec", 120);

  //打块时间；按秒计算，（已经废弃，后面用毫秒取代）
  val _DBLK_EPOCH_SEC = prop.get(PROP_DOMAIN + "blk.epoch.sec", 1); //2 seconds each block

  //打块时间：按毫秒计算，
  val BLK_EPOCH_MS = prop.get(PROP_DOMAIN + "blk.epoch.ms", 1000); //2 seconds each block
  //没有交易时候的最大打块时间
  val BLK_NOOP_EPOCH_MS = prop.get(PROP_DOMAIN + "blk.noop.epoch.ms", 1000); //2 seconds each block

  //有交易的时候，加快打块时间，打块最小等待时间
  val BLK_MIN_EPOCH_MS = prop.get(PROP_DOMAIN + "blk.min.epoch.ms", 10); //2 seconds each block

  //等待同步块的时间，防止总是循环请求
  val BLK_WAIT_SYNC_SEC = prop.get(PROP_DOMAIN + "blk.wait.sync.sec", 1); //2 seconds each block

  // 控制延迟，Tx每次循环状态转变需要等待的时间
  val TXS_EPOCH_MS = prop.get(PROP_DOMAIN + "txs.epoch.ms", 1000);
  
  // 控制延迟，ChainKey每次循环状态转变需要等待的时间
  val CHAINKEY_EPOCH_MS = prop.get(PROP_DOMAIN + "chainkey.epoch.ms", 10000);
  
  //每个块超时最大等待时间
  val MAX_WAIT_BLK_EPOCH_MS = prop.get(PROP_DOMAIN + "max.wait.blk.epoch.ms", 60000); //1 min to wait for next block mine

  //同步块时候，每一页最大块的数量
  val SYNCBLK_PAGE_SIZE = prop.get(PROP_DOMAIN + "syncblk.page.size", 1000);

  val SYNC_TX_SLEEP_MS = prop.get(PROP_DOMAIN + "synctx.sleep.ms", 0);
  
  val SYNC_TX_SLEEP_MAX_MS = prop.get(PROP_DOMAIN + "synctx.sleep.max.ms", 3000);

  //Term投票的比例
  val VOTE_QUORUM_RATIO = prop.get(PROP_DOMAIN + "vote.quorum.ratio", 60); //60%

  //同步块，并发控制，最大的请求数
  val SYNCBLK_MAX_RUNNER = prop.get(PROP_DOMAIN + "syncblk.max.runner", 1);

  //同步块，达到并发限制的时候，需要等待的时间
  val SYNCBLK_WAITSEC_NEXTRUN = prop.get(PROP_DOMAIN + "syncblk.waitsec.nextrun", 1);

  //每个term里面最多出块数量（已废弃）
  val MAX_BLK_COUNT_PERTERM = prop.get(PROP_DOMAIN + "max.blk.count.perterm", 10);

  // 每个term里面最小出块数量（已废弃）
  val MIN_BLK_COUNT_PERTERM = prop.get(PROP_DOMAIN + "min.blk.count.perterm", 3);

  //每次状态（出块，同步等）先成功循环等待时间, 秒单位，已取消
  val TICK_BLKCTRL_SEC = prop.get(PROP_DOMAIN + "tick.blkctrl.sec", 1);

  //控制线程初始化等待时间
  val INITDELAY_BLKCTRL_SEC = prop.get(PROP_DOMAIN + "initdelay.blkctrl.sec", 1);

  //成为cominer（挖矿工）时，最多允许和当前全网高度相差多少块
  val DTV_BEFORE_BLK = prop.get(PROP_DOMAIN + "dtv.before.blk", 5);

  //term进行xbft投票时的超时时间
  val DTV_TIMEOUT_SEC = prop.get(PROP_DOMAIN + "dtv.timeout.sec", 60);

  //每个term，打块的数量，Term出块范围=DTV_MUL_BLOCKS_EACH_TERM*conodes*DTV_BLOCKS_EACH_MINER
  val DTV_MUL_BLOCKS_EACH_TERM = prop.get(PROP_DOMAIN + "dtv.mul.blocks.each.term", 8);
  //每次打块节点出块的数量，Term出块范围=DTV_MUL_BLOCKS_EACH_TERM*conodes*DTV_BLOCKS_EACH_MINER
  val DTV_BLOCKS_EACH_MINER = prop.get(PROP_DOMAIN + "dtv.blocks.each.miner", 3);

  //超级节点最多数量
  val DTV_MAX_SUPER_MINER = prop.get(PROP_DOMAIN + "dtv.max.super.miner", 19);
  //超级节点最少数量
  val DTV_MIN_SUPER_MINER = prop.get(PROP_DOMAIN + "dtv.min.super.miner", 5);

  //等待其他节点出块时，随机等待时间的基数，sleep_ms=rand()%DTV_TIME_MS_EACH_BLOCK
  val DTV_TIME_MS_EACH_BLOCK = prop.get(PROP_DOMAIN + "dtv.time.ms.each_block", 2000);

  //每次状态（出块，同步等）先成功循环等待时间，毫秒
  val TICK_DCTRL_MS = prop.get(PROP_DOMAIN + "tick.dctrl.ms", BLK_EPOCH_MS);
  //交易同步线程的间隔查询时间
  val TICK_DCTRL_MS_TX = prop.get(PROP_DOMAIN + "tick.dctrl.ms.tx", TXS_EPOCH_MS);

  //控制线程初始化等待时间
  val INITDELAY_DCTRL_SEC = prop.get(PROP_DOMAIN + "initdelay.dctrl.sec", 8);

  //城为挖矿节点，需要追上当前全网高度的差距
  val BLOCK_DISTANCE_COMINE = prop.get(PROP_DOMAIN + "block.distance.comine", 3);

  val BLOCK_DISTANCE_NETBITS = prop.get(PROP_DOMAIN + "block.distance.netbits", 2);
  
  val BLOCK_DISTANCE_WAITMS = prop.get(PROP_DOMAIN + "block.distance.waitsec", 10)*1000;

  //term投票被否决是最少等待时间，sleep = random(BAN_MINSEC_FOR_VOTE_REJECT,BAN_MAXSEC_FOR_VOTE_REJECT)
  val BAN_MINSEC_FOR_VOTE_REJECT = prop.get(PROP_DOMAIN + "ban.minsec.for.vote.reject", 20);
  //term投票被否决是最大等待时间，sleep = random(BAN_MINSEC_FOR_VOTE_REJECT,BAN_MAXSEC_FOR_VOTE_REJECT)
  val BAN_MAXSEC_FOR_VOTE_REJECT = prop.get(PROP_DOMAIN + "ban.maxsec.for.vote.reject", 120);

  //节点超时出块后，投票的等待时间
  val MAX_TIMEOUTSEC_FOR_REVOTE = prop.get(PROP_DOMAIN + "max.timeoutsec.for.revote", 30);

  //每个块最大的交易数
  val MAX_TNX_EACH_BLOCK = prop.get(PROP_DOMAIN + "max.tnx.each.block", 10000);

  //每个块最小的交易数
  val MIN_TNX_EACH_BLOCK = prop.get(PROP_DOMAIN + "min.tnx.each.block", 1000);

  //动态调整出块时，包含的交易数，随着tx执行时间控制。最大块执行时间，连续出现次数超过ADJUST_BLOCK_TX_CHECKTIMES且大于该值，则每个块的交易数要减少ADJUST_BLOCK_TX_STEP
  val ADJUST_BLOCK_TX_MAX_TIMEMS = prop.get(PROP_DOMAIN + "adjust.block.tx.max.timems", 60000);
  //动态调整出块时，包含的交易数，随着tx执行时间控制。最小块执行时间，连续出现次数超过ADJUST_BLOCK_TX_CHECKTIMES且小于该值，则每个块的交易数要增加ADJUST_BLOCK_TX_STEP
  val ADJUST_BLOCK_TX_MIN_TIMEMS = prop.get(PROP_DOMAIN + "adjust.block.tx.min.timems", 8000);
  //动态调整出块时，包含的交易数，随着tx执行时间控制。每次调整的增量/减量值
  val ADJUST_BLOCK_TX_STEP = prop.get(PROP_DOMAIN + "adjust.block.tx.step", 10000);
  //动态调整出块时，包含的交易数，随着tx执行时间控制。每次调整，需要连续出现的次数
  val ADJUST_BLOCK_TX_CHECKTIMES = prop.get(PROP_DOMAIN + "adjust.block.tx.checktimes", 2);

  //广播交易时最大的数量
  val MAX_TNX_EACH_BROADCAST = prop.get(PROP_DOMAIN + "max.tnx.each.broadcast", 3000);
  //广播交易时最少的数量
  val MIN_TNX_EACH_BROADCAST = prop.get(PROP_DOMAIN + "min.tnx.each.broadcast", 10);

  //出块cws的押金
  val MAX_CWS_GUARANTY = prop.get(PROP_DOMAIN + "max.cws.guaranty", 10);

  //高度相同后需要等待多少个term以上才能变成cominer
  val COMINER_WAIT_BLOCKS_TODUTY = prop.get(PROP_DOMAIN + "cominer.wait.blocks.toduty", 60);

  //内存最大缓存term的数量
  val MAX_POSSIBLE_TERMID = prop.get(PROP_DOMAIN + "max.possible.termid", 100);

  //节点心跳的间隔时间
  val HEATBEAT_TICK_SEC = prop.get(PROP_DOMAIN + "heatbeat.tick.sec", 600);

  //节点心跳的超时时间
  val HEATBEAT_TIMEOUT_SEC = prop.get(PROP_DOMAIN + "heatbeat.timeout.sec", 60);
  //节点心跳失败次数，超过这个值，则认为节点已经掉线，从cominer里面移除
  val HEATBEAT_FAILED_COUNT = prop.get(PROP_DOMAIN + "heatbeat.failed.count", 2);

  //重启时是否重置term，而不是从数据库中读取
  val FORCE_RESET_VOTE_TERM = prop.get(PROP_DOMAIN + "force.reset.vote.term", 1);

  //每次同步块，最大的请求范围，
  val MAX_SYNC_BLOCKS = prop.get(PROP_DOMAIN + "max.sync.blocks", 3000);

  //同步安全块的范围
  val SYNC_SAFE_BLOCK_COUNT = prop.get(PROP_DOMAIN + "sync.safe.block.count", 10);

  //同步交易限流：每秒最大的tps
  val SYNC_TX_TPS_LIMIT = prop.get(PROP_DOMAIN + "sync.tx.tps.limit", 10000000); //每秒钟最多1万笔交易同步

  //当有交易时，低于这个值，则不做立即打块请求，需要sleep一段时间，介于【WAIT_BLOCK_MIN_TXN，WAIT_BLOCK_MAX_TXN】之间，则需要等待BLK_MIN_EPOCH_MS时间
  val WAIT_BLOCK_MIN_TXN = prop.get(PROP_DOMAIN + "wait.block.min.txn", 1); //至少100笔以上就不等了

  //当有交易时，大于这个值，则立即打块，不sleep，介于【WAIT_BLOCK_MIN_TXN，WAIT_BLOCK_MAX_TXN】之间，则需要等待BLK_MIN_EPOCH_MS时间
  val WAIT_BLOCK_MAX_TXN = prop.get(PROP_DOMAIN + "wait.block.max.txn", 800); //超过5000笔以上就不等了

  //并行处理个数，交易体同步
  val PARALL_SYNC_TX_BATCHBS = prop.get(PROP_DOMAIN + "parall.sync.tx.batchbs", 6);
  //并行处理个数，交易确认同步
  val PARALL_SYNC_TX_CONFIRM = prop.get(PROP_DOMAIN + "parall.sync.tx.confirm", 6);
  //并行处理个数，交易后确认同步
  val PARALL_SYNC_TX_WALLOUT = prop.get(PROP_DOMAIN + "parall.sync.tx.wallout", 6);

  //当前节点是否参与挖矿
  val RUN_COMINER = prop.get(PROP_DOMAIN + "run.cominer", 1);
  
  //节点准入Token名称
  val AUTH_TOKEN = prop.get(PROP_DOMAIN + "auth.token", "");
  //节点准入持有Token的最小值
  var AUTH_TOKEN_MIN: BigInteger = new BigInteger(prop.get(PROP_DOMAIN + "auth.token.min", "10000000"));
  var AUTH_NODE_FILTER = if ( prop.get("org.mos.evfs.node.filter", "off") == "off") false else true; 

  //同步交易时，在本地的缓存，给其他节点调用
  val TX_MAX_CACHE_SIZE = prop.get(PROP_DOMAIN + "tx.max.cache.size", 100000); //80%
  val TX_WALL_MAX_CACHE_SIZE = prop.get(PROP_DOMAIN + "tx.wall.max.cache.size", 300000); //80%
  val TX_CONFIRM_MAX_CACHE_SIZE = prop.get(PROP_DOMAIN + "tx.confirm.max.cache.size", 300000); //80%
  

  //连续打块不sleep的次数限制
  val DCTRL_CONTINUE_LOOP_COUNT = prop.get(PROP_DOMAIN + "dctrl.continue.loop.count", 100); //80%
  //出块后，块确认需要广播节点的比例 MAX 100 min 0
  val DCTRL_BLOCK_CONFIRMATION_RATIO = prop.get(PROP_DOMAIN + "block.confirmation.ratio", 0); //60%
  
  
    //最多有多少个矿机
  val MAX_BLOCK_MAKER = prop.get(PROP_DOMAIN + "max.block.maker", 21); //60%
  
  // 最多有多少个验证节点
  val MAX_BLOCK_NOTARY = prop.get(PROP_DOMAIN + "max.block.notary", 13); //60%

  val MAX_CONTINUE_BLOCK = prop.get(PROP_DOMAIN + "max.continue.block", 6); 
  
  val MAX_WAITMS_WHEN_LAST_BLOCK_NOT_APPLY = prop.get(PROP_DOMAIN + "max.waitms.when.last.block.not.apply", 5000);
  val BAN_BLOCKS_FOR_NOT_APPLY =  prop.get(PROP_DOMAIN + "ban.blocks.for.not.apply", 10);
  //最小可以使用的内存
  val METRIC_COMINER_MIN_FREE_MEMEORY_MB = prop.get(PROP_DOMAIN + "metric.cominer.min.free.memory.mb",512);
  val METRIC_SYNCTX_FREE_MEMEORY_MB = prop.get(PROP_DOMAIN + "metric.synctx.free.memory.mb",512);
  val MAX_COINBASE_QUEUE_SIZE = prop.get(PROP_DOMAIN + "max.coinbase.queue.size",100);
  //超过10次内存不足的警告，就不能做矿机了
  val METRIC_COMINER_LOW_MEMORY_COUNT = prop.get(PROP_DOMAIN + "metric.cominer.low.memory.count",10);
  
  val THREAD_COUNT_WITNESS = prop.get(PROP_DOMAIN + "thread.count.witness",10);
}

