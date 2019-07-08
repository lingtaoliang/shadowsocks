package com.liang.shadow.socks.protocol.server;

import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.protocol.CustomByteBuffDecodeHandler;
import com.liang.shadow.socks.service.AccountManageService;
import com.liang.shadow.socks.service.ProxyClient;
import com.liang.shadow.socks.utils.netty.BuffReleaseListener;
import com.liang.shadow.socks.utils.netty.CustomBuffUtils;
import com.liang.shadow.socks.utils.netty.IdleChannelCloseException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.net.InetAddress;

/**
 * 处理自己的Client过来的请求
 * Created by lianglingtao on 2019/2/25.
 */
public class ShadowClientRequestHandler extends CustomByteBuffDecodeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ShadowClientRequestHandler.class);
    private Channel channel;
    private ProxyClient proxyClient;

    public ShadowClientRequestHandler(AccountManageService accountManageService) {
        this.proxyClient = ProxyClient.instance(accountManageService);
    }

    @Override
    public int decode(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (channel == null) {
            byte type = msg.readByte();
            byte length = msg.readByte();
            if (msg.readableBytes() < length + 2) {
                return DECODE_WAIT_NEXT;
            }
            String host = type == 0x03 ? new String(CustomBuffUtils.readBytes(msg, length)) : InetAddress.getByAddress(CustomBuffUtils.readBytes(msg, length)).toString();
            if (type != 0x03 && host.startsWith("/")) {
                host = host.substring(1);
            }
            short port = msg.readShort();
            channel = proxyClient.getChannel(host, port, ctx, Cipher.ENCRYPT_MODE);
            byte[] bytes = ByteBufUtil.getBytes(msg);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            channel.write(byteBuf).addListener(new BuffReleaseListener(byteBuf));
            return DECODE_SUCCESS;
        }
        channel.write(msg).addListener(new BuffReleaseListener(msg));
        return SUCCESS_BUT_RELEASE_BUFF_BY_SELF;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        if (channel != null) {
            channel.flush();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        if (cause instanceof IOException) {
            if (LocalConf.verbose) {
                LOG.error("remote reject connection.", cause);
            } else {
                LOG.warn("remote reject connection");
            }
        } else if (cause instanceof IdleChannelCloseException) {
            LOG.debug("idle channel closed");
        } else {
            LOG.error("to client make an error.", cause);
        }
        if (channel != null) {
            channel.close();
            channel = null;
        }
        ctx.close();
    }

}
