package com.kzrdut.config;

import com.kzrdut.client.servicecenter.balance.impl.ConsistencyHashBalance;
import com.kzrdut.server.serviceregister.impl.ZKServiceRegister;
import common.serializer.myserializer.Serializer;
import lombok.*;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcConfig {
    // 名称
    private String name = "rpc";
    // 端口
    private Integer port = 9999;
    // 主机名
    private String host = "localhost";
    // 版本号
    private String version = "1.0.0";
    // 注册中心
    private String registry = new ZKServiceRegister().toString();
    // 序列化器
    private String serializer = Serializer.getSerializerByCode(3).toString();
    // 负载均衡
    private String loadBalance = new ConsistencyHashBalance().toString();
}
