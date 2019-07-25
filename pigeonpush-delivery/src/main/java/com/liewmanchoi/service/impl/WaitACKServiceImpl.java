package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.api.WaitACKService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangsheng
 * @date 2019/7/25
 */
@Slf4j
@Service
public class WaitACKServiceImpl implements WaitACKService {
  @Autowired
  private RedisDAO redisDAO;

  @Override
  public void addWaitACK(String clientID, Long messageID) {
    if (clientID == null || messageID == null){
      log.warn(">>>   传入的参数有误   <<<");
      return;
    }

    redisDAO.addWaitACK(clientID, messageID);
    log.info(">>>   向clientID[{}]增加待确认消息ID[{}]   <<<", clientID, messageID);
  }
}
