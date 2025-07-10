package common.serializer.mycode;

import common.exception.SerializeException;
import common.message.MessageType;
import common.serializer.myserializer.Serializer;
import common.trace.TraceContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class MyDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(MyDecoder.class);

    // 将字节流解码为对象, 并添加到 out 中, 供下一个 handler 使用
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 6) {
            // messageType + serializerType + length
            return;
        }

        // 读取 traceMsg
        int traceLength = in.readInt();
        byte[] traceBytes = new byte[traceLength];
        in.readBytes(traceBytes);
        serializeTraceMsg(traceBytes);

        // 读取消息类型
        short messageType = in.readShort();
        if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            log.warn("暂不支持此类型数据, messageType: {}", messageType);
        }

        // 读取序列化方式
        short serializerType = in.readShort();      // 读取序列化类型
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if (serializer == null) {
            log.error("不存在对应的序列化器, serializerType: {}", serializerType);
            throw new SerializeException("不存在对应的序列化器, serializerType: " + serializerType);
        }

        // 读取序列化数组长度
        int length = in.readInt();
        if (in.readableBytes() < length) {
            // 数据不完整, 等待更多数据
            return;
        }

        // 读取序列化数组
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        log.debug("已收到字节: {}", Arrays.toString(bytes));

        // 反序列化对象
        Object deserialize = serializer.deserialize(bytes, messageType);
        // 将对象添加到 out 列表中
        out.add(deserialize);
    }

    private void serializeTraceMsg(byte[] traceByte) {
        String traceMsg = new String(traceByte);
        String[] msgs = traceMsg.split(";");

        if (!msgs[0].isEmpty()) {
            TraceContext.setTraceId(msgs[0]);
        }
        if (!msgs[1].isEmpty()) {
            TraceContext.setParentSpanId(msgs[1]);
        }
    }
}
