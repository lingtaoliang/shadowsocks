import com.liang.shadow.socks.service.AccountManageService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.util.concurrent.TimeUnit;

/**
 * Created by lianglingtao on 2019/2/26.
 */
public class ClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientTest.class);

    @Test
    public void test() throws Exception {
        AccountManageService accountManageService = new AccountManageService("lianglingtao", "123");
        Cipher cipher = accountManageService.cipher(Cipher.ENCRYPT_MODE);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf in = (ByteBuf) msg;
                            byte[] bytes = ByteBufUtil.getBytes(in);
                            LOG.info("response {} ",Hex.encodeHex(bytes));
                            System.out.println(Hex.encodeHex(bytes));
                            ReferenceCountUtil.safeRelease(msg);
                        }

                    });
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("127.0.0.1", 1081).sync(); // (5)
            f.channel().writeAndFlush(Unpooled.wrappedBuffer(cipher.update("哇咔咔咔咔咔咔卡".getBytes())));
            TimeUnit.SECONDS.sleep(5);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
