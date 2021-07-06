package client;

import com.xu.client.NettyClient;
import com.xu.server.RequestFuture;
import org.junit.Test;

public class RPCTest {


    @Test
    public void test() {
        NettyClient rpcClient = new NettyClient();
        for (int i = 0; i < 10; i++) {
            Object result = rpcClient.sendMsg("hello" + i, "print");
            System.out.println(result);
        }
    }
}
