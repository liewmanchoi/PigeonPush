package com.liewmanchoi.service.api;

import com.liewmanchoi.domain.message.PushMessage;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
public interface MessagePersistService {
  void persistMessage(PushMessage message);
}
