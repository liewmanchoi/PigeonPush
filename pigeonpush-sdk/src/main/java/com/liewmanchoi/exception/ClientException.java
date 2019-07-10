package com.liewmanchoi.exception;

import lombok.Getter;

/**
 * @author wangsheng
 * @date 2019/7/8
 */
public class ClientException extends RuntimeException {

  @Getter
  private ErrorEnum errorEnum;

  public ClientException(ErrorEnum errorEnum, String message) {
    super(message);
    this.errorEnum = errorEnum;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + errorEnum.getReason();
  }

  public enum ErrorEnum {
    /**
     * 无法获取DEVICE_ID
     */
    DEVICE_ID_FAILURE("无法获取DEVICE_ID"),
    /**
     * 无法找到配置文件
     */
    CONFIGURE_FILE_NOT_FOUNT("无法找到配置文件");

    @Getter
    private String reason;

    ErrorEnum(String reason) {
      this.reason = reason;
    }
  }
}
