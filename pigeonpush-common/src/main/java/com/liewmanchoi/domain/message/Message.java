package com.liewmanchoi.domain.message;

import com.liewmanchoi.util.PMessageRecycler;
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
  public static final Message PING_MSG = new Message(Message.PING, null);
  private static final byte PONG = 1 << 1;
  public static final Message PONG_MSG = new Message(Message.PONG, null);
  private static final byte ACK = 1 << 2;
  private static final byte PUSH = 1 << 3;

  PMessage pMessage;
  /** 消息类型，一共分为PUSH/ACK/PING/PONG四种 */
  private byte type;

  private Message(byte type, PMessage pMessage) {
    this.type = type;
    this.pMessage = pMessage;
  }

  public static Message buildPush(int messageId, int clientId, String text) {
    PMessage pMessage = PMessageRecycler.reuse();
    pMessage.setMessageId(messageId);
    pMessage.setClientId(clientId);
    pMessage.setText(text);
    return new Message(Message.PUSH, pMessage);
  }

  public static Message buildACK(int messageId, int clientId) {
    PMessage pMessage = PMessageRecycler.reuse();
    pMessage.setMessageId(messageId);
    pMessage.setClientId(clientId);
    return new Message(Message.ACK, pMessage);
  }
}
