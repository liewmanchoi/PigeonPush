package com.liewmanchoi.entity;

import java.util.List;
import lombok.Data;

/**
 * @author wangsheng
 * @date 2019/7/23
 */
@Data
public class RequestEntity {
  private List<String> cid;
  private String title;
  private String text;

  public boolean isValid() {
    return (cid != null && !cid.isEmpty())
        && ((title != null && !title.isEmpty()) || (text != null && !text.isEmpty()));
  }
}
