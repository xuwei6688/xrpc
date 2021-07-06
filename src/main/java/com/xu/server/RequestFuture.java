package com.xu.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>RequestFuture 是客户端请求对象。</p>
 *
 * NettyClient 发起请求时会构建一个 RequestFuture 封装请求消息，使用 JSON 序列化后发送给服务端。
 * 然后调用 RequestFuture 的 get 方法等待结果。
 * 服务端接收到消息处理后会返回一个 {@link Response}，Response 里的 id 和 RequestFuture 是相同的。
 * 客户端收到 Response 后根据 id 可以从 futureMap 找到对应的 RequestFuture，然后调用 received 方法将结果设置到 result 上，
 * 最后唤醒等待的线程。
 */
public class RequestFuture {
    private static Map<Long, RequestFuture> futureMap = new HashMap<>();
    private static AtomicInteger idGenerator = new AtomicInteger();
    private static final Long WAIT_TIME = 5000L;
    private  long id;
    /**
     * 请求服务对应url
     */
    private Object url;
    /**
     * 请求消息
     */
    private Object msg;
    /**
     * 响应结果
     */
    private Object result;

    public RequestFuture() {
    }

    public RequestFuture(Object msg) {
        this.msg = msg;
        this.id = idGenerator.incrementAndGet();
        futureMap.put(id, this);
    }

    public Object get() {
        synchronized (this) {
            while (result == null) {
                try {
                    wait(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static void received(Response response) {
        if (response == null) {
            return;
        }
        RequestFuture future = futureMap.get(response.getId());
        future.setResult(response.getResult());
        synchronized (future) {
            future.notify();
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Object getUrl() {
        return url;
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
