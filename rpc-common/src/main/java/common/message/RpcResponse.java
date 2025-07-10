package common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcResponse implements Serializable {
    private int code;           // 状态码
    private String message;     // 返回消息
    private Class<?> dataType;  // 加入传输数据的类型, 以便在自定义序列化器中解析
    private Object data;        // 具体数据

    // 构造成功信息
    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).dataType(data.getClass()).data(data).build();
    }

    // 构造失败信息
    public static RpcResponse fail(String msg) {
        return RpcResponse.builder().code(500).message(msg).build();
    }
}
