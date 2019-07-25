package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.service.api.MessagePersistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Service
public class MessagePersistServiceImpl implements MessagePersistService {
  @Autowired
  private RedisDAO redisDAO;

  @Override
  public void persistMessage(PushMessage message) {
    if (message == null || !message.isValid()) {
      log.warn(">>>   传入消息无效   <<<");
      return;
    }

    if (message.bodyIsEmpty()) {
      log.info(">>>   消息体为空，无法持久化   <<<");
      return;
    }

    redisDAO.addMessage(message.getMessageId(), message.getTitle(), message.getText());
  }
}
