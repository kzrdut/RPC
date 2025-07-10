package com.kzrdut.server.serviceregister.impl;

import com.kzrdut.annotation.Retryable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import com.kzrdut.server.serviceregister.ServiceRegister;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ZKServiceRegister implements ServiceRegister {
    // 根节点
    private static final String ROOT_PATH = "MyRPC";
    private static final String RETRY = "CanRetry";
    // zookeeper 客户端
    private CuratorFramework client;

    // zookeeper 客户端和服务端之间进行连接
    public ZKServiceRegister() {
        // 指数时间重试策略
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper 的地址固定, 不管是服务提供者还是, 消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg 中的 tickTime 有关系
        // zk 还会根据 minSessionTimeout 与 maxSessionTimeout 两个参数重新调整最后的超时值, 默认分别为 tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        log.info("zookeeper 连接成功");
    }

    @Override
    public void register(Class<?> clazz, InetSocketAddress serviceAddress) {
        String serviceName = clazz.getName();
        try {
            if (client.checkExists().forPath("/" + serviceName) == null) {
                // 将当前服务实例注册成永久节点, 即哪怕服务下线也能够正常工作
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT);
                log.info("服务节点 {} 创建成功", "/" + serviceName);
            }

            // 注册白名单
            List<String> retryableMethods = getRetryableMethod(clazz);
            log.info("可重试的方法: {}", retryableMethods);
            CuratorFramework curatorFramework = client.usingNamespace(RETRY);
            for (String retryableMethod : retryableMethods) {
                // 注册临时节点
                curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + getServiceAddress(serviceAddress) + "/" + retryableMethod);
            }
        } catch (Exception e) {
            log.error("服务注册失败, 服务名: {}, 错误信息: {}", serviceName, e.getMessage(), e);
        }
    }

    private List<String> getRetryableMethod(Class<?> clazz){
        List<String> retryableMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            // 判断方法是否被加上了 Retryable 注解
            if (method.isAnnotationPresent(Retryable.class)) {
                // 添加到白名单中
                String methodSignature = getMethodSignature(clazz, method);
                retryableMethods.add(methodSignature);
            }
        }
        return retryableMethods;
    }

    // 根据接口名字和方法获取方法签名
    // 示例: UserService#getUserById(int, String)
    private String getMethodSignature(Class<?> clazz, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getName()).append("#").append(method.getName()).append("(");
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

    public String getServiceAddress(InetSocketAddress address) {
        return address.getHostName() + ":" + address.getPort();
    }
}
