package com.liewmanchoi.zookeeper;

import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
@Slf4j
@NoArgsConstructor
public class ZookeeperRegistry {

  @Getter
  private CuratorFramework zkCli;

  public void init(
      String connectString,
      int connectionTimeoutMs,
      int sessionTimeoutMs,
      int baseSleepTimeMs,
      int maxRetries,
      String namespace) {
    zkCli =
        CuratorFrameworkFactory.builder()
            .connectString(connectString)
            .connectionTimeoutMs(connectionTimeoutMs)
            .sessionTimeoutMs(sessionTimeoutMs)
            .retryPolicy(new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries))
            .namespace(namespace)
            .build();

    // 添加回调，处理连接事件
    zkCli
        .getConnectionStateListenable()
        .addListener(
            (CuratorFramework client, ConnectionState newState) -> {
              switch (newState) {
                case CONNECTED:
                  log.info("连接zookeeper集群成功");
                  break;
                case RECONNECTED:
                  log.info("重新连接zookeeper集群");
                  break;
                default:
                  log.warn("与zookeeper集群的连接发生异常");
                  break;
              }
            });

    zkCli.start();
  }

  /**
   * 监听节点的子节点变化
   *
   * @param path 被监听的节点
   * @param biConsumer 回调函数
   * @return 成功设置监听，则返回<code>true</code>
   */
  public boolean discover(
      String path, BiConsumer<CuratorFramework, PathChildrenCacheEvent> biConsumer) {
    if (path == null || zkCli == null || biConsumer == null) {
      return false;
    }

    try {
      Stat stat = zkCli.checkExists().forPath(path);
      if (stat != null) {
        PathChildrenCache watcher = new PathChildrenCache(zkCli, path, true);
        watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        // 增加回调
        watcher.getListenable().addListener(biConsumer::accept);

        return true;
      }
    } catch (Exception e) {
      log.error("监听[{}]子节点发生异常", path, e);
    }

    return false;
  }
}
