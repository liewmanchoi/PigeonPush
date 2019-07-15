package com.liewmanchoi.service;

/**
 * @author wangsheng
 * @date 2019/7/11
 */
public interface AppAuthService {

  /**
   * 生成keyToken，并且持久化到redis集群中
   *
   * @param deviceId 用户唯一标识符
   * @return keyToken
   */
  String getAndPersistKeyToken(String deviceId);
}
