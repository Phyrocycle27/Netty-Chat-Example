import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        new Client("192.168.1.54", 3141).run();
    }

    private final String host;
    private final int port;

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void run() throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();

        SslContext sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        try {
            Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChatClientInitializer(sslCtx));

            Channel channel = bootstrap.connect(host, port).sync().channel();

            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            for (;;) {
                String line = in.readLine();

                if (line == null) break;

                lastWriteFuture = channel.writeAndFlush(line);


                if (line.toLowerCase().equals("bye")) {
                    channel.closeFuture().sync();
                    break;
                }
            }

            if (lastWriteFuture != null) lastWriteFuture.sync();
        }
        finally {
            group.shutdownGracefully();
        }
    }
}