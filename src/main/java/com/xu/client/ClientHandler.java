package com.xu.client;

import com.alibaba.fastjson.JSONObject;
import com.xu.server.RequestFuture;
import com.xu.server.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = JSONObject.parseObject(msg.toString(), Response.class);
        System.out.println(ctx.channel().id());
        RequestFuture.received(response);
    }

}
