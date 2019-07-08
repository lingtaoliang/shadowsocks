package com.liang.shadow.socks.utils.netty;

import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.service.AccountManageService;
import com.liang.shadow.socks.service.ProxyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by lianglingtao on 2019/2/28.
 */
public class CustomChannelPoolMap extends AbstractChannelPoolMap<CustomChannelPoolMap.PoolInfo, SimpleChannelPool> {

    private AccountManageService accountManageService;

    public CustomChannelPoolMap(AccountManageService accountManageService, ProxyClient proxyClient) {
        this.accountManageService = accountManageService;
    }

    @Override
    protected SimpleChannelPool newPool(PoolInfo key) {
        final Bootstrap b = new Bootstrap();
        SocketAddress socketAddress = InetSocketAddress.createUnresolved(key.host, key.port);
        b.group(new NioEventLoopGroup())
                .remoteAddress(socketAddress)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        b.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, LocalConf.CONNECT_TIMEOUT_SECONDS * 1000);
        return new SimpleChannelPool(b, new ClientChannelPoolHandler(accountManageService, (int) TimeUnit.MINUTES.toSeconds(30)));
    }

    public static class PoolInfo {
        public String host;
        public int port;
        public int mode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PoolInfo poolInfo = (PoolInfo) o;

            if (port != poolInfo.port) return false;
            if (mode != poolInfo.mode) return false;
            return host.equals(poolInfo.host);

        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + port;
            result = 31 * result + mode;
            return result;
        }
    }
}
