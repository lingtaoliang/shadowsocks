package com.liang.shadow.socks.protocol;

import com.liang.shadow.socks.service.AccountManageService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.Cipher;

/**
 * Created by lianglingtao on 2019/2/26.
 */
public class DecryptHandler extends ChannelInboundHandlerAdapter {


    private AccountManageService accountManageService;
    private Cipher cipher;

    public DecryptHandler(AccountManageService accountManageService) {
        this.accountManageService = accountManageService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        if (cipher == null) {
            this.cipher = accountManageService.cipher(Cipher.DECRYPT_MODE);
        }
        byte[] cipherText = ByteBufUtil.getBytes(in);
        byte[] bytes = cipher.update(cipherText);
        ReferenceCountUtil.safeRelease(msg);
        super.channelRead(ctx, Unpooled.wrappedBuffer(bytes));
    }

}
