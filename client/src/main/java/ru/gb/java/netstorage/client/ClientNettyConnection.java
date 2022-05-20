package ru.gb.java.netstorage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ClientNettyConnection {
    private static final ClientNettyConnection INSTANCE;

    static {
        try {
            INSTANCE = new ClientNettyConnection();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int PORT = 45001;
    private static final String ADDRESS = "localhost";
    public static final int MB_20 = 20 * 1_000_000;
    private ChannelFuture channelFuture;

    private ClientNettyConnection() throws InterruptedException {
        Thread t = new Thread(() -> {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.remoteAddress(ADDRESS, PORT);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(
                                new ObjectDecoder(MB_20, ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler()
                        );
                    }
                });

                channelFuture = bootstrap.connect().sync();
                channelFuture.channel().closeFuture().sync();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                eventLoopGroup.shutdownGracefully();
            }
        });
        t.start();
    }

    public void close() {
        channelFuture.channel().close();
    }
    public void sendMessage(Object msg) {
        channelFuture.channel().writeAndFlush(msg);
    }
    public static ClientNettyConnection getInstance () {
        return INSTANCE;
    }


}
