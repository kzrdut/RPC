package com.kzrdut.comsumer;

import com.kzrdut.client.proxy.ClientProxy;
import com.kzrdut.pojo.User;
import com.kzrdut.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ConsumerTest {
    private static final int THREAD_POOL_SIZE = 30;
    // 创建一个固定大小的线程池
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);
        for (int i = 0; i < 120; i++) {
            final Integer i1 = i;
            if (i % 30 == 0) {
                // 模拟每 30 个请求的延迟
                Thread.sleep(10000);
            }

            // 向 Executor 服务(线程池)提交任务
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    User user = proxy.getUserById(i1);
                    if (user != null) {
                        log.info("从服务端得到的user = {}", user);
                    } else {
                        log.warn("获取的 user 为 null, userId = {}", i1);
                    }

                    Integer id = proxy.insertUserById(User.builder()
                            .id(i1)
                            .userName("User" + i1)
                            .gender(true)
                            .build());

                    if (id != null) {
                        log.info("向服务端插入user的id = {}", id);
                    } else {
                        log.warn("插入失败, 返回的id为null, userId = {}", i1);
                    }
                } catch (Exception e) {
                    log.error("调用服务时发生异常, userId = {}", i1, e);
                }
            });
        }

        // 正常关闭执行程序服务
        EXECUTOR_SERVICE.shutdown();
        clientProxy.close();
    }
}
