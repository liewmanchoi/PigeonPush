package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Service
public class AuthServiceImpl implements AuthService {
  @Autowired private RedisDAO redisDAO;

  @Override
  public boolean checkToken(String clientID, String token) {
    if (clientID == null || token == null) {
      return false;
    }

    return token.equals(redisDAO.getToken(clientID));
  }

  @Override
  public void removeToken(String clientID) {
    redisDAO.removeToken(clientID);
  }
}
