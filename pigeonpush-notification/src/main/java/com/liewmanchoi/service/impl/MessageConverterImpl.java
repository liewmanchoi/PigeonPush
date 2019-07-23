package com.liewmanchoi.service.impl;

import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.entity.RequestEntity;
import com.liewmanchoi.service.api.IDService;
import com.liewmanchoi.service.api.MessageConverter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/23
 */
@Service
public class MessageConverterImpl implements MessageConverter {
  @Autowired
  private IDService idService;

  @Override
  public List<PushMessage> convert(RequestEntity entity) {
    if (entity == null) {
      return null;
    }

    // 生成消息全局唯一id
    long messageId = idService.generateID();
    // 获取服务端列表
    List<String> clientIdList = entity.getCid();
    String title = entity.getTitle();
    String text = entity.getText();

    List<PushMessage> messageList = new ArrayList<>();

    for (String clientId : clientIdList) {
      PushMessage message = new PushMessage();
      message.setMessageId(messageId);
      message.setClientId(clientId);
      message.setText(title);
      message.setText(text);

      messageList.add(message);
    }

    return messageList;
  }
}
