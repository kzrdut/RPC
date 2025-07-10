package common.serializer.myserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface Serializer {
    // 将对象序列化为字节数组
    // 因为网络通信中, 数据只能以字节流形式进行传输
    byte[] serialize(Object object);

    // 将字节数组反序列化为 Java 对象
    Object deserialize(byte[] bytes, int messageType);

    // 返回使用的序列化器
    int getType();

    // 定义静态常量 serializerMap
    Map<Integer, Serializer> serializerMap = new HashMap<>();

    // 根据序号取出序列化器
    static Serializer getSerializerByCode(int code) {
        // 静态映射, 保证只初始化一次
        if(serializerMap.isEmpty()) {
            serializerMap.put(0, new ObjectSerializer());
            serializerMap.put(1, new JsonSerializer());
            serializerMap.put(2, new KryoSerializer());
            serializerMap.put(3, new HessianSerializer());
            serializerMap.put(4, new ProtostuffSerializer());
        }
        return serializerMap.get(code); // 如果不存在, 则返回 null
    }
}
