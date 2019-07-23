package com.liewmanchoi.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * ip tool
 *
 * @author wangsheng
 */
@Slf4j
public class IpUtil {
  private static final String ANYHOST = "0.0.0.0";
  private static final String LOCALHOST = "127.0.0.1";
  private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
  private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
  private static final Pattern ADDRESS_PATTERN =
      Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}:\\d{1,5}$");
  private static volatile InetAddress LOCAL_ADDRESS = null;

  private static boolean isInvalidLocalHost(String host) {
    return host == null
        || host.length() == 0
        || "localhost".equalsIgnoreCase(host)
        || "0.0.0.0".equals(host)
        || (LOCAL_IP_PATTERN.matcher(host).matches());
  }

  public static boolean isValidLocalHost(String host) {
    return !isInvalidLocalHost(host);
  }

  public static InetAddress getLocalAddress() {
    return getLocalAddress(null);
  }

  /**
   * <pre>
   * 查找策略：首先看是否已经查到ip --> hostname对应的ip --> 根据连接目标端口得到的本地ip --> 轮询网卡
   * </pre>
   * @return local ip
   */
  public static InetAddress getLocalAddress(Map<String, Integer> destHostPorts) {
    if (LOCAL_ADDRESS != null) {
      return LOCAL_ADDRESS;
    }

    InetAddress localAddress = getLocalAddressByHostname();
    if (!isValidAddress(localAddress)) {
      localAddress = getLocalAddressBySocket(destHostPorts);
    }

    if (!isValidAddress(localAddress)) {
      localAddress = getLocalAddressByNetworkInterface();
    }

    if (isValidAddress(localAddress)) {
      LOCAL_ADDRESS = localAddress;
    }
    log.info("ip地址 >>> [{}]", LOCAL_ADDRESS);

    return localAddress;
  }

  private static InetAddress getLocalAddressByHostname() {
    try {
      InetAddress localAddress = InetAddress.getLocalHost();
      if (isValidAddress(localAddress)) {
        return localAddress;
      }
    } catch (Throwable e) {
      log.warn("Failed to retriving local address by hostname:" + e);
    }
    return null;
  }

  private static InetAddress getLocalAddressBySocket(Map<String, Integer> destHostPorts) {
    if (destHostPorts == null || destHostPorts.size() == 0) {
      return null;
    }

    for (Map.Entry<String, Integer> entry : destHostPorts.entrySet()) {
      String host = entry.getKey();
      int port = entry.getValue();
      try {
        Socket socket = new Socket();
        try {
          SocketAddress addr = new InetSocketAddress(host, port);
          socket.connect(addr, 1000);
          return socket.getLocalAddress();
        } finally {
          try {
            socket.close();
          } catch (Throwable e) {
            log.error("发生错误", e);
          }
        }
      } catch (Exception e) {
        log.warn(
            "Failed to retrieving local address by connecting to dest host:port([{}]:[{}]) false, e=[{}]",
            host,
            port,
            e);
      }
    }
    return null;
  }

  private static InetAddress getLocalAddressByNetworkInterface() {
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        try {
          NetworkInterface network = interfaces.nextElement();
          if (network.isLoopback()) {
            continue;
          }

          Enumeration<InetAddress> addresses = network.getInetAddresses();
          while (addresses.hasMoreElements()) {
            try {
              InetAddress address = addresses.nextElement();
              if (!(address instanceof Inet4Address)) {
                continue;
              }

              if (isValidAddress(address)) {
                return address;
              }
            } catch (Throwable e) {
              log.warn("Failed to retrieving ip address, " + e.getMessage(), e);
            }
          }
        } catch (Throwable e) {
          log.warn("Failed to retrieving ip address, " + e.getMessage(), e);
        }
      }
    } catch (Throwable e) {
      log.warn("Failed to retrieving ip address, " + e.getMessage(), e);
    }
    return null;
  }

  public static boolean isValidAddress(String address) {
    return ADDRESS_PATTERN.matcher(address).matches();
  }

  private static boolean isValidAddress(InetAddress address) {
    if (address == null || address.isLoopbackAddress()) {
      return false;
    }
    String name = address.getHostAddress();
    return (name != null
        && !ANYHOST.equals(name)
        && !LOCALHOST.equals(name)
        && IP_PATTERN.matcher(name).matches());
  }

  /**
   * return ip to avoid lookup dns
   *
   * @param socketAddress 地址
   * @return String
   */
  public static String getHostName(SocketAddress socketAddress) {
    if (socketAddress == null) {
      return null;
    }

    if (socketAddress instanceof InetSocketAddress) {
      InetAddress addr = ((InetSocketAddress) socketAddress).getAddress();
      if (addr != null) {
        return addr.getHostAddress();
      }
    }

    return null;
  }

  /**
   * get ip address
   *
   * @return String
   */
  public static String getIp() {
    return getLocalAddress().getHostAddress();
  }

  /**
   * get ip:port
   *
   * @param port 端口号
   * @return String
   */
  public static String getIpPort(int port) {
    String ip = getIp();
    return getIpPort(ip, port);
  }

  private static String getIpPort(String ip, int port) {
    if (ip == null) {
      return null;
    }
    return ip.concat(":").concat(String.valueOf(port));
  }

  public static Object[] parseIpPort(String address) {
    String[] array = address.split(":");

    String host = array[0];
    int port = Integer.parseInt(array[1]);

    return new Object[] {host, port};
  }
}
