package com.liang.shadow.socks.utils.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lianglingtao on 2019/2/28.
 */
public class BuffReleaseListener implements ChannelFutureListener {

    private static final Logger LOG = LoggerFactory.getLogger(BuffReleaseListener.class);
    private Object[] byteBuf;

    public BuffReleaseListener(Object... byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (byteBuf != null) {
            for (Object o : byteBuf) {
                if (o != null) {
                    try {
                        ReferenceCountUtil.release(o);
                    } catch (Throwable t) {
                        LOG.debug("ignore is ok", t);
                    }
                }
            }
        }
    }
}
