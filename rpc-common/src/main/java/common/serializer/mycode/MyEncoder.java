package common.serializer.mycode;

import common.message.MessageType;
import common.message.RpcRequest;
import common.message.RpcResponse;
import common.serializer.myserializer.Serializer;
import common.trace.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    // MessageToByteEncoder 是 netty 专门用来实现编码器的抽象类
    private Serializer serializer;

    // netty 在写出数据时会调用这个方法, 将 Java 对象编码成二进制数据
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        log.debug("Encoding message of type: {}", msg.getClass());

        // 写入 trace 消息头
        String traceMsg = TraceContext.getTraceId() + ";" + TraceContext.getSpanId();
        byte[] traceBytes = traceMsg.getBytes();
        out.writeInt(traceBytes.length);
        out.writeBytes(traceBytes);

        // 写入消息类型
        if (msg instanceof RpcRequest) {
            // 根据类型写入类型标识
            out.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        } else {
            log.error("Unknown message type: {}", msg.getClass());
            throw new IllegalArgumentException("Unknown message type: " + msg.getClass());
        }

        // 写入序列化方式
        out.writeShort(serializer.getType());
        // 序列化数组
        byte[] serializeBytes = serializer.serialize(msg);
        if (serializeBytes == null || serializeBytes.length == 0) {
            throw new IllegalArgumentException("Serialized message is empty");
        }

        // 写入消息的字节长度
        out.writeInt(serializeBytes.length);
        // 写入序列化数组
        out.writeBytes(serializeBytes);
    }
}
