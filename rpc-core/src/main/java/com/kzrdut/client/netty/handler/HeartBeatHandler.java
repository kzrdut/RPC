package com.kzrdut.client.netty.handler;

import common.message.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            IdleState idleState = idleStateEvent.state();

            if (idleState == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(RpcRequest.heartBeat());
                log.info("超过 10 秒没有写数据, 发送心跳包");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
