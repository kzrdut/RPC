package com.kzrdut.client.servicecenter;

import common.message.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface ServiceCenter {
    // 根据服务名查找地址
    InetSocketAddress serviceDiscovery(RpcRequest rpcRequest);

    // 判断是否可以重试
    boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature);

    // 关闭客户端
    void close();
}
