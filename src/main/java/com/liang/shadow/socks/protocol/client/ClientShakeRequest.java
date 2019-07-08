package com.liang.shadow.socks.protocol.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class ClientShakeRequest {

    private Integer version;
    private byte[] methods;
    /**
     * CMD是SOCK的命令码
     * 0x01表示CONNECT请求
     * 0x02表示BIND请求
     * 0x03表示UDP转发
     */
    private Integer cmd;
    private String host;
    private byte hostType;
    private byte[] hostBytes;
    private Integer port;
    private ByteBuf reply;

    public Integer getVersion() {
        return version;
    }

    public ClientShakeRequest setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public byte[] getMethods() {
        return methods;
    }

    public ClientShakeRequest setMethods(byte[] methods) {
        this.methods = methods;
        return this;
    }

    public ClientShakeRequest setMethods(ByteBuf methods) {
        return setMethods(ByteBufUtil.getBytes(methods));
    }

    public Integer getCmd() {
        return cmd;
    }

    public ClientShakeRequest setCmd(Integer cmd) {
        this.cmd = cmd;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ClientShakeRequest setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ClientShakeRequest setPort(Integer port) {
        this.port = port;
        return this;
    }

    public byte[] getHostBytes() {
        return hostBytes;
    }

    public ClientShakeRequest setHostBytes(byte[] hostBytes) {
        this.hostBytes = hostBytes;
        return this;
    }

    public byte getHostType() {
        return hostType;
    }

    public ClientShakeRequest setHostType(byte hostType) {
        this.hostType = hostType;
        return this;
    }

    public ByteBuf getReply() {
        return reply;
    }

    public ClientShakeRequest setReply(ByteBuf reply) {
        this.reply = reply;
        return this;
    }
}
