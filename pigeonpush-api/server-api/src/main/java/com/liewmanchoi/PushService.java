package com.liewmanchoi;

import com.liewmanchoi.domain.message.PushMessage;
import java.util.List;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
public interface PushService {

  /**
   * 消息推送<br> 如果消息体为空，则必须先查询Redis获取对应的消息体
   *
   * @param messages 待推送的消息列表
   */
  void pushMessage(List<PushMessage> messages);
}
