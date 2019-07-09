package com.liewmanchoi.domain.user;

import java.io.Serializable;
import java.net.InetSocketAddress;
import lombok.Data;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
public class DeviceInfo implements Serializable {
  private static final long serialVersionUID = 8985907482107353772L;

  private String deviceId;
  private String deviceToken;
  private InetSocketAddress serverAddress;
}
