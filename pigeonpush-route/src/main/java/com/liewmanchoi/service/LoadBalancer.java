package com.liewmanchoi.service;

import java.net.InetSocketAddress;

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
  InetSocketAddress getPushServerAddress();
}
