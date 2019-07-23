package com.liewmanchoi.domain.message;

import com.liewmanchoi.util.GlobalRecycler;
import io.netty.util.Recycler;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 通用消息格式
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
public class Message implements Serializable {
  public static final byte PING = 1;
  public static final byte PONG = 1 << 1;
  public static final byte ACK = 1 << 2;
  public static final byte PUSH = 1 << 3;
  public static final byte AUTH_REQ = 1 << 4;
  public static final byte AUTH_RES = 1 << 5;
  private static final long serialVersionUID = 2037446596875616637L;
  public final transient Recycler.Handle<Message> handle;

  /** 消息类型，一共分为PUSH/ACK/PING/PONG/AUTH五种 */
  private byte type;

  private String clientId;
  /**
   * 扩展字段，用于传递鉴权/ACK等信息
   *
   * <p>ACK类型：传递messageId，类型为Integer<br>
   * AUTH_REQ类型：传递token，类型为String<br>
   * AUTH_RES类型：传递服务端发送的鉴权结果，类型为Integer
   */
  private Map<String, Object> attachment;

  public Message(Recycler.Handle<Message> handle) {
    this.handle = handle;
  }

  public static Message buildPING(String clientId) {
    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.PING);
    message.setClientId(clientId);
    return message;
  }

  public static Message buildPONG(String clientId) {
    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.PONG);
    message.setClientId(clientId);
    return message;
  }

  public static Message buildPush(PushMessage pushMessage) {
    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.PUSH);
    final String clientId = pushMessage.getClientId();

    message.setClientId(clientId);

    Map<String, Object> attachment = new HashMap<>(3);
    attachment.put("messageId", pushMessage.getMessageId());
    attachment.put("title", pushMessage.getTitle());
    attachment.put("text", pushMessage.getText());
    message.setAttachment(attachment);

    return message;
  }

  public static Message buildACK(String clientId, Long messageId) {
    Map<String, Object> attachment = new HashMap<>(1);
    attachment.put("messageId", messageId);

    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.ACK);
    message.setClientId(clientId);
    message.setAttachment(attachment);

    return message;
  }

  public static Message buildAuthReq(String clientId, String token) {
    Map<String, Object> attachment = new HashMap<>(1);
    attachment.put("token", token);

    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.AUTH_REQ);
    message.setClientId(clientId);
    message.setAttachment(attachment);

    return message;
  }

  public static Message buildAuthRes(String clientId, int code) {
    Map<String, Object> attachment = new HashMap<>(1);
    attachment.put("code", code);

    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.AUTH_RES);
    message.setClientId(clientId);
    message.setAttachment(attachment);

    return message;
  }

  public PushMessage getPushMessage() {
    if (type != PUSH) {
      return null;
    }

    PushMessage pushMessage = new PushMessage();
    pushMessage.setClientId(clientId);
    pushMessage.setMessageId((Long) attachment.get("messageId"));
    pushMessage.setTitle((String) attachment.get("title"));
    pushMessage.setText((String) attachment.get("text"));

    return pushMessage;
  }

  public Long getPushMessageID() {
    if (type != PUSH) {
      return null;
    }

    return (Long) attachment.get("messageId");
  }

  public Long getACKMessageID() {
    if (type != ACK) {
      return null;
    }

    return (Long) attachment.get("messageId");
  }

  /** 获取AUTH_REQ携带的token */
  public String getToken() {
    if (type != AUTH_REQ) {
      return null;
    }

    return (String) attachment.get("token");
  }

  public void recycle() {
    this.type = 0;
    this.clientId = null;
    this.attachment = null;
    handle.recycle(this);
  }

  public static class AuthState {

    public static final int SUCCESS = 0;
    public static final int FAILURE = -1;
  }
}
