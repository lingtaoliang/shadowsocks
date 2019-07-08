package com.liang.shadow.socks.utils.netty;

/**
 * Created by lianglingtao on 2019/3/11.
 */
public class IdleChannelCloseException extends RuntimeException {

    public IdleChannelCloseException() {
    }

    public IdleChannelCloseException(String message) {
        super(message);
    }

    public IdleChannelCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdleChannelCloseException(Throwable cause) {
        super(cause);
    }

    public IdleChannelCloseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
