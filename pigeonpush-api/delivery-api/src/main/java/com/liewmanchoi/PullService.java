package com.liewmanchoi;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
public interface PullService {

  /**
   * 拉取消息
   *
   * @param deviceId SDK唯一标识符
   */
  void pullMessage(String deviceId);
}
