package com.kzrdut.client.circuitbreaker;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public enum CircuitBreakerState {
    // 关闭, 开启, 半开启
    CLOSED, OPEN, HALF_OPEN
}
