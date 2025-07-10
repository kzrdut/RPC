#   轻量级异步 RPC 框架 —— 支持链路追踪与插件化架构

本项目是一个基于 Netty 实现的轻量级远程过程调用（RPC）框架，集成了服务注册与发现、序列化、负载均衡、熔断限流、异步链路追踪等关键能力，采用模块化设计，适用于高并发和可扩展的分布式系统。

---

##  项目特性

* **服务注册与发现**：基于 Zookeeper 实现服务中心，支持动态感知服务上下线。
* **灵活的序列化机制**：支持 Kryo、Hessian、JSON、Protostuff，基于 SPI 可热插拔切换。
* **多样的负载均衡策略**：内置随机、轮询、一致性哈希等多种策略。
* **熔断与限流支持**：集成令牌桶算法与自定义熔断器，提升系统鲁棒性。
* **链路追踪能力**：对接 Zipkin，支持完整调用链追踪。
* **可配置的重试机制**：基于注解与 Guava Retry 自动进行失败重试。
* **模块化架构**：各模块职责清晰，便于理解、替换与扩展。

---

##  模块结构

```text
rpc-api/                 # 服务接口定义
│
├── annotation/          # 自定义注解，如 @Retryable
├── pojo/                # 公共 DTO 类，如 User.java
└── service/             # RPC 接口，如 UserService.java

rpc-common/              # 通用工具和抽象模块
│
├── exception/           # 自定义异常定义
├── message/             # 协议消息结构
├── serializer/          # 序列化接口与实现
├── spi/                 # SPI 加载机制
├── trace/               # 链路追踪工具
└── util/                # 通用工具类

rpc-core/                # 核心功能实现
│
├── client/              # 客户端核心逻辑
│   ├── circuitbreaker/  # 熔断器
│   ├── netty/           # 网络通信
│   ├── proxy/           # 动态代理
│   ├── retry/           # 重试机制
│   └── servicecenter/   # 服务发现
├── config/              # 配置管理
├── server/              # 服务端逻辑
│   ├── netty/           # 服务端网络模块
│   ├── provider/        # 服务发布与注册
│   ├── ratelimit/       # 限流实现
│   └── serviceregister/ # 注册机制
└── trace/               # 链路追踪

rpc-provider/            # 服务提供者（服务端启动模块）
│
├── impl/                # 接口实现类
└── ProviderTest.java    # 启动类

rpc-consumer/            # 服务消费者（客户端调用模块）
│
└── ConsumerTest.java    # 调用入口
```

---

## 技术选型

* **Java 8+**
* **Netty**：高性能 NIO 网络通信框架
* **Zookeeper**：服务注册与发现中心
* **SLF4J + Logback**：日志组件
* **Zipkin**：分布式链路追踪
* **SPI**：实现插件式架构，支持动态序列化协议加载

---

## 快速启动指南

1. 启动本地 Zookeeper 服务（默认端口 2181）
2. 编译并启动 `rpc-provider` 模块
3. 启动 `rpc-consumer` 模块发起 RPC 调用
4. 观察链路追踪（Zipkin）、日志与调用结果

---
