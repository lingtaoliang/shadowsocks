package com.liang.shadow.socks.protocol.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lianglingtao on 2019/2/26.
 */
public class LogHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(LogHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        byte[] bytes = ByteBufUtil.getBytes(in);
        LOG.info("recv {}", Hex.encodeHex(bytes));
        LOG.info("recv {}", new String(bytes));
        super.channelRead(ctx, msg);
    }
}
