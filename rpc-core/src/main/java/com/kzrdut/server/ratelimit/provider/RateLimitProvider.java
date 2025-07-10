package com.kzrdut.server.ratelimit.provider;

import com.kzrdut.server.ratelimit.RateLimit;
import com.kzrdut.server.ratelimit.impl.TokenBucketRateLimitImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class RateLimitProvider {
    // 提供限流器
    // 存储每个接口名称和对应的速率限制器之间的映射
    private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    // 默认的限流桶容量和令牌生成速率
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_RATE = 100;

    // 提供限流实例
    public RateLimit getRateLimit(String interfaceName) {
        return rateLimitMap.computeIfAbsent(interfaceName, key -> {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(DEFAULT_RATE, DEFAULT_CAPACITY);
            log.info("为接口 {} 创建了新的限流器: {}", interfaceName, rateLimit);
            return rateLimit;
        });
    }
}
