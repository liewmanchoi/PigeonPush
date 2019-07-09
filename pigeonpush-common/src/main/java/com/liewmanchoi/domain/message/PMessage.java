package com.liewmanchoi.domain.message;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推送消息/ACK/PING
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PMessage implements Serializable {

  private static final long serialVersionUID = -4676901532250471660L;

  private Integer messageId;
  private Integer clientId;
  private String text;

  public static boolean isValid(PMessage message) {
    return message.getMessageId() != null || message.getClientId() != null;
  }

  public static boolean isPING(PMessage message) {
    if (!isValid(message)) {
      return false;
    }

    return message.getClientId() != null
        && message.getMessageId() == null
        && message.getText() == null;
  }
}
