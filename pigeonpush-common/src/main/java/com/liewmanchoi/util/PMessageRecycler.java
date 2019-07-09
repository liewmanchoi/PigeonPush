package com.liewmanchoi.util;

import com.liewmanchoi.domain.message.PMessage;
import io.netty.util.Recycler;

/**
 * @author wangsheng
 * @date 2019/7/9
 */
public class PMessageRecycler {
  private static Recycler<PMessage> RECYCLER =
      new Recycler<PMessage>() {
        @Override
        protected PMessage newObject(Handle<PMessage> handle) {
          return new PMessage(handle);
        }
      };

  public static PMessage reuse() {
    return RECYCLER.get();
  }
}
