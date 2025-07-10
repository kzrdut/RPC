package com.kzrdut.client.servicecenter.balance.impl;

import com.kzrdut.client.servicecenter.balance.LoadBalance;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ConsistencyHashBalance implements LoadBalance {
    // 虚拟节点个数
    private static final int VIRTUAL_NUM = 5;

    @Getter
    // 保存虚拟节点的 hash 值和对应的虚拟节点, key 为 hash 值, value 为虚拟节点的名称
    private SortedMap<Integer, String> shards = new TreeMap<>();

    @Getter
    // 真实节点列表
    private List<String> realNodes = new LinkedList<>();

    // 获取虚拟节点的个数
    public static int getVirtualNum() {
        return VIRTUAL_NUM;
    }

    // FNV1_32_HASH 算法
    // 初始化负载均衡器, 将真实的服务节点和对应的虚拟节点添加到哈希环中
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;
            if (hash < 0) {
                hash = Math.abs(hash);
            }
        }
        return hash;
    }

    public void init(List<String> serviceList) {
        // 初始化负载均衡器, 将真实的服务节点和对应的虚拟节点添加到哈希环中
        for (String server : serviceList) {
            realNodes.add(server);
            log.info("真实节点{} 被添加", server);
            // 遍历 serviceList 真实节点列表, 每个真实节点都会生成 VIRTUAL_NUM 个虚拟节点, 并计算它们的哈希值
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                // 命名规则: server&&VM<i>, 其中 <i> 是编号
                String virtualNode = server + "&&VM" + i;
                // 计算 hash 值并加入到 shards
                int hash = getHash(virtualNode);
                // sortedmap 根据 hash 值对虚拟节点进行排序
                shards.put(hash, virtualNode);
                log.info("虚拟节点{} hash : {}, 被添加", virtualNode, hash);
            }
        }
    }

    /**
     * 获取被分配的节点名
     *
     * @param node
     * @return
     */
    // 根据请求的 node, 选择一个服务器节点
    public String getServer(String node, List<String> serviceList) {
        if (shards.isEmpty()) {
            // 只初始化一次
            init(serviceList);
        }

        // 获取所有 hash 值大于等于给定节点哈希值的虚拟节点
        SortedMap<Integer, String> subMap = shards.tailMap(getHash(node));

        Integer key;
        if (subMap.isEmpty()) {
            // 选择哈希值最大的虚拟节点
            key = shards.lastKey();
        } else {
            // 选中 tailMap 的第一个节点
            key = shards.firstKey();
        }
        String virtualNode = shards.get(key);
        // 返回该虚拟节点对应的真实节点的名称
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    // 模拟负载均衡, 通过生成一个随机字符串(random), 来模拟请求, 最终通过一致性哈希选择一个服务器
    @Override
    public String balance(List<String> addressList) {
        // 如果 addressList 为空或 null, 抛出 IllegalArgumentException
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }
        // 生成一个随机字符串, 然后将其作为 node 参数传递给 getserver 方法, 来获取相应的服务器地址
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }

    @Override
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            log.info("真实节点{} 上线添加", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VM" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                log.info("虚拟节点{} hash : {}, 被添加", virtualNode, hash);
            }
        }
    }

    @Override
    public void delNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            log.info("真实节点 [{}] 下线移除", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VM" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                log.info("虚拟节点 [{}] hash: {}, 被移除", virtualNode, hash);
            }
        }
    }

    @Override
    public String toString() {
        return "ConsistencyHash";
    }
}
