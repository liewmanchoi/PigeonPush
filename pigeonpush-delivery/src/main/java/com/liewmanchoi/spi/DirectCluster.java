package com.liewmanchoi.spi;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

/**
 * Dubbo Cluster扩展类，保证消息分发模块直接调用指定的推送服务器
 *
 * @author wangsheng
 * @date 2019/7/25
 */
public class DirectCluster implements Cluster {

  @Override
  public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
    return new DirectClusterInvoker<>(directory);
  }
}
