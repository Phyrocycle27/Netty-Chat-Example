import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public ChatClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(sslCtx.newHandler(ch.alloc(), "192.168.1.54", 3141));
         pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
           1048576, 0, 4, 0, 4
        ));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("bytesDecoder", new ByteArrayDecoder());
        pipeline.addLast("bytesEncoder", new ByteArrayEncoder());
        pipeline.addLast("chat", new ChatClientHandler());
    }
}