import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChatClientHandler extends ChannelInboundHandlerAdapter {

    private final Encryption enc = new Encryption();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Active");
        ctx.channel().writeAndFlush(enc.getPublicKey());
        System.out.println("Channel id is: " + ctx.channel().localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (!enc.isKeySet()) {
            enc.createSharedKey((byte[]) msg);
            System.out.println("Encryption AES key is: " + enc.isKeySet());

            ch.pipeline().addAfter("frameEncoder", "cipherDecoder", new CipherDecoder(enc));
            ch.pipeline().addAfter("cipherDecoder", "cipherEncoder", new CipherEncoder(enc));
            ch.pipeline().remove("bytesDecoder");
            ch.pipeline().remove("bytesEncoder");
        } else {
            System.out.println(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}