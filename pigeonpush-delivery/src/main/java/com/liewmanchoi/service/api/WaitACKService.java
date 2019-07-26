package com.liewmanchoi.service.api;

import java.util.Set;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
public interface WaitACKService {
  void addWaitACK(String clientID, Long messageID);

  Set<Long> getMessageIds(String clientID);
}
