package com.kzrdut.trace.interceptor;

import com.kzrdut.trace.TraceIdGenerator;
import com.kzrdut.trace.ZipkinReporter;
import common.trace.TraceContext;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ServerTraceInterceptor {
    public static void beforeHandle() {
        String traceId = TraceContext.getTraceId();
        String spanId = TraceIdGenerator.generateSpanId();
        String parentSpanId = TraceContext.getParentSpanId();

        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId(parentSpanId);

        // 记录服务端 Span
        long startTimestamp = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTimestamp));
    }

    public static void afterHandle(String serviceName) {
        long startTimestamp = Long.parseLong(TraceContext.getStartTimestamp());
        long endTimestamp = System.currentTimeMillis();
        long duration = endTimestamp - startTimestamp;

        // 上报服务端 Span
        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "server-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "server"
        );

        // 清理 TraceContext
        TraceContext.clear();
    }
}
