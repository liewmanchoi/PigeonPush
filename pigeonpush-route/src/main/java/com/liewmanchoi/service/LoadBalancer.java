package com.liewmanchoi.service;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author wangsheng
 * @date 2019/7/12
 */
public interface LoadBalancer {

  /**
   * 返回经负载均衡策略选择后产生的推送服务器地址
   *
   * @return InetSocketAddress
   */
  InetSocketAddress selectPushServer(List<InetSocketAddress> addresses, String key);
}
