package com.liang.shadow.socks.protocol.client;

import com.liang.shadow.socks.cmd.ShadowClient;
import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.service.AccountManageService;
import com.liang.shadow.socks.service.ProxyClient;
import com.liang.shadow.socks.utils.netty.BuffReleaseListener;
import com.liang.shadow.socks.utils.netty.IdleChannelCloseException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ShadowClient.class);
    private Channel channel;
    private Cipher encryptCipher;
    private AccountManageService accountManageService;
    private ProxyClient proxyClient;
    private String remoteHost;
    private int remotePort;

    public ClientConnectHandler(AccountManageService accountManageService, String remoteHost, int remotePort) {
        this.accountManageService = accountManageService;
        this.proxyClient = ProxyClient.instance(accountManageService);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (encryptCipher == null) {
            this.encryptCipher = accountManageService.cipher(Cipher.ENCRYPT_MODE);
        }
        // 握手阶段
        if (msg instanceof ClientShakeRequest) {
            ByteBuf reply = ((ClientShakeRequest) msg).getReply();
            if (reply != null) { // 之前协议解析器需要给客户端响应，那么进行响应，注意，这里需要flush，以规避少部分客户端数据收不到的问题
                ctx.writeAndFlush(reply).addListener(new BuffReleaseListener(reply));
            }
            if (StringUtils.isNotEmpty(((ClientShakeRequest) msg).getHost())) { // 已经解析出透传的host地址，那么给ss服务端建立链接，并告知
                this.channel = proxyClient.getChannel(remoteHost, remotePort, ctx, Cipher.DECRYPT_MODE);
                // 协议统一改为hostType，hostLength，host；以便ss服务端解析
                ByteBuffer localBuffer = ByteBuffer.allocate(2 + ((ClientShakeRequest) msg).getHostBytes().length + Short.BYTES);
                localBuffer.put(((ClientShakeRequest) msg).getHostType());
                localBuffer.put((byte) ((ClientShakeRequest) msg).getHostBytes().length);
                localBuffer.put(((ClientShakeRequest) msg).getHostBytes());
                localBuffer.putShort(((ClientShakeRequest) msg).getPort().shortValue());
                byte[] bytes = encryptCipher.update(localBuffer.array());
                ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
                this.channel.write(byteBuf).addListener(new BuffReleaseListener(byteBuf));
            }
            return;
        }
        // 透传阶段
        ByteBuf in = (ByteBuf) msg;
        byte[] bytes = encryptCipher.update(ByteBufUtil.getBytes(in));
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        this.channel.write(byteBuf).addListener(new BuffReleaseListener(msg, byteBuf));
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
        if (this.channel != null) {
            channel.close();
            encryptCipher = null;
            this.channel = null;
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        if (cause instanceof IOException) {
            if (LocalConf.verbose) {
                LOG.error("server reject connection.", cause);
            } else {
                LOG.warn("server reject connection");
            }
        } else if (cause instanceof IdleChannelCloseException) {
            LOG.debug("idle channel closed");
        } else {
            LOG.error("to server make an error.", cause);
        }
        if (this.channel != null) {
            channel.close();
            encryptCipher = null;
            this.channel = null;
        }
        ctx.close();
    }

}
