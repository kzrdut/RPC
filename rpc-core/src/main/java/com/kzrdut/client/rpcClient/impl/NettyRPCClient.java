package com.kzrdut.client.rpcClient.impl;

import com.kzrdut.client.netty.handler.MDCChannelHandler;
import common.trace.TraceContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import com.kzrdut.client.netty.nettyInitializer.NettyClientInitializer;
import com.kzrdut.client.rpcClient.RPCClient;
import com.kzrdut.client.servicecenter.ServiceCenter;
import com.kzrdut.client.servicecenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.slf4j.MDCContextMap;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class NettyRPCClient implements RPCClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;

    private final InetSocketAddress address;

    // netty 客户端初始化
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                // NettyClientInitializer 这里配置 netty 对消息的处理机制
                .handler(new NettyClientInitializer());
    }

    public NettyRPCClient(InetSocketAddress address) throws InterruptedException {
        this.address = address;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        // 获取当前链路跟踪上下文的一个快照
        Map<String, String> MDCContextMap = TraceContext.getCopy();
        if (address == null) {
            log.error("服务发现失败, 返回的地址为 null");
            return RpcResponse.fail("服务发现失败, 地址为 null");
        }
        // 从注册中心获取 host, post
        String host = address.getHostName();
        int port = address.getPort();
        try {
            // 连接到远程服务
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.attr(MDCChannelHandler.TRACE_CONTEXT_KEY).set(MDCContextMap);
            // 发送数据
            channel.writeAndFlush(rpcRequest);
            // sync() 堵塞获取结果
            channel.closeFuture().sync();
            // 阻塞的获得结果, 通过给 channel 设计别名, 获取特定名字下的 channel 中的内容
            // AttributeKey 是线程隔离的, 不会有线程安全问题
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
            RpcResponse rpcResponse = channel.attr(key).get();

            if (rpcResponse == null) {
                log.error("服务响应为空, 可能是请求失败或超时");
                return RpcResponse.fail("服务响应为空");
            }

            log.info("收到响应: {}", rpcRequest);
            return rpcResponse;
        } catch (InterruptedException e) {
            log.error("请求被中断, 发送请求失败: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("发送请求时发生异常: {}", e.getMessage(), e);
        }
        return RpcResponse.fail("请求失败");
    }

    // 优雅关闭 Netty 资源
    public void close() {
        try {
            eventLoopGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error("关闭 Netty 资源时发生异常: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
