package com.xu.client;

import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  有了注册中心，可能一个服务有多个节点，多个节点可能还需要有权重。
 *  于是实现了一个简单的根据权重负载均衡，这里可以配置 weight，针对每个节点根据权重创建一定数量的 Channel。
 *  这些Channel都维护在ChannelFutureManager里，当客户端发起调用时，就从这些channel中遍历，
 *  选出一个channel，当遍历到末尾时下次再从头遍历。
 */
public class ChannelFutureManager {
    private static final AtomicInteger position = new AtomicInteger(0);
    private static final List<ChannelFuture> channelFutures = new CopyOnWriteArrayList<>();

    private ChannelFutureManager() {}

    public static ChannelFuture get() {
        ChannelFuture channelFuture = get(position);
        if (channelFuture == null) {
            ServerChangeWatcher.refreshFuture();
        }
        return get(position);
    }

    public static ChannelFuture get(AtomicInteger position) {
        int size = channelFutures.size();
        if (size == 0) {
            return null;
        }
        ChannelFuture channelFuture = null;
        synchronized (position) {
            if (position.get() == size) {
                channelFuture = channelFutures.get(0);
                position.set(0);
            }else {
                channelFuture = channelFutures.get(position.getAndIncrement());
            }

            if (!channelFuture.channel().isActive()) {
                channelFutures.remove(channelFuture);
                channelFuture = get(position);
            }
        }
        return channelFuture;
    }

    public static void removeChannel(ChannelFuture channelFuture) {
        channelFutures.remove(channelFuture);
    }

    public static void add(ChannelFuture channelFuture) {
        channelFutures.add(channelFuture);
    }

    public static void clear() {
        channelFutures.forEach(e->{
            e.channel().close();
        });
        channelFutures.clear();
    }
}
