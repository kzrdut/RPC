package com.kzrdut.client.retry;

import com.github.rholder.retry.*;
import com.kzrdut.client.rpcClient.RPCClient;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class GuavaRetry {
    public RpcResponse sendRequestWithRetry(RpcRequest rpcRequest, RPCClient rpcClient) {
        // 设置超时重试机制
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 无论出现什么异常, 都进行重试
                .retryIfException()
                // 返回结果为 error(状态码为 500)时进行
                .retryIfResult(rpcResponse -> Objects.equals(rpcResponse.getCode(), 500))
                // 重试等待策略: 等待 2s 后再进行重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试停止策略: 重试次数达到 3 次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试监听器: 第 {} 次调用", attempt.getAttemptNumber());
                    }
                })
                .build();

        try {
            // 执行 RPC 请求, 进行重试
            return retryer.call(() -> rpcClient.sendRequest(rpcRequest));
        } catch (Exception e) {
            log.error("重试失败: 请求 {} 执行时遇到异常", rpcRequest.getMethodName(), e);
        }
        return RpcResponse.fail("重试失败, 所有重试尝试已结束");
    }
}
