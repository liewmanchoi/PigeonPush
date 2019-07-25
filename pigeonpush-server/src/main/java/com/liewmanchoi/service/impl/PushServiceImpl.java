package com.liewmanchoi.service.impl;

import com.liewmanchoi.PushService;
import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.domain.message.Message;
import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.server.Server;
import com.liewmanchoi.service.ACKService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangsheng
 * @date 2019/7/20
 */
@Slf4j
@Component
@org.apache.dubbo.config.annotation.Service(
    version = "1.0.0",
    timeout = 10000,
    interfaceClass = PushService.class)
public class PushServiceImpl implements PushService {
  @Autowired private Server server;
  @Autowired private ACKService ackService;
  @Autowired private RedisDAO redisDAO;

  @Override
  public void pushMessage(PushMessage pushMessage) {
    if (pushMessage == null || !pushMessage.isValid()) {
      return;
    }
    final Long messageID = pushMessage.getMessageId();
    final String clientID = pushMessage.getClientId();

    // 如果消息体为空，查询后填充
    if (pushMessage.bodyIsEmpty()) {
      log.info(">>> 消息[{}]的消息体为空，正在向Redis查询   <<<", messageID);

      Map<String, String> bodyMap = redisDAO.getMessageBody(messageID);

      if (bodyMap == null) {
        // 如果查找不到对应的消息体，拒绝发送
        log.warn(">>>   无法查找到消息[{}]的消息体，不再推送消息   <<<", messageID);
        // 消息无法发送，应视作已经成功过送达（相当于收到ACK确认消息）
        ackService.handleACK(clientID, messageID);
        return;
      }

      final String title = bodyMap.get("title");
      final String text = bodyMap.get("text");

      if (title == null && text == null) {
        // 如果查找到的消息体为空，说明发生了异常，也应该拒绝发送
        log.warn(">>>   查找到[{}]的消息体为空，不再推送消息   <<<", messageID);
        // 消息无法发送，应视作已经成功送达（相当于收到ACK确认消息）
        ackService.handleACK(clientID, messageID);
        return;
      }

      // 填充消息体
      pushMessage.setTitle(title);
      pushMessage.setText(text);
    }

    // 构造Message并发送
    Message message = Message.buildPush(pushMessage);

    server.push(message);
  }
}
