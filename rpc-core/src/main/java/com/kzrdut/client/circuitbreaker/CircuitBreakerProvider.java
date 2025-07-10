package com.kzrdut.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class CircuitBreakerProvider {
    // 使用线程安全的 ConcurrentHashMap
    private Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    // 根据服务名获取对应的熔断器实例
    public CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakerMap.computeIfAbsent(serviceName, key -> {
            log.info("服务 {} 不存在熔断器, 创建新的熔断器实例", serviceName);
            // 创建并返回新熔断器
            return new CircuitBreaker(1, 0.5, 10000);
        });
    }
}
