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
public class RpcRequest implements Serializable {
    private RequestType type = RequestType.NORMAL;      // 请求类型
    private String interfaceName;       // 服务类名, 客户端只知道接口
    private String methodName;          // 方法名
    private Object[] params;            // 参数列表
    private Class<?>[] paramsType;      // 参数类型

    public static RpcRequest heartBeat(){
        return RpcRequest.builder().type(RequestType.HEARTBEAT).build();
    }
}
