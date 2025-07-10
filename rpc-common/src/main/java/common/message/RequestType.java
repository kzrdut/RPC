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
public enum RequestType {
    NORMAL(0), HEARTBEAT(1);

    private final int code;
}
