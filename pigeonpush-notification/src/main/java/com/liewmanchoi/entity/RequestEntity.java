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
}
