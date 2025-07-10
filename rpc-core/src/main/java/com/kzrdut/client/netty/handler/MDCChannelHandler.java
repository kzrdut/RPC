package com.kzrdut.client.netty.handler;

import common.trace.TraceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class MDCChannelHandler extends ChannelOutboundHandlerAdapter {
    public static final AttributeKey<Map<String, String>>  TRACE_CONTEXT_KEY=AttributeKey.valueOf("TraceContext");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 从 Channel 属性中获取 Trace 上下文
        Map<String, String> traceContext
                = ctx.channel().attr(TRACE_CONTEXT_KEY).get();

        if (traceContext != null) {
            // 设置到当前线程的 TraceContext 或 MDC
            TraceContext.clone(traceContext);
            log.info("已绑定 Trace 上下文: {}", traceContext);
        } else {
            log.error("Trace 上下文未设置!");
        }

        // 继续传递请求
        super.write(ctx, msg, promise);
    }
}
