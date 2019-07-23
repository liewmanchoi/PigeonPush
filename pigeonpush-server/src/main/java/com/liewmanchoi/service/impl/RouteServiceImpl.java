package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.RouteService;
import com.liewmanchoi.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Service
public class RouteServiceImpl implements RouteService {
  @Autowired private RedisDAO redisDAO;

  @Override
  public void addRoute(String clientID) {
    String socketAddress = IpUtil.getLocalAddress().toString();
    redisDAO.addRoute(clientID, socketAddress);
  }

  @Override
  public void removeRoute(String clientID) {
    redisDAO.removeRoute(clientID);
  }
}
