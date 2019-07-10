package com.liewmanchoi;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * @author wangsheng
 * @date 2019/7/8
 */
public class UUIDTest {
  private static String getNewMac() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!nif.getName().equalsIgnoreCase("wlan0")) {
          System.out.println(nif.getName());
          continue;
        }

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return null;
        }

        StringBuilder res = new StringBuilder();
        for (byte b : macBytes) {
          res.append(String.format("%02X", b));
        }

        return res.toString();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println(getNewMac());
  }
}
