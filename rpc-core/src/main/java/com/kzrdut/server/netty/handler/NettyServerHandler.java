package com.kzrdut.server.netty.handler;

import com.kzrdut.server.provider.ServiceProvider;
import com.kzrdut.server.ratelimit.RateLimit;
import com.kzrdut.trace.interceptor.ServerTraceInterceptor;
import common.message.RequestType;
import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
// 处理服务器端响应的处理器
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private ServiceProvider serviceProvider;

    public NettyServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 接收 rpcRequest, 读取并调用服务
        if (rpcRequest == null) {
            log.error("RpcRequest 为空!");
            return;
        }
        if (rpcRequest.getType() == RequestType.HEARTBEAT) {
            log.info("接收到来自客户端的心跳包");
            return;
        }

        if (rpcRequest.getType() == RequestType.NORMAL) {
            // trace 记录
            ServerTraceInterceptor.beforeHandle();
            // trace 上报
            ServerTraceInterceptor.afterHandle(rpcRequest.getMethodName());

            RpcResponse response = getResponse(rpcRequest);
            channelHandlerContext.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("处理请求时发生异常: ", cause);
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        // 得到服务名
        String interfaceName = rpcRequest.getInterfaceName();
        // 接口限流降级
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if (!rateLimit.getToken()) {
            // 如果获取令牌失败, 进行限流降级, 快速返回结果
            log.warn("服务限流，接口: {}", interfaceName);
            return RpcResponse.fail("服务限流, 接口 " + interfaceName + " 当前无法处理请求, 请稍后再试");
        }

        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("方法执行错误, 接口: {}, 方法: {}", interfaceName, rpcRequest.getMethodName(), e);
            return RpcResponse.fail("方法执行错误");
        }
    }
}
