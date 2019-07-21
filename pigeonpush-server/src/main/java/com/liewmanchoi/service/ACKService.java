package com.liewmanchoi.service;

/**
 * @author wangsheng
 * @date 2019/7/21
 */
public interface ACKService {

  void handleACK(String clientID, int messageID);
}
