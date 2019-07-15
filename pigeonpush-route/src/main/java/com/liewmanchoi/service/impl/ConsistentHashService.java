package com.liewmanchoi.service.impl;

import com.google.common.collect.Iterables;
import com.liewmanchoi.service.LoadBalancer;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 改进的仿Redis Cluster一致性哈希算法
 *
 * <p>
 *
 * @author wangsheng
 * @date 2019/7/14
 * @see <a href=https://docs.datastax.com/en/archived/cassandra/3.0/cassandra/architecture/archDataDistributeHashing.html></a>
 * @see <a href=https://redis.io/topics/cluster-tutorial></a>
 */
@Slf4j
@Service
public class ConsistentHashService implements LoadBalancer {

  /**
   * 有序散列表，值为推送服务器地址，键为推送服务器负责的哈希值区段终点
   */
  private TreeSet<InetSocketAddress> slots;

  public ConsistentHashService() {
    slots = new TreeSet<>(Comparator.comparingInt(this::hash));
  }

  @Override
  public InetSocketAddress selectPushServer(List<InetSocketAddress> addresses, String key) {
    if (addresses == null || addresses.size() == 0) {
      return null;
    }

    if (addresses.size() == 1) {
      return addresses.get(0);
    }

    processSlots(addresses);
    int hashCode = hash(key);

    // 根据散列值的大小确定对应的推送服务器
    int n = slots.size();
    // 计算间隔长度
    int len = 0xffff / n;
    // 计算对应的服务器序号
    int k = hashCode / len;

    return Iterables.get(slots, k);
  }

  private synchronized void processSlots(List<InetSocketAddress> addresses) {
    // 必须保证并发安全
    slots.addAll(addresses);
  }

  /**
   * hash函数，InetSocketAddress的md5值的后16位
   *
   * @return 散列值
   */
  private int hash(Object object) {
    byte[] md5 = DigestUtils.md5Digest(object.toString().getBytes());

    // 提取为16位数
    return (md5[0] & 0xff) | ((md5[1] & 0xff) << 8);
  }
}
