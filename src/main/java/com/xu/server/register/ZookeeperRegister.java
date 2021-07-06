package com.xu.server.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;

public class ZookeeperRegister {
    public static final String SERVER_PATH = "/netty";

    public void register(String ip, int port) {
        try {
            //连接ZooKeeper
            CuratorFramework client = ZookeeperFactory.recreate();
            //检查服务路径是否存在
            Stat stat = client.checkExists().forPath(SERVER_PATH);
            if (stat == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(SERVER_PATH, "0".getBytes());
            }
            int weight = 1;
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(SERVER_PATH + "/" + ip + "#" + port + "#" + weight + "#");
        } catch (Exception e) {

        }
    }
}
