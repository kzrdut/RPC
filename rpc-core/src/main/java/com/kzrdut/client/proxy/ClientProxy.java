package com.kzrdut.client.proxy;

import com.kzrdut.client.circuitbreaker.CircuitBreaker;
import com.kzrdut.client.circuitbreaker.CircuitBreakerProvider;
import com.kzrdut.client.retry.GuavaRetry;
import com.kzrdut.client.rpcClient.RPCClient;
import com.kzrdut.client.rpcClient.impl.NettyRPCClient;
import com.kzrdut.client.servicecenter.ServiceCenter;
import com.kzrdut.client.servicecenter.impl.ZKServiceCenter;
import com.kzrdut.trace.interceptor.ClientTraceInterceptor;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

@Slf4j
public class ClientProxy implements InvocationHandler {
    private RPCClient rpcClient;
    private ServiceCenter serviceCenter;
    private CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy() throws InterruptedException {
        serviceCenter = new ZKServiceCenter();
        circuitBreakerProvider = new CircuitBreakerProvider();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // trace 记录
        ClientTraceInterceptor.beforeInvoke();
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();

        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        // 判断熔断器是否允许通过
        if (!circuitBreaker.allowRequest()) {
            log.warn("熔断器开启, 请求被拒绝: {}", rpcRequest);
            return null;
        }

        RpcResponse rpcResponse;
        // 为保持幂等性, 只对白名单上的服务进行重试操作
        // 先检查是否需要重试
        String methodSignature = getMethodSignature(rpcRequest.getInterfaceName(), method);
        log.info("方法签名: {}", methodSignature);

        InetSocketAddress serviceAddress = serviceCenter.serviceDiscovery(rpcRequest);
        rpcClient = new NettyRPCClient(serviceAddress);
        if (serviceCenter.checkRetry(serviceAddress, methodSignature)) {
            // 调用 retry 进行重试操作
            try {
                log.info("尝试重试调用服务: {}", methodSignature);
                rpcResponse = new GuavaRetry().sendRequestWithRetry(rpcRequest, rpcClient);
            } catch (Exception e) {
                log.error("重试调用失败: {}", methodSignature, e);
                circuitBreaker.recordFailure();
                throw e;  // 将异常抛给调用者
            }
        } else {
            // 只调用一次
            rpcResponse = rpcClient.sendRequest(rpcRequest);
        }

        if (rpcResponse != null) {
            if (rpcResponse.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if (rpcResponse.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", rpcRequest.getInterfaceName(), rpcResponse.getCode());
        }

        // 上报 trace
        ClientTraceInterceptor.afterInvoke(method.getName());

        return rpcResponse != null ? rpcResponse.getData() : null;
    }

    public <T> T getProxy(Class<T> clazz) {
        // 表示由当前类来代理指定 class 对象
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }

    // 根据接口名字和方法获取方法签名
    private String getMethodSignature(String interfaceName, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            builder.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                builder.append(",");
            } else {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    // 关闭创建的资源
    // 注: 如果在需要 C-S 保持长连接的场景下无需调用 close 方法
    public void close() {
        rpcClient.close();
        serviceCenter.close();
    }
}
