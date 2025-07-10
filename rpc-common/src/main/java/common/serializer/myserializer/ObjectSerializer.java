package common.serializer.myserializer;

import java.io.*;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        byte[] bytes;
        // 创建一个内存中的输出流, 用于存储序列化后的字节数组
        // 其中 baos 是一个可变大小的字节数据缓冲区
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            // 一个对象输出流, 用于将 Java 对象序列化为字节流, 并将其连接到 baos 上
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();

            // 将 baos 其内部缓冲区中的数据转换为字节数组
            bytes = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object object;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            object = objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String toString() {
        return "JDK";
    }
}
