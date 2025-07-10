package com.kzrdut;

import com.kzrdut.config.RpcConfig;
import com.kzrdut.config.RpcConstant;
import common.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    public static void initialize() {
        RpcConfig customRpcConfig;
        try {
            customRpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.CONFIG_FILE_PREFIX);
            log.info("成功加载配置文件，配置文件名称 = {}", RpcConstant.CONFIG_FILE_PREFIX); // 添加成功加载的日志
        } catch (Exception e) {
            customRpcConfig = new RpcConfig();
            log.warn("配置加载失败，使用默认配置");
        }
        initialize(customRpcConfig);
    }

    public static void initialize(RpcConfig customRpcConfig) {
        rpcConfig = customRpcConfig;
        log.info("RPC 框架初始化，配置 = {}", customRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    initialize();       // 确保值初始化一次, 保证是单例
                }
            }
        }
        return rpcConfig;
    }
}
