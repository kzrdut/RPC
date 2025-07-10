package common.serializer.myserializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.kzrdut.pojo.User;
import common.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class KryoSerializer implements Serializer {
    private Kryo kryo;

    public KryoSerializer() {
        this.kryo = new Kryo();
    }

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("无法序列空对象");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            kryo.writeObject(output, object); // 使用 Kryo 写入对象
            return output.toBytes(); // 返回字节数组
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("无法反序列化 null 或空字节数组");
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            // 根据 messageType 来反序列化不同的类
            Class<?> clazz = getClassForMessageType(messageType);
            return kryo.readObject(input, clazz);       // 使用 Kryo 反序列化对象
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
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
        return 2;
    }

    @Override
    public String toString() {
        return "Kryo";
    }
}
