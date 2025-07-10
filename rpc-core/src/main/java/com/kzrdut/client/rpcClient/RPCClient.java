package com.kzrdut.client.rpcClient;

import common.message.RpcRequest;
import common.message.RpcResponse;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface RPCClient {
    // 定义底层通信的方法
    RpcResponse sendRequest(RpcRequest rpcRequest);

    // 关闭资源
    void close();
}
