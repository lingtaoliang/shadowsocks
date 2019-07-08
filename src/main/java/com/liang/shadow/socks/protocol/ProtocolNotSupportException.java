package com.liang.shadow.socks.protocol;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class ProtocolNotSupportException extends Exception {

    public ProtocolNotSupportException() {
        super();
    }

    public ProtocolNotSupportException(String message) {
        super(message);
    }

    public ProtocolNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolNotSupportException(Throwable cause) {
        super(cause);
    }

    protected ProtocolNotSupportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
