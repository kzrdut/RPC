package com.kzrdut.server.server.impl;

import com.kzrdut.server.provider.ServiceProvider;
import com.kzrdut.server.server.RpcServer;
import com.kzrdut.server.server.work.WorkThread;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@AllArgsConstructor
@Slf4j
public class SimpleRPCServer implements RpcServer {
    private ServiceProvider serviceProvider;
    private AtomicBoolean isRunning;          // 控制服务器运行状态
    private ServerSocket serverSocket;

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            log.info("服务器启动了, 监听端口: {}", port);
            while (isRunning.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(new WorkThread(socket, serviceProvider)).start();
                } catch (IOException e) {
                    if (isRunning.get()) {
                        // 如果不是因为服务器被停止导致的异常
                        log.error("连接时发生异常: {}", e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("服务器启动失败: {}", e.getMessage(), e);
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        if (!isRunning.get()) {
            return;
        }

        isRunning.set(false);
        log.info("服务器正在关闭...");

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                // 关闭 ServerSocket
                serverSocket.close();
                log.info("服务器已关闭");
            } catch (IOException e) {
                log.error("关闭服务器时发生异常: {}", e.getMessage(), e);
            }
        }
    }
}
