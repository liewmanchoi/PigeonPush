package com.liewmanchoi.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangsheng
 * @date 2019/7/11
 */
@Data
@NoArgsConstructor
public class AppInfo implements Serializable {

  private static final long serialVersionUID = -2962025299689404053L;

  private String deviceId;
  private String keyToken;
  private Date updateTime;
  private Date tokenExpire;
}
