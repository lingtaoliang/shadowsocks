package com.liang.shadow.socks.utils.netty;

import io.netty.buffer.ByteBuf;

/**
 * Created by lianglingtao on 2019/3/1.
 */
public class CustomBuffUtils {

    public static byte[] readBytes(ByteBuf byteBuf, int length) {
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
