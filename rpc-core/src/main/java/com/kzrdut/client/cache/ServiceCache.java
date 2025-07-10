package com.kzrdut.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Description 服务缓存管理器
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class ServiceCache {
    /**
     * 服务注册缓存表
     * <ul>
     *   <li><b>key</b>: 服务名称</li>
     *   <li><b>value</b>: 服务提供者地址列表</li>
     * </ul>
     * 示例:
     * <pre>
     * {"userService": ["127.0.0.1:8080", "192.168.1.100:8080"]}
     * </pre>
     */
    private final Map<String, List<String>> cache = new HashMap<>();

    /**
     * 添加服务地址到缓存
     *
     * @param serviceName 服务名称
     * @param address     服务地址（格式：ip:port）
     * @throws IllegalArgumentException 如果参数为空
     */
    public void addServiceToCache(String serviceName, String address) {
        if (serviceName == null || address == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        cache.computeIfAbsent(serviceName, key -> new ArrayList<>()).add(address);
        log.info("服务[{}]地址[{}]{}添加",
                serviceName,
                address,
                cache.get(serviceName).size() > 1 ? "追加" : "首次");
    }

    /**
     * 获取服务地址列表
     *
     * @param serviceName 服务名称
     * @return 不可修改的地址列表副本，保证线程安全
     */
    public List<String> getServiceFromCache(String serviceName) {
        List<String> addressList = cache.get(serviceName);
        if (addressList == null) {
            log.warn("未注册的服务：{}", serviceName);
            return Collections.emptyList();
        }
        return List.copyOf(addressList);
    }

    /**
     * 替换服务地址
     *
     * @param serviceName 服务名称
     * @param oldAddress  待替换的旧地址
     * @param newAddress  新地址
     */
    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        List<String> addressList = cache.get(serviceName);
        if (addressList != null && addressList.remove(oldAddress)) {
            addressList.add(newAddress);
            log.info("服务[{}]地址已更新：{} -> {}", serviceName, oldAddress, newAddress);
        }
        log.warn("替换失败，服务[{}]未找到地址[{}]", serviceName, oldAddress);
    }

    /**
     * 删除服务地址
     *
     * @param serviceName 服务名称
     * @param address     待删除地址
     */
    public void deleteServiceFromCache(String serviceName, String address) {
        List<String> addresses = cache.get(serviceName);
        if (addresses != null && addresses.remove(address)) {
            log.info("服务[{}]地址[{}]已移除", serviceName, address);
            if (addresses.isEmpty()) {
                cache.remove(serviceName);
                log.info("服务[{}]无可用地址，已清除注册记录", serviceName);
            }
            return;
        }
        log.warn("删除失败，服务[{}]不存在或地址[{}]无效", serviceName, address);
    }
}
