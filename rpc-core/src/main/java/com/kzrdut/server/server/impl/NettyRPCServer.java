package com.kzrdut.server.server.impl;

import com.kzrdut.server.netty.nettyInitializer.NettyServerInitializer;
import com.kzrdut.server.provider.ServiceProvider;
import com.kzrdut.server.server.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class NettyRPCServer implements RpcServer {
    private final ServiceProvider serviceProvider;
    private Channel serverChannel;                      // 用于主动关闭监听端口
    private NioEventLoopGroup bossGroup;                // 负责处理客户端的连接请求
    private NioEventLoopGroup workerGroup;              // 进行数据读写操作

    public NettyRPCServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        log.info("Netty 服务端启动中, 端口: {}", port);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            serverChannel = channelFuture.channel();
            log.info("Netty 服务端启动成功，监听端口 {}", port);

            // 阻塞，直到通道关闭
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Netty 服务端启动中断:{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Netty 服务端异常:{}", e.getMessage(), e);
        } finally {
            shutdown(bossGroup, workerGroup);
            log.info("Netty 服务端已关闭");
        }
    }

    @Override
    public void stop() {
        if (serverChannel != null && serverChannel.isOpen()) {
            try {
                serverChannel.close().sync();
                log.info("Netty 服务端主通道已关闭");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("关闭 Netty 服务端主通道时中断: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Netty 服务端主通道尚未启动或已关闭");
        }

        shutdown(bossGroup, workerGroup);
    }

    private void shutdown(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup) {
        if (bossGroup != null && !bossGroup.isShuttingDown()) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            log.info("Boss 线程组已关闭");
        }
        if (workerGroup != null && !workerGroup.isShuttingDown()) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            log.info("Worker 线程组已关闭");
        }
    }
}
