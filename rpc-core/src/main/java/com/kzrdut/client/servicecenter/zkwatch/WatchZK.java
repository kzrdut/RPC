package com.kzrdut.client.servicecenter.zkwatch;

import com.kzrdut.client.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener.Type;

/**
 * @Description Zookeeper 节点监听器<br>
 * 1. 监听指定路径下的节点创建/更新/删除事件<br>
 * 2. 自动同步节点变化到本地服务缓存<br>
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class WatchZK {
    private CuratorFramework client;                // curator 提供的 zookeeper 客户端
    private ServiceCache serviceCache;              // 本地服务缓存管理器

    public WatchZK(CuratorFramework client, ServiceCache serviceCache) {
        this.client = client;
        this.serviceCache = serviceCache;
    }

    /**
     * 监听指定路径的节点变更事件
     * <p>
     * 监听范围：
     * <ul>
     *   <li>节点创建（NODE_CREATED）</li>
     *   <li>节点数据变更（NODE_CHANGED）</li>
     *   <li>节点删除（NODE_DELETED）</li>
     * </ul>
     *
     * @param path 监听的 Zookeeper 路径
     * @throws InterruptedException 如果监听过程中线程被中断
     */
    public void watchToUpdate(String path) {
        CuratorCache curatorCache = CuratorCache.build(client, path);

        curatorCache.listenable().addListener(this::handleNodeEvent);

        curatorCache.start();
        log.info("已启动对路径[{}]的监听", path);
    }

    /**
     * 处理节点变更事件
     *
     * @param type    事件类型枚举
     * @param oldData 变更前的节点数据（删除/创建时为null）
     * @param newData 变更后的节点数据（删除时为null）
     */
    private void handleNodeEvent(Type type, ChildData oldData, ChildData newData) {
        switch (type) {
            case NODE_CREATED:
                handleNodeCreated(newData);
                break;
            case NODE_CHANGED:
                handleNodeChanged(oldData, newData);
                break;
            case NODE_DELETED:
                handleNodeDeleted(oldData);
                break;
            default:
                log.debug("忽略未处理的事件类型: {}", type);
        }
    }

    private void handleNodeCreated(ChildData newData) {
        String[] pathParts = parsePath(newData);
        if (pathParts.length >= 3) {
            String serviceName = pathParts[1];
            String address = pathParts[2];
            serviceCache.addServiceToCache(serviceName, address);
            log.info("节点创建: 服务[{}] 地址[{}]", serviceName, address);
        }
    }

    private void handleNodeChanged(ChildData oldData, ChildData newData) {
        String[] oldPath = parsePath(oldData);
        String[] newPath = parsePath(newData);

        if (oldPath.length >= 3 && newPath.length >= 3) {
            String serviceName = oldPath[1];
            String oldAddress = oldPath[2];
            String newAddress = newPath[2];

            serviceCache.replaceServiceAddress(serviceName, oldAddress, newAddress);
            log.info("节点更新: 服务[{}], 地址[{} -> {}]",
                    serviceName, oldAddress, newAddress);
        }
    }

    private void handleNodeDeleted(ChildData oldData) {
        String[] pathParts = parsePath(oldData);
        if (pathParts.length >= 3) {
            String serviceName = pathParts[1];
            String address = pathParts[2];
            serviceCache.deleteServiceFromCache(serviceName, address);
            log.info("节点删除: 服务[{}], 地址[{}]", serviceName, address);
        }
    }

    // 解析节点对应地址
    private String[] parsePath(ChildData childData) {
        String path = new String(childData.getData());
        log.info("节点路径: {}", path);
        return path.split("/");
    }
}
