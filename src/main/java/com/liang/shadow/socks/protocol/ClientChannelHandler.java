package com.liang.shadow.socks.protocol;

import com.liang.shadow.socks.cmd.ShadowClient;
import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.service.ProxyClient;
import com.liang.shadow.socks.utils.netty.BuffReleaseListener;
import com.liang.shadow.socks.utils.netty.ChannelPoolInfo;
import com.liang.shadow.socks.utils.netty.IdleChannelCloseException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ShadowClient.class);

    private Cipher decryptCipher; // ss客户端请求ss服务端需要加密请求，解密下载；ss服务端下载后需要加密响应，因此给个扩展点，直接在client代理这个事
    private ChannelPoolInfo channelPoolInfo;
    private ProxyClient proxyClient;

    public ClientChannelHandler(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        if (channelPoolInfo == null) {
            this.channelPoolInfo = proxyClient.getChannelIdToContext().get(ctx.channel().id().asLongText());
            this.decryptCipher = proxyClient.getAccountManageService().cipher(this.channelPoolInfo.getMode() == Cipher.DECRYPT_MODE ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE);
        }
        if (channelPoolInfo.getMode() > 0) {
            byte[] fromBytes = ByteBufUtil.getBytes(in);
            byte[] bytes = decryptCipher.update(fromBytes);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            this.channelPoolInfo.getCtx().write(byteBuf).addListener(new BuffReleaseListener(byteBuf, msg));
        } else {
            this.channelPoolInfo.getCtx().write(in).addListener(new BuffReleaseListener(msg));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        if (this.channelPoolInfo != null) {
            this.channelPoolInfo.getCtx().flush();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (channelPoolInfo != null) {
            channelPoolInfo.getCtx().close();
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        if (cause instanceof IdleChannelCloseException) {
            LOG.warn("idle channel closed");
        } else if (cause instanceof IOException) {
            if (LocalConf.verbose) {
                LOG.error("to remote make an error", cause);
            } else {
                LOG.debug("to remote make an error");
            }
        } else {
            // Close the connection when an exception is raised.
            LOG.error("client error", cause);
        }
        if (channelPoolInfo != null) {
            channelPoolInfo.getCtx().close();
        }
        ctx.close();
    }

}