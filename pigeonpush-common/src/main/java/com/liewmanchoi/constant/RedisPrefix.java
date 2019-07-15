package com.liewmanchoi.constant;

/**
 * @author wangsheng
 * @date 2019/7/14
 */
public class RedisPrefix {

  public static final String REG_PRE = "REG:";
  public static final String AUTH_PRE = "AUTH:";

  public static String getRedisKey(String appId, String functionName, Object id) {
    return appId + functionName + id;
  }
}
