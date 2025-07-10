package com.kzrdut.provider;

import com.kzrdut.RpcApplication;
import com.kzrdut.provider.impl.UserServiceImpl;
import com.kzrdut.server.provider.ServiceProvider;
import com.kzrdut.server.server.RpcServer;
import com.kzrdut.server.server.impl.NettyRPCServer;
import com.kzrdut.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ProviderTest {
    public static void main(String[] args) {
        RpcApplication.initialize();
        String host = RpcApplication.getRpcConfig().getHost();
        int port = RpcApplication.getRpcConfig().getPort();

        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(host, port);
        serviceProvider.registerServiceInterface(userService);

        RpcServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(port);
        log.info("RPC 服务端启动, 监听端口 {}", port);
    }
}
