package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.ACKService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
@Service
public class ACKServiceImpl implements ACKService {

  @Autowired private RedisDAO redisDAO;

  @Override
  public void handleACK(String clientID, Integer messageID) {
    if (clientID == null || messageID == null) {
      return;
    }

    redisDAO.removeWaitACK(clientID, messageID);
  }
}
