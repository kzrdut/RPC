package com.kzrdut.client.servicecenter.impl;

import com.kzrdut.client.cache.ServiceCache;
import com.kzrdut.client.servicecenter.ServiceCenter;
import com.kzrdut.client.servicecenter.balance.impl.ConsistencyHashBalance;
import com.kzrdut.client.servicecenter.zkwatch.WatchZK;
import common.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ZKServiceCenter implements ServiceCenter {
    // zookeeper 根路径节点
    private static final String ROOT_PATH = "MyRPC";
    private static final String RETRY = "CanRetry";
    // 使用 zookeeper 的客户端
    private CuratorFramework client;
    // 本地缓存
    private ServiceCache serviceCache;
    // 保证线程安全
    private Set<String> retryServiceCache = new CopyOnWriteArraySet<>();

    // 由于 zookeeper 提供了客户端和服务端, 所以这里需要进行C-S间的连接配置
    public ZKServiceCenter() throws InterruptedException {
        // 指数时间重试策略
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // 使用心跳监听状态
        client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        client.start();
        log.info("Zookeeper 连接成功");

        // 初始化本地缓存
        serviceCache = new ServiceCache();
        // 注册 zookeeper 事件监听器
        WatchZK watcher = new WatchZK(client, serviceCache);
        watcher.watchToUpdate(ROOT_PATH);       // 对当前根节点路径进行监听
    }

    @Override
    public InetSocketAddress serviceDiscovery(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getInterfaceName();
        try {
            // 先从缓存中找
            List<String> serviceList = serviceCache.getServiceFromCache(serviceName);
            // 如果为空(一般是初始化时), 则将 zookeeper 中的地址添加到缓存中
            if (serviceList == null) {
                serviceList = client.getChildren().forPath("/" + serviceName);
                for (String service : serviceList) {
                    serviceCache.addServiceToCache(serviceName, service);
                }
            }
            if (serviceList.isEmpty()) {
                log.warn("未找到服务:{}", serviceName);
            }
            // 负载均衡
            String address = new ConsistencyHashBalance().balance(serviceList);
            return parseAddress(address);
        } catch (Exception e) {
            log.error("服务查找失败, 服务名: {}", serviceName, e);
        }
        return null;
    }

    @Override
    public boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature) {
        // 白名单缓存
        if (retryServiceCache.isEmpty()) {
            try {
                // 创建一个 RETRY 命名空间的实例
                CuratorFramework curatorFramework = client.usingNamespace(RETRY);
                List<String> retryableMethods = curatorFramework.getChildren().forPath("/" + getServiceAddress(serviceAddress));
                retryServiceCache.addAll(retryableMethods);
            } catch (Exception e) {
                log.error("检查重试失败, 方法签名:{}", methodSignature, e);
            }
        }
        return retryServiceCache.contains(methodSignature);
    }

    @Override
    public void close() {
        client.close();
    }

    // 将 地址 转换为 xxx.xxx.xxx.xxx:port 形式
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
