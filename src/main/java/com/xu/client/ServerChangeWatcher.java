package com.xu.client;

import com.xu.server.register.ZookeeperFactory;
import com.xu.server.register.ZookeeperRegister;
import io.netty.channel.ChannelFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;

public class ServerChangeWatcher  implements CuratorWatcher {
    public static ServerChangeWatcher watcher = null;
    public static final int SERVER_COUNT = 100;

    public static ServerChangeWatcher getInstance() {
        if (watcher == null) {
            watcher = new ServerChangeWatcher();
        }
        return watcher;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        if (event.getState().equals(Watcher.Event.KeeperState.Disconnected)
            || event.getState().equals(Watcher.Event.KeeperState.Expired)) {
            CuratorFramework client = ZookeeperFactory.recreate();
            client.getChildren().usingWatcher(this).forPath(ZookeeperRegister.SERVER_PATH);
        }else {
            CuratorFramework client = ZookeeperFactory.create();
            client.getChildren().usingWatcher(this).forPath(ZookeeperRegister.SERVER_PATH);
        }

        refreshFuture();
    }

    public static void refreshFuture(){
        CuratorFramework client = ZookeeperFactory.create();
        try {
            ChannelFutureManager.clear();
            List<String> servers = client.getChildren().forPath(ZookeeperRegister.SERVER_PATH);
            for (String server : servers) {
                String[] props = server.split("#");
                int weight = Integer.parseInt(props[2]);
                if (weight >= 0) {
                    for (int i = 0; i < weight * SERVER_COUNT; i++) {
                        ChannelFuture future = NettyClient.getBootstrap().connect(props[0], Integer.parseInt(props[1])).sync();
                        ChannelFutureManager.add(future);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            client.getChildren().usingWatcher(getInstance()).forPath(ZookeeperRegister.SERVER_PATH);
        } catch (Exception e) {

        }
    }
}
