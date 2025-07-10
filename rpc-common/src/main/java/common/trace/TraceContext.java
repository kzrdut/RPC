package common.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class TraceContext {
    public static void setTraceId(String TraceId) {
        MDC.put("TraceId", TraceId);
    }

    public static String getTraceId() {
        return MDC.get("TraceId");
    }

    public static void setSpanId(String SpanId) {
        MDC.put("SpanId", SpanId);
    }

    public static String getSpanId() {
        return MDC.get("SpanId");
    }

    public static void setParentSpanId(String ParentSpanId) {
        MDC.put("ParentSpanId", ParentSpanId);
    }

    public static String getParentSpanId() {
        return MDC.get("ParentSpanId");
    }

    public static void setStartTimestamp(String StartTimestamp) {
        MDC.put("StartTimestamp", StartTimestamp);
    }

    public static String getStartTimestamp() {
        return MDC.get("StartTimestamp");
    }

    public static Map<String, String> getCopy() {
        return MDC.getCopyOfContextMap();
    }

    public static void clone(Map<String, String> context) {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
    }

    public static void clear() {
        MDC.clear();
    }
}
