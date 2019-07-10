package com.liewmanchoi.util;

import com.liewmanchoi.exception.ClientException;
import com.liewmanchoi.exception.ClientException.ErrorEnum;
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

  /**
   * 获取设备唯一标识码
   *
   * @return deviceId，获取失败返回null
   */
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
        return DEVICE_ID;
      }
    } catch (Exception ex) {
      log.error("获取WIFi网卡mac地址失败", ex);
      throw new ClientException(ErrorEnum.DEVICE_ID_FAILURE, "获取deviceId失败");
    }

    log.error("设备中不存在WIFI网卡");
    throw new ClientException(ErrorEnum.DEVICE_ID_FAILURE, "设备中不存在WIFI网卡");
  }
}
