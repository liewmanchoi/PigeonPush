package com.liewmanchoi.service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
public interface AuthService {
  boolean checkToken(String clientID, String token);
}
