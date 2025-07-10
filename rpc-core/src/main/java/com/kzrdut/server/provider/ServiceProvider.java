package com.kzrdut.server.provider;

import com.kzrdut.server.serviceregister.ServiceRegister;
import com.kzrdut.server.serviceregister.impl.ZKServiceRegister;
import lombok.Getter;
import com.kzrdut.server.ratelimit.provider.RateLimitProvider;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ServiceProvider {
    // 服务提供中心
    private Map<String, Object> interfaceProvider;
    private final String host;
    private final int port;
    // 注册服务类
    private ServiceRegister serviceRegister;
    // 限流器
    private @Getter RateLimitProvider rateLimitProvider;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
        this.rateLimitProvider = new RateLimitProvider();
    }

    public void registerServiceInterface(Object service) {
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for (Class<?> clazz : interfaceName) {
            // 本机的映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(clazz, new InetSocketAddress(host, port));
        }
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
