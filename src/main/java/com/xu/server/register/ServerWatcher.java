package com.xu.server.register;

import com.xu.server.NettyServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetAddress;

/**
 * 监听ZooKeeper上的临时节点，防止Session中断导致服务注册的临时节点丢失。
 * 在Session中断后重新注册服务到ZooKeeper上
 */
public class ServerWatcher implements CuratorWatcher {
//    public static String serverKey = "";
    public static ServerWatcher serverWatcher = null;
    private ZookeeperRegister register = new ZookeeperRegister();

    public static ServerWatcher getInstance() {
        if (serverWatcher == null) {
            serverWatcher = new ServerWatcher();
        }
        return serverWatcher;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        System.out.println("服务监听 ZooKeeper event" + event.getState());
        if (event.getState().equals(Watcher.Event.KeeperState.Disconnected)
                || event.getState().equals(Watcher.Event.KeeperState.Expired)) {
            //先尝试关闭旧连接
            ZookeeperFactory.create().close();

            InetAddress netAddress = InetAddress.getLocalHost();

            register.register(netAddress.getHostAddress(), NettyServer.PORT);
        }else {
            CuratorFramework client = ZookeeperFactory.create();
            client.getChildren().usingWatcher(this).forPath(ZookeeperRegister.SERVER_PATH);
        }
    }
}
