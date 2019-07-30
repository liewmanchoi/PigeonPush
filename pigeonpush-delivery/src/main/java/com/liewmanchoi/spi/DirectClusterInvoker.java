package com.liewmanchoi.spi;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcResult;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
public class DirectClusterInvoker<T> extends AbstractClusterInvoker<T> {
  public DirectClusterInvoker(Directory<T> directory) {
    super(directory);
  }

  @Override
  protected Result doInvoke(
      Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
      throws RpcException {
    try {
      // 1. 获取设置的ip地址
      String ipAddress = (String) RpcContext.getContext().get("ip");
      log.info(">>>   设置的远程ip地址为[{}]   <<<", ipAddress);
      if (StringUtils.isBlank(ipAddress)) {
        // 无法获取到ip地址，直接忽略，返回空的RpcResult
        log.error(">>>   没有设置被调用推送服务器的IP地址   <<<");
        return new RpcResult();
      }

      // 2.检查是否有可用的invoker
      checkInvokers(invokers, invocation);

      // 3. 根据指定的ip地址获取对应的invoker
      Invoker<T> targetInvoker =
          invokers.stream()
              .filter(invoker -> invoker.getUrl().getHost().equals(ipAddress))
              .findFirst()
              .orElse(null);
      if (targetInvoker == null) {
        // 如果没有对应的invoker，直接忽略，返回空的RpcResult
        log.error(">>>   无法找到IP地址为[{}]的推送服务器   <<<", ipAddress);
        return new RpcResult();
      }

      // 4. 发起远程调用
      return targetInvoker.invoke(invocation);
    } catch (Throwable throwable) {
      log.error(">>>   忽略异常：" + throwable.getMessage(), throwable);
      return new RpcResult();
    }
  }
}
