package com.liewmanchoi.domain.message;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * 推送消息/ACK
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
@ToString
public class PushMessage implements Serializable {
  private static final long serialVersionUID = -4676901532250471660L;

  private Long messageId;
  private String clientId;
  private String title;
  private String text;

  public boolean isValid() {
    return messageId != null && clientId != null;
  }

  public boolean bodyIsEmpty() {
    return title == null || text == null;
  }
}
