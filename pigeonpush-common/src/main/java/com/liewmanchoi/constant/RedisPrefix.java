package com.liewmanchoi.constant;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
public class RedisPrefix {

  public static final String ROUTE_PRE = "ROUTE:";
  public static final String AUTH_PRE = "AUTH:";
  public static final String ACK_PRE = "WAIT_ACK:";
  public static final String MSG_PRE = "MSG_BODY:";
  private static final String APP_PRE = "PIGEON_PUSH:";

  public static String getRedisKey(String funcID, Object id) {
    return APP_PRE + funcID + id;
  }
}
