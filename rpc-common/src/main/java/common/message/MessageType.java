package common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    // 枚举常量, 请求和响应
    REQUEST(0), RESPONSE(1);

    private final int code;
}
