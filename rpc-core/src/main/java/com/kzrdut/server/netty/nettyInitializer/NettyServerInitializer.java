package com.kzrdut.server.netty.nettyInitializer;

import com.kzrdut.server.netty.handler.HeartBeatHandler;
import com.kzrdut.server.netty.handler.NettyServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import common.serializer.mycode.MyDecoder;
import common.serializer.mycode.MyEncoder;
import common.serializer.myserializer.JsonSerializer;
import io.netty.handler.timeout.IdleStateHandler;
import com.kzrdut.server.provider.ServiceProvider;

import java.util.concurrent.TimeUnit;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    public NettyServerInitializer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 服务端关注读事件和写事件, 如果 10 秒内没有收到客户端的消息, 将会触发 IdleState.READER_IDLE 事件, 将由 HeartbeatHandler 进行处理
        pipeline.addLast(new IdleStateHandler(20,20,0, TimeUnit.SECONDS));
        pipeline.addLast(new HeartBeatHandler());
        // 添加自定义的编/解码器, 这里使用的是 json
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
