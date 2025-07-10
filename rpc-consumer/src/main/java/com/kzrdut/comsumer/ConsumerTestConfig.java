package com.kzrdut.comsumer;

import com.kzrdut.config.RpcConfig;
import common.util.ConfigUtil;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class ConsumerTestConfig {
    public static void main(String[] args) {
        // TODO:待测试
        RpcConfig rpc = ConfigUtil.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
