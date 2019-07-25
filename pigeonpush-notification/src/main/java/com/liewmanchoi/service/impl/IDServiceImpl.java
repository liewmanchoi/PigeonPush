package com.liewmanchoi.service.impl;

import com.liewmanchoi.config.IDConfig;
import com.liewmanchoi.service.api.IDService;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Snowflake算法<br>
 * https://www.cnblogs.com/lirenzuo/p/8440413.html<br>
 * Twitter_Snowflake<br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。<br>
 * 41位的时间截，可以使用69年，年数T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位数据中心Id和5位工作机器Id<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 *
 * @author wangsheng
 * @date 2019/7/23
 */
@Service
public class IDServiceImpl implements IDService {
  /** 开始时间戳 */
  private final long timeEpoch = 1563883600000L;
  /** 机器id所占的位数 */
  private final long workerIdBits = 5L;
  /** 数据中心id所占的位数 */
  private final long dataCenterIdBits = 5L;
  /** 支持的最大机器id */
  private final long maxWorkerId = ~(-1L << workerIdBits);
  /** 支持的最大数据中心id */
  private final long maxDataCenterId = ~(-1L << dataCenterIdBits);
  /** 序列号在id中所占的位数 */
  private final long sequenceBits = 12L;
  /** 机器id左移的位数 */
  private final long workerIdShift = sequenceBits;
  /** 数据标识id左移的位数 */
  private final long dataCenterIdShift = sequenceBits + workerIdBits;
  /** 时间戳左移的位数 */
  private final long timestampShift = sequenceBits + workerIdBits + dataCenterIdBits;
  /** 生成序列的掩码 */
  private final long sequenceMask = ~(-1L << sequenceBits);
  @Autowired private IDConfig idConfig;
  /** 工作机器id */
  private long workerId;
  /** 数据中心id */
  private long dataCenterId;
  /** 毫秒内序列 */
  private long sequence = 0L;
  /** 上次生成id时使用的时间戳 */
  private long lastTimestamp = -1L;

  @Override
  public Long generateID() {
    return nextId();
  }

  @PostConstruct
  public void init() {
    long workId = idConfig.getWorkerId();
    long dataCenterId = idConfig.getDataCenterId();

    if (workId < 0L || workId > maxWorkerId) {
      throw new IllegalArgumentException(String.format("workerId不能小于0或者大于%d", maxWorkerId));
    }

    if (dataCenterId < 0L || dataCenterId > maxDataCenterId) {
      throw new IllegalArgumentException(String.format("workerId不能小于0或者大于%d", maxWorkerId));
    }

    this.workerId = workId;
    this.dataCenterId = dataCenterId;
  }

  /**
   * 生成下一个id（线程安全）
   */
  private synchronized long nextId() {
    long timestamp = currentTimeStamp();
    if (timestamp < lastTimestamp) {
      // 如果当前时间戳小于上次生成id时使用的时间戳，说明发生过时间回拨，直接抛出异常
      throw new RuntimeException(
          String.format("发生了时间回拨，在[%d]毫毛内无法提供id生成服务", lastTimestamp - timestamp));
    }

    if (timestamp == lastTimestamp) {
      // 如果是在同一毫秒内，则进行毫秒内序列生成
      sequence = (sequence + 1) & sequenceMask;

      if (sequence == 0) {
        // 如果sequence溢出，则阻塞至下一毫秒
        timestamp = tillNextMills(lastTimestamp);
      }
    } else {
      // 如果不在同一毫秒内，则重置sequence值
      sequence = 0L;
    }

    lastTimestamp = timestamp;
    // 组装最终生成的id号
    return ((timestamp - timeEpoch) << timestampShift)
        | (dataCenterId << dataCenterIdShift)
        | (workerId << workerIdShift)
        | sequence;
  }

  private long currentTimeStamp() {
    return System.currentTimeMillis();
  }

  /**
   * 阻塞直至生成在给定时间点之后的时间戳
   *
   * @param lastTimeStamp 给定时间戳
   * @return 生成时间戳
   */
  private long tillNextMills(long lastTimeStamp) {
    long timestamp = currentTimeStamp();

    while (timestamp <= lastTimeStamp) {
      timestamp = currentTimeStamp();
    }

    return timestamp;
  }
}
