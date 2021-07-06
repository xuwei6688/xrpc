package com.xu.server;

import com.xu.server.register.ServerWatcher;
import com.xu.server.register.ZookeeperFactory;
import com.xu.server.register.ZookeeperRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetAddress;

public class NettyServer {
    public static final Integer PORT = 8081;
    private static ZookeeperRegister register = new ZookeeperRegister();

    public static void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ServerChannelInitializer());

            ChannelFuture future = bootstrap.bind(PORT).sync();

            //注册服务到注册中心
            InetAddress netAddress = InetAddress.getLocalHost();
            register.register(netAddress.getHostAddress(), PORT);
            //注册一个Watcher，在服务节点发生
            CuratorFramework client = ZookeeperFactory.create();
            client.getChildren().usingWatcher(ServerWatcher.getInstance()).forPath(ZookeeperRegister.SERVER_PATH);

            future.channel().closeFuture().sync();



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
