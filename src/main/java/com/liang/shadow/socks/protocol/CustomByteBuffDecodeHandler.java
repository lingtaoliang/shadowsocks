package com.liang.shadow.socks.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;

/**
 * Created by lianglingtao on 2019/3/1.
 */
public abstract class CustomByteBuffDecodeHandler extends ChannelInboundHandlerAdapter {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    protected static final int DECODE_SUCCESS = 0;
    protected static final int DECODE_WAIT_NEXT = 1;
    protected static final int SUCCESS_BUT_RELEASE_BUFF_BY_SELF = 2;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int code = -1;
        ByteBuf toClient = null;
        ByteBuf in = (ByteBuf) msg;
        try {
            boolean hasAppendBefore = false;
            if (baos.size() > 0) {
                hasAppendBefore = true;
                baos.write(ByteBufUtil.getBytes(in));
            }
            toClient = hasAppendBefore ? Unpooled.wrappedBuffer(baos.toByteArray()) : null;
            code = decode(ctx, toClient != null ? toClient : in);
        } finally {
            if (code == SUCCESS_BUT_RELEASE_BUFF_BY_SELF) {
                if (toClient != null) { // 此时，子类要求自己释放的其实是Wrap过的，而输入需要这边释放
                    ReferenceCountUtil.safeRelease(msg);
                }
            } else {
                if (toClient != null) {
                    ReferenceCountUtil.safeRelease(toClient);
                }
                ReferenceCountUtil.safeRelease(msg);
                if (code == DECODE_SUCCESS && baos.size() > 0) {
                    baos = new ByteArrayOutputStream();
                }
                if (code == DECODE_WAIT_NEXT) {
                    baos.write(ByteBufUtil.getBytes(in));
                }
            }
        }
    }

    protected abstract int decode(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;


}
