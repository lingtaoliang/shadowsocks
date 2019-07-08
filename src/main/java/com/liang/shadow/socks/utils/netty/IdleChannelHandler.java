package com.liang.shadow.socks.utils.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by lianglingtao on 2019/3/11.
 */
public class IdleChannelHandler extends IdleStateHandler {

    private boolean closed = false;

    public IdleChannelHandler(int allIdleTimeSeconds) {
        super(0, 0, allIdleTimeSeconds);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        assert evt.state().equals(IdleState.ALL_IDLE);
        idleChannelClose(ctx);
    }

    protected void idleChannelClose(ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
            ctx.fireExceptionCaught(new IdleChannelCloseException());
            ctx.close();
            closed = true;
        }
    }
}
