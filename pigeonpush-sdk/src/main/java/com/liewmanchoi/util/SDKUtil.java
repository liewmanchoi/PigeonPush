package com.liewmanchoi.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/8
 */
@Slf4j
public class SDKUtil {
  private static String CLIENT_ID = null;

  /**
   * 获取设备唯一标识码
   *
   * @return deviceId，获取失败返回null
   */
  public static String getClientId() {
    // 为了简单起见，直接使用ip地址作为设备的唯一标识符
    if (CLIENT_ID != null) {
      return CLIENT_ID;
    }

    byte[] ipBytes = IpUtil.getLocalAddress().getAddress();
    StringBuilder stringBuilder = new StringBuilder();
    for (byte b : ipBytes) {
      stringBuilder.append(String.format("%02X", b));
    }

    CLIENT_ID = stringBuilder.toString();
    return CLIENT_ID;
  }
}
