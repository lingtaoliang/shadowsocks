package com.liang.shadow.socks.service;

import com.liang.shadow.socks.cmd.ShadowClient;
import com.liang.shadow.socks.protocol.DecryptHandler;
import com.liang.shadow.socks.protocol.client.ClientConnectHandler;
import com.liang.shadow.socks.protocol.client.ClientRequestDecoder;
import com.liang.shadow.socks.protocol.server.ShadowClientRequestHandler;
import com.liang.shadow.socks.utils.netty.IdleChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class ShadowSocksService {

    private static final Logger LOG = LoggerFactory.getLogger(ShadowClient.class);
    public static final int MODE_CLIENT = 1;
    public static final int MODE_SERVER = 2;


    private ChannelFuture channelFuture;
    private AccountManageService accountManageService;
    private String remoteHost;
    private Integer remotePort;
    private Integer listenPort;
    private Integer mode;
    private Integer timeoutSeconds;

    ShadowSocksService() {
    }

    private void init() {
        Objects.requireNonNull(accountManageService, "Use Builder Please");
        Objects.requireNonNull(listenPort, "Use Builder Please");
        Objects.requireNonNull(mode, "Use Builder Please");
        Objects.requireNonNull(timeoutSeconds, "Use Builder Please");
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(mode == MODE_SERVER ? new ServerInitializer() : new ClientInitializer())
                .option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

        channelFuture = serverBootstrap.bind(listenPort);
        LOG.info(String.format("Start To Listen %d, Mode %s.", listenPort, mode == MODE_SERVER ? "Server" : "Client"));
    }

    public void start() {
        Objects.requireNonNull(channelFuture, "Use Builder Please");
        channelFuture.syncUninterruptibly();
    }

    class ClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(
                    new IdleChannelHandler(timeoutSeconds),
                    new ClientRequestDecoder(),
                    new ClientConnectHandler(accountManageService, remoteHost, remotePort)
            );
        }
    }

    class ServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(
                    new IdleChannelHandler(timeoutSeconds),
                    new DecryptHandler(accountManageService),
                    new ShadowClientRequestHandler(accountManageService)
            );
        }
    }

    public static class ShadowSocksServiceBuilder {
        private ShadowSocksService shadowSocksService = new ShadowSocksService();

        public ShadowSocksServiceBuilder listen(Integer port) {
            Objects.requireNonNull(port);
            shadowSocksService.listenPort = port;
            return this;
        }

        public ShadowSocksServiceBuilder clientMode() {
            shadowSocksService.mode = MODE_CLIENT;
            return this;
        }

        public ShadowSocksServiceBuilder serverMode() {
            shadowSocksService.mode = MODE_SERVER;
            return this;
        }

        public ShadowSocksServiceBuilder accountManageService(AccountManageService accountManageService) {
            Objects.requireNonNull(accountManageService);
            shadowSocksService.accountManageService = accountManageService;
            return this;
        }

        public ShadowSocksServiceBuilder serverHost(String serverHost) {
            Objects.requireNonNull(serverHost);
            shadowSocksService.remoteHost = serverHost;
            return this;
        }

        public ShadowSocksServiceBuilder serverPort(Integer serverPort) {
            Objects.requireNonNull(serverPort);
            shadowSocksService.remotePort = serverPort;
            return this;
        }

        public ShadowSocksServiceBuilder timeoutSeconds(Integer timeout) {
            Objects.requireNonNull(timeout);
            shadowSocksService.timeoutSeconds = timeout;
            return this;
        }

        public ShadowSocksService build() {
            shadowSocksService.init();
            return shadowSocksService;
        }

    }

}
