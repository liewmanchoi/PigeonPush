package com.liewmanchoi.service.api;

import com.liewmanchoi.domain.message.PushMessage;
import com.liewmanchoi.entity.RequestEntity;
import java.util.List;

/**
 * @author wangsheng
 * @date 2019/7/23
 */
public interface MessageConverter {
  List<PushMessage> convert(RequestEntity entity);
}
