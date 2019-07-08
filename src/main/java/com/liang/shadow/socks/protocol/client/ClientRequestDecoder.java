package com.liang.shadow.socks.protocol.client;

import com.liang.shadow.socks.protocol.CustomByteBuffDecodeHandler;
import com.liang.shadow.socks.protocol.ProtocolNotSupportException;
import com.liang.shadow.socks.utils.netty.CustomBuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;

/**
 * SOCKS5 https://zh.wikipedia.org/wiki/SOCKS
 * Created by lianglingtao on 2019/2/25.
 */
public class ClientRequestDecoder extends CustomByteBuffDecodeHandler {

    private ClientStatus nextStatus = ClientStatus.START;
    private ClientShakeRequest shakeRequest = new ClientShakeRequest();
    private static final byte[] REQUEST_ACCEPT = new byte[]{
            0x05, // socket5
            0x00, // success
            0x00, // rcv
            0x01, // ipv4
            0x00, 0x00, 0x00, 0x00, // ipV4 address
            0x08, 0x43 // port
    };


    protected int decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int readableBytes = in.readableBytes();
        switch (nextStatus) {
            case START: {
                if (readableBytes < 2) {
                    return DECODE_WAIT_NEXT;
                }
                // version
                shakeRequest.setVersion((int) in.readByte());
                // method length
                int methodLength = (int) in.readByte();
                if (shakeRequest.getVersion() != 5) {
                    throw new ProtocolNotSupportException("Version Not 5");
                }
                if (readableBytes < methodLength) {
                    return DECODE_WAIT_NEXT;
                }
                // methods
                shakeRequest.setMethods(CustomBuffUtils.readBytes(in, methodLength));
                nextStatus = ClientStatus.ADDRESS_GET;
                shakeRequest.setReply(Unpooled.wrappedBuffer(new byte[]{
                        0x05, // socket 5
                        0x00,// no need auth
                }));
                ctx.fireChannelRead(shakeRequest);
                return DECODE_SUCCESS;
            }
            case ADDRESS_GET: {
                if (readableBytes < 4) {
                    return DECODE_WAIT_NEXT;
                }
                // version
                if (shakeRequest.getVersion() != in.readByte()) {
                    throw new ProtocolNotSupportException("Bad Version");
                }
                // command
                shakeRequest.setCmd((int) in.readByte());
                // rsv 保留字段
                in.readByte();
                byte addressType = in.readByte();
                readableBytes = in.readableBytes();
                shakeRequest.setHostType(addressType);
                String host;
                byte[] hostBytes;
                if (addressType == 0x01) { // ipV4
                    if (readableBytes < 4) {
                        return DECODE_WAIT_NEXT;
                    }
                    hostBytes = CustomBuffUtils.readBytes(in, 4);
                    InetAddress address = InetAddress.getByAddress(hostBytes);
                    host = address.toString();
                } else if (addressType == 0x03) { // 域名
                    int methodLength = in.readByte();
                    readableBytes = in.readableBytes();
                    if (readableBytes < methodLength) {
                        return DECODE_WAIT_NEXT;
                    }
                    int hostLength = in.readByte();

                    hostBytes = CustomBuffUtils.readBytes(in, hostLength);
                    host = new String(hostBytes);
                } else if (addressType == 0x04) { // ipV6
                    if (readableBytes < 16) {
                        return DECODE_WAIT_NEXT;
                    }
                    hostBytes = CustomBuffUtils.readBytes(in, 16);
                    InetAddress address = InetAddress.getByAddress(hostBytes);
                    host = address.toString();
                } else {
                    throw new ProtocolNotSupportException("Bad AddressType " + addressType);
                }
                shakeRequest.setHost(host).setHostBytes(hostBytes);
                readableBytes = in.readableBytes();
                if (readableBytes < 2) {
                    return DECODE_WAIT_NEXT;
                }
                int port = in.readShort();
                shakeRequest.setPort(port);
                nextStatus = ClientStatus.PROXY;
                shakeRequest.setReply(hostResponse());
                ctx.fireChannelRead(shakeRequest);
                return DECODE_SUCCESS;
            }
            default: {
                ctx.fireChannelRead(in);
                return SUCCESS_BUT_RELEASE_BUFF_BY_SELF;
            }
        }


    }

    private ByteBuf hostResponse() {
        return Unpooled.wrappedBuffer(REQUEST_ACCEPT);
    }


    public enum ClientStatus {
        START,
        ADDRESS_GET,
        PROXY,
    }

}
