package com.xu.client;

import com.alibaba.fastjson.JSONObject;
import com.xu.server.RequestFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {
    public static EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    public static Bootstrap getBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0, 4, 0, 4));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ClientHandler());
                        ch.pipeline().addLast(new LengthFieldPrepender(4, false));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                });
        return bootstrap;
    }


    public Object sendMsg(String msg, String path) {
        Object result = null;
        try {
            RequestFuture request = new RequestFuture(msg);
            request.setUrl(path);
            String str = JSONObject.toJSONString(request);
            ChannelFuture future = ChannelFutureManager.get();
            future.channel().writeAndFlush(str);
            result = request.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
