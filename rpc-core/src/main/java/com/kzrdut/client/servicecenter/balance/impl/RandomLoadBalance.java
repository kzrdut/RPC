package com.kzrdut.client.servicecenter.balance.impl;

import com.kzrdut.client.servicecenter.balance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class RandomLoadBalance implements LoadBalance {
    // 将Random声明为类级别的字段
    private final Random random = new Random();

    private final List<String> addressList = new CopyOnWriteArrayList<>();

    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }
        int choose = random.nextInt(addressList.size());
        log.info("负载均衡选择了第 {} 号服务器, 地址是:{}", choose, addressList.get(choose));
        return addressList.get(choose);
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
