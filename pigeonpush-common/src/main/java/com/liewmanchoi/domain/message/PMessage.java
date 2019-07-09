package com.liewmanchoi.domain.message;

import io.netty.util.Recycler;
import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * 推送消息/ACK/PING
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
@ToString
public class PMessage implements Serializable {
  private static final long serialVersionUID = -4676901532250471660L;
  @ToString.Exclude private final transient Recycler.Handle<PMessage> handle;
  private Integer messageId;
  private Integer clientId;
  private String text;

  public PMessage(Recycler.Handle<PMessage> handle) {
    this.handle = handle;
  }

  public static boolean isValid(PMessage message) {
    return message.getMessageId() != null && message.getClientId() != null;
  }

  public void recycle() {
    this.messageId = null;
    this.clientId = null;
    this.text = null;
    handle.recycle(this);
  }
}
