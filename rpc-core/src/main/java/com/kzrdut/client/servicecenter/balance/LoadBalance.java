package com.kzrdut.client.servicecenter.balance;

import java.util.List;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public interface LoadBalance {
    String balance(List<String> addressList);

    void addNode(String node);

    void delNode(String node);
}
