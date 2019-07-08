package com.liang.shadow.socks.utils.netty;

import io.netty.channel.ChannelHandlerContext;

public class ChannelPoolInfo {
    private String host;
    private int port;
    private ChannelHandlerContext ctx;
    private int mode;


    public String getHost() {
        return host;
    }

    public ChannelPoolInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ChannelPoolInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ChannelPoolInfo setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        return this;
    }

    public int getMode() {
        return mode;
    }

    public ChannelPoolInfo setMode(int mode) {
        this.mode = mode;
        return this;
    }
}