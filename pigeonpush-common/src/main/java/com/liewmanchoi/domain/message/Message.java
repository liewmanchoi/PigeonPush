package com.liewmanchoi.domain.message;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用消息格式
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
  private static final long serialVersionUID = 2037446596875616637L;

  private static final byte PING = 1;
  private static final byte PONG = 1 << 1;
  private static final byte ACK = 1 << 2;
  private static final byte PUSH = 1 << 3;
  /**
   * 消息类型，一共分为PUSH/ACK/PING/PONG四种
   */
  private byte type;

  PMessage pMessage;

  public static Message buildPush(int messageId, int clientId, String text) {
    return new Message(Message.PUSH, new PMessage(messageId, clientId, text));
  }

  public static Message buildACK(int messageId, int clientId) {
    return new Message(Message.ACK, new PMessage(messageId, clientId, null));
  }

  public static Message buildPING(int clientId) {
    return new Message(Message.PING, new PMessage(null, clientId, null));
  }

  public static Message buildPONG() {
    return new Message(Message.PONG, null);
  }
}
