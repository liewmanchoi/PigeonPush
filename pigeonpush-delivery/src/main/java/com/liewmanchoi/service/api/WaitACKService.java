package com.liewmanchoi.service.api;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
public interface WaitACKService {
  void addWaitACK(String clientID, Long messageID);
}
