package com.kzrdut.client.servicecenter.balance.impl;

import com.kzrdut.client.servicecenter.balance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class RoundLoadBalance implements LoadBalance {
    // 使用 AtomicInteger 保证线程安全
    private AtomicInteger choose = new AtomicInteger(0);

    private List<String> addressList = new CopyOnWriteArrayList<>();

    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }

        // 获取当前索引并更新为下一个
        int currentChoose = choose.getAndUpdate(i -> (i + 1) % addressList.size());

        String selectedServer = addressList.get(currentChoose);
        log.info("负载均衡选择了服务器: {}", selectedServer);
        return selectedServer;
    }

    @Override
    public void addNode(String node) {
        addressList.add(node);
        log.info("节点 {} 已加入负载均衡", node);
    }

    @Override
    public void delNode(String node) {
        addressList.remove(node);
        log.info("节点 {} 已从负载均衡中移除", node);
    }
}
