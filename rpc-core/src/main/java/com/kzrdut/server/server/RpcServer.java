package com.kzrdut.server.server;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface RpcServer {
    void start(int port);
    void stop();
}
