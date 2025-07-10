package com.kzrdut.client.netty.handler;

import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 接收到 response , 给 channel 设计别名, 让 sendRequest 里读取 response
        AttributeKey<RpcResponse> RESPONSE_key = AttributeKey.valueOf("RpcResponse");
        // 将响应存入 Channel 属性
        channelHandlerContext.channel().attr(RESPONSE_key).set(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel exception occurred", cause);
        ctx.close();
    }
}
