package com.liewmanchoi.service.impl;

import com.liewmanchoi.dao.RedisDAO;
import com.liewmanchoi.service.api.WaitACKService;
import java.util.HashSet;
import java.util.Set;
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
  @Autowired private RedisDAO redisDAO;

  @Override
  public void addWaitACK(String clientID, Long messageID) {
    if (clientID == null || messageID == null) {
      log.warn(">>>   传入的参数有误   <<<");
      return;
    }

    redisDAO.addWaitACK(clientID, messageID);
    log.info(">>>   向clientID[{}]增加待确认消息ID[{}]   <<<", clientID, messageID);
  }

  @Override
  public Set<Long> getMessageIds(String clientID) {
    if (clientID == null) {
      log.warn(">>>   传入的参数有误   <<<");
      return null;
    }

    Set<Object> messageIds = redisDAO.getWaitACKMessages(clientID);
    if (messageIds == null || messageIds.isEmpty()) {
      log.warn(">>>   没有查询到[{}]对应的待确认消息列表   <<<", clientID);
      return null;
    }

    // 强制类型转换
    // TODO: 更优雅的实现方式？
    Set<Long> result = new HashSet<>();
    for (Object id : messageIds) {
      result.add((Long) id);
    }

    return result;
  }
}
