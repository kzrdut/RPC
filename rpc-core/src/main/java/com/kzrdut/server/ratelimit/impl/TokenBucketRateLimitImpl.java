package com.kzrdut.server.ratelimit.impl;

import com.kzrdut.server.ratelimit.RateLimit;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class TokenBucketRateLimitImpl implements RateLimit {
    private final long RATE;                // 令牌产生速率(单位为 ms)
    private final long CAPACITY;            // 令牌桶容量
    private volatile long curCapacity;      // 当前容量
    private volatile long timeStamp;        // 距离上一次请求的时间戳

    public TokenBucketRateLimitImpl(int rate, int capacity) {
        this.RATE = rate;
        this.CAPACITY = capacity;
        this.curCapacity = capacity;
    }

    private void refill() {
        long currentTime = System.currentTimeMillis();          // 当前时间
        long elapsed = currentTime - timeStamp;                 // 经过的时间
        long token = elapsed * RATE;                        // 生成的令牌数
        curCapacity = Math.max(curCapacity + token, CAPACITY);
        timeStamp = currentTime;
    }

    @Override
    public synchronized boolean getToken() {
        refill();
        if (curCapacity > 0) {
            curCapacity--;
            return true;
        }
        return false;
    }
}
