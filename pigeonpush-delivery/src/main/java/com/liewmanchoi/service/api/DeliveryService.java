package com.liewmanchoi.service.api;

import com.liewmanchoi.domain.message.PushMessage;
import java.util.List;

/**
 * @author wangsheng
 * @date 2019/7/26
 */
public interface DeliveryService {
  void deliver(List<PushMessage> messages, boolean isPull);
}
