package com.liang.shadow.socks.service;

import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.utils.netty.ChannelPoolInfo;
import com.liang.shadow.socks.utils.netty.CustomChannelPoolMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lianglingtao on 2019/2/25.
 */
public class ProxyClient {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyClient.class);
    private static volatile ProxyClient proxyClient;

    private Map<String, ChannelPoolInfo> channelIdToContext = new ConcurrentHashMap<>();
    private CustomChannelPoolMap poolMap;
    private AccountManageService accountManageService;

    ProxyClient(AccountManageService accountManageService) {
        this.accountManageService = accountManageService;
        this.init();
    }

    private void init() {
        ProxyClient c = this;
        this.poolMap = new CustomChannelPoolMap(accountManageService, c);
    }

    private Channel connect(ChannelPoolInfo channelPoolInfo) throws InterruptedException, ExecutionException, TimeoutException {
        CustomChannelPoolMap.PoolInfo poolInfo = new CustomChannelPoolMap.PoolInfo();
        poolInfo.host = channelPoolInfo.getHost();
        poolInfo.port = channelPoolInfo.getPort();
        poolInfo.mode = channelPoolInfo.getMode();
        Channel channel = poolMap.get(poolInfo).acquire().get(LocalConf.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        channelIdToContext.put(channel.id().asLongText(), channelPoolInfo);
        return channel;
    }

    public Channel getChannel(String host, int port, ChannelHandlerContext ctx, int cipherMode) throws InterruptedException, ExecutionException, TimeoutException {
        ChannelPoolInfo poolInfo = new ChannelPoolInfo();
        poolInfo.setHost(host).setPort(port);
        poolInfo.setCtx(ctx).setMode(cipherMode);
        return connect(poolInfo);
    }


    public Map<String, ChannelPoolInfo> getChannelIdToContext() {
        return channelIdToContext;
    }

    public AccountManageService getAccountManageService() {
        return accountManageService;
    }

    public static ProxyClient instance(AccountManageService accountManageService) {
        if (proxyClient == null) {
            synchronized (ProxyClient.class) {
                if (proxyClient == null) {
                    proxyClient = new ProxyClient(accountManageService);
                }
            }
        }
        return proxyClient;
    }

}
