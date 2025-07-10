package com.kzrdut.server.ratelimit;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface RateLimit {
    // 获取访问许可
    boolean getToken();
}
