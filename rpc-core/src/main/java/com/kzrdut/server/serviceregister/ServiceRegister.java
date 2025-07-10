package com.kzrdut.server.serviceregister;

import java.net.InetSocketAddress;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface ServiceRegister {
    // 服务注册, 服务名和对应的地址
    void register(Class<?> clazz, InetSocketAddress address);
}
