package com.liewmanchoi;

import com.liewmanchoi.client.Client;
import com.liewmanchoi.domain.message.PushMessage;

/**
 * @author wangsheng
 * @date 2019/7/27
 */
public class SdkApp {
  public static void main(String[] args) {
    String url = "http://192.168.29.132";
    // 创建client
    Client client = new Client();
    // 设置回调函数
    client.addListener(
        (PushMessage message) -> {
          System.out.println(">>>>>>   收到新的推送消息   <<<<<<");
          System.out.println(">>>>>>   消息标题：[" + message.getTitle() + "]");
          System.out.println(">>>>>>   消息正文：[" + message.getText() + "]");
        });
    // 启动client
    client.run(url);
  }
}
