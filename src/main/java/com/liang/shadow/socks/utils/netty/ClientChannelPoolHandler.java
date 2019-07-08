package com.liang.shadow.socks.utils.netty;

import com.liang.shadow.socks.protocol.ClientChannelHandler;
import com.liang.shadow.socks.service.AccountManageService;
import com.liang.shadow.socks.service.ProxyClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;

/**
 * Created by lianglingtao on 2019/2/28.
 */
public class ClientChannelPoolHandler implements ChannelPoolHandler {

    private final ProxyClient proxyClient;
    private final int timeoutSeconds;

    public ClientChannelPoolHandler(AccountManageService accountManageService, int timeoutSeconds) {
        this.proxyClient = ProxyClient.instance(accountManageService);
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        proxyClient.getChannelIdToContext().remove(ch.id().asLongText());
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {

    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new IdleChannelHandler(timeoutSeconds),
                new ClientChannelHandler(proxyClient)
        );
    }
}
