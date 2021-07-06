package com.xu.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestFuture future = JSONObject.parseObject(msg.toString(), RequestFuture.class);
        Response response = Mediator.process(future);
        ctx.channel().writeAndFlush(JSON.toJSONString(response));
    }
}
