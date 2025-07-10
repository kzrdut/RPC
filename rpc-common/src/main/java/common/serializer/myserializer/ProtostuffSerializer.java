package common.serializer.myserializer;

import com.kzrdut.pojo.User;
import common.exception.SerializeException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ProtostuffSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("无法序列空对象");
        }

        Schema schema = RuntimeSchema.getSchema(object.getClass());

        // 使用 LinkedBuffer 来创建缓冲区(默认大小 1024)
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        // 序列化对象为字节数组
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("无法反序列化 null 或空字节数组");
        }

        // 根据 messageType 来反序列化不同的类
        Class<?> clazz = getClassForMessageType(messageType);
        Schema schema = RuntimeSchema.getSchema(clazz);
        Object obj;
        try {
            obj = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SerializeException("由于反射问题, 反序列化失败");
        }

        // 反序列化字节数组为对象
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    private Class<?> getClassForMessageType(int messageType) {
        if (messageType == 1) {
            return User.class;
        } else {
            throw new SerializeException("未知消息类型: " + messageType);
        }
    }

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public String toString() {
        return "Protostuff";
    }
}
