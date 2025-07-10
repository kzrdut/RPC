package common.serializer.myserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        // 将对象转化为 json 格式的字符的数组
        return JSONObject.toJSONBytes(object);
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object object;
        switch (messageType) {
            case 0:
                // 将字节数组转化为 RpcRequest 对象
                RpcRequest rpcRequest = JSON.parseObject(bytes, RpcRequest.class);
                // 存储解析后的请求参数
                Object[] objects = new Object[rpcRequest.getParamsType().length];
                for (int i = 0; i < objects.length; i++) {
                    // paramsType 是目标参数类型
                    Class<?> paramsType = rpcRequest.getParamsType()[i];
                    // 如果类型兼容, 则直接赋值, 否则使用 fastjson 进行类型转换
                    if (!paramsType.isAssignableFrom(rpcRequest.getParams()[i].getClass())) {
                        objects[i] = JSONObject.toJavaObject((JSONObject) rpcRequest.getParams()[i], rpcRequest.getParamsType()[i]);
                    } else {
                        objects[i] = rpcRequest.getParams()[i];
                    }
                }
                rpcRequest.setParams(objects);
                object = rpcRequest;
                break;
            case 1:
                RpcResponse rpcResponse = JSON.parseObject(bytes, RpcResponse.class);
                Class<?> dataType = rpcResponse.getDataType();
                // 判断转化后的 rpcResponse 对象中的 data 的类型是否正确
                if (!dataType.isAssignableFrom(rpcResponse.getData().getClass())) {
                    rpcResponse.setData(JSONObject.toJavaObject((JSONObject) rpcResponse.getData(), dataType));
                }
                object = rpcResponse;
                break;
            default:
                log.info("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return object;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String toString() {
        return "Json";
    }
}
