package com.liewmanchoi.util;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangsheng
 * @date 2019/7/8
 */
@Slf4j
public class SDKUtil {
  private static String DEVICE_ID = null;

  public static String getDeviceId() {
    // 为了简单起见，直接使用wifi网卡mac地址作为设备的唯一标识符
    if (DEVICE_ID != null) {
      return DEVICE_ID;
    }

    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!"wlan0".equalsIgnoreCase(nif.getName())) {
          continue;
        }

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return null;
        }

        StringBuilder res = new StringBuilder();
        for (byte b : macBytes) {
          res.append(String.format("%02X:", b));
        }

        if (res.length() > 1) {
          res.deleteCharAt(res.length() - 1);
        }

        DEVICE_ID = res.toString();
      }
    } catch (Exception ex) {
      log.error("获取WIFi网卡mac地址失败", ex);
    }

    return DEVICE_ID;
  }
}
