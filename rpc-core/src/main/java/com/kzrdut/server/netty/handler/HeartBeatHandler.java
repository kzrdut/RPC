package com.kzrdut.server.netty.handler;

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
        try {
            // 处理 IdleState.READER_IDLE 时间
            if (evt instanceof IdleStateEvent idleStateEvent) {
                IdleState idleState = idleStateEvent.state();

                if (idleState == IdleState.READER_IDLE) {
                    log.info("超过 20 秒没有收到客户端心跳, channel : {}", ctx.channel());
                    // 关闭 channel, 避免造成更多资源占用
                    ctx.close();
                } else if (idleState == IdleState.WRITER_IDLE) {
                    log.info("超过 20s 没有写数据, channel : {}", ctx.channel());
                    // 关闭 channel, 避免造成更多资源占用
                    ctx.close();
                }
            }
        } catch (Exception e) {
            log.error("处理事件发生异常", e);
        }
    }
}
