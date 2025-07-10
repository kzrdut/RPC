package com.kzrdut.trace.interceptor;

import com.kzrdut.trace.TraceIdGenerator;
import com.kzrdut.trace.ZipkinReporter;
import common.trace.TraceContext;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ClientTraceInterceptor {
    public static void beforeInvoke() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) {
            traceId = TraceIdGenerator.generateTraceId();
            TraceContext.setTraceId(traceId);
        }
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);

        long startTimestamp = System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTimestamp));
    }

    public static void afterInvoke(String serviceName) {
        long startTimestamp = Long.parseLong(TraceContext.getStartTimestamp());
        long endTimestamp = System.currentTimeMillis();
        long duration = endTimestamp - startTimestamp;

        // 上报客户端 Span
        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "client-" + serviceName,
                startTimestamp,
                duration,
                serviceName,
                "client"
        );

        // 清理 TraceContext
        TraceContext.clear();
    }
}