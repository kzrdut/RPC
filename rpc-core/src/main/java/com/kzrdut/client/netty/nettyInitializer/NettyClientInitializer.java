package com.kzrdut.client.netty.nettyInitializer;

import com.kzrdut.client.netty.handler.HeartBeatHandler;
import com.kzrdut.client.netty.handler.MDCChannelHandler;
import common.serializer.myserializer.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import com.kzrdut.client.netty.handler.NettyClientHandler;
import common.serializer.mycode.MyDecoder;
import common.serializer.mycode.MyEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    public NettyClientInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        try {
            // 根据传入的序列化器类型初始化编码器
            pipeline.addLast(new MyEncoder(Serializer.getSerializerByCode(3)));
            pipeline.addLast(new MyDecoder());
            pipeline.addLast(new NettyClientHandler());
            pipeline.addLast(new MDCChannelHandler());
            // 客户端只关注写事件, 如果超过 10 秒没有发送数据, 则发送心跳包
            pipeline.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
            pipeline.addLast(new HeartBeatHandler());
            log.info("使用序列化程序类型初始化的 Netty 客户端管道: {}",Serializer.getSerializerByCode(3).getType());
        } catch (Exception e) {
            log.error("初始化 Netty 客户端管道时出错", e);
            throw e;  // 重新抛出异常, 确保管道初始化失败时处理正确
        }
    }
}
