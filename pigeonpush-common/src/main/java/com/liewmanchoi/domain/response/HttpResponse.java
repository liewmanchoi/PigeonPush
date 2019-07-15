package com.liewmanchoi.domain.response;

import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

/**
 * HTTP接口响应给请求端的消息体
 *
 * <p>1. 认证和负载均衡服务器 -> SDK<br>
 * 2. 推送服务notification -> 推送消息生产者
 *
 * @author wangsheng
 * @date 2019/7/9
 */
@Data
public class HttpResponse implements Serializable {
  private static final long serialVersionUID = 6320874985319179068L;

  public static final class CODE {
    public static final int OK = 200;
    public static final int FAILURE = 500;
  }

  public HttpResponse() {
    this.data = new HashMap<>();
  }

  /** 响应码 */
  private int code;
  /** 具体状态信息 */
  private HashMap<String, Object> data;
}
