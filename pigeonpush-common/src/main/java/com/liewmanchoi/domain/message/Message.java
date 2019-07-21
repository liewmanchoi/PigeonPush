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
  private static final long serialVersionUID = 2037446596875616637L;
  public static final byte PING = 1;
  public static final byte PONG = 1 << 1;
  public static final byte ACK = 1 << 2;
  public static final byte PUSH = 1 << 3;
  public static final byte AUTH_REQ = 1 << 4;
  public static final byte AUTH_RES = 1 << 5;
  public final transient Recycler.Handle<Message> handle;
  // TODO: 合并PushMessage至扩展字段
  /** 推送消息主体部分 */
  PushMessage pushMessage;
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

  public static Message buildPush(String clientId, PushMessage pushMessage) {
    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.PUSH);
    message.setClientId(clientId);
    message.setPushMessage(pushMessage);
    return message;
  }

  public static Message buildACK(String clientId, int messageId) {
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

  /**
   * 获取AUTH_REQ携带的token
   */
  public String getToken() {
    if (type != AUTH_REQ) {
      return null;
    }

    return (String) attachment.get("token");
  }

  // TODO: 增加getAuthReqToken/getAuthResCode()方法
  // TODO: 增加checkAuthToken()/checkAuthState()方法

  public static Message buildAuthRes(String clientId, int code) {
    Map<String, Object> attachment = new HashMap<>(1);
    attachment.put("code", code);

    Message message = GlobalRecycler.reuse(Message.class);
    message.setType(Message.AUTH_RES);
    message.setClientId(clientId);
    message.setAttachment(attachment);

    return message;
  }

  public static class AuthState {

    public static final int SUCCESS = 0;
    public static final int FAILURE = -1;
  }

  public void recycle() {
    this.type = 0;
    this.clientId = null;
    this.attachment = null;
    this.pushMessage = null;
    handle.recycle(this);
  }
}
