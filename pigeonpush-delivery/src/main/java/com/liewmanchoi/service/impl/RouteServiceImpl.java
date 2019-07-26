package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.api.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Service
public class RouteServiceImpl implements RouteService {
  @Autowired private RedisDAO redisDAO;

  @Override
  public String getRoute(String clientID) {
    if (clientID == null) {
      log.warn(">>>   输入的参数有误   <<<");
      return null;
    }

    log.info(">>>   查询clientID[{}]对应的路由信息   <<<", clientID);
    return redisDAO.getSocketAddress(clientID);
  }
}
