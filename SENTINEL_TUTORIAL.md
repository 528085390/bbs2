# Sentinel 熔断限流教程

## 一、Sentinel 在项目中的作用

### 1.1 为什么需要熔断限流

微服务架构中，服务间通过 Feign HTTP 调用形成**调用链**，一个服务不可用可能引发**雪崩效应**：

```
post-service ──Feign──▶ section-service
comment-service ──Feign──▶ post-service
interaction-service ──Feign──▶ post-service
search-service ──Feign──▶ post-service
```

| 问题 | 说明 |
|------|------|
| **雪崩传播** | section-service 宕机 → post-service 所有涉及版块查询的接口全部阻塞等待 → 连接池耗尽 → post-service 也宕机 |
| **上游拥塞** | Feign 默认使用阻塞 HTTP 客户端，下游慢响应会快速耗尽上游线程池 |
| **恶意请求** | 认证接口被暴力破解、搜索接口被爬虫大量调用，拖垮整个系统 |

Sentinel 提供了两把保护伞：

| 保护机制 | 作用层面 | 解决什么问题 |
|---------|---------|------------|
| **熔断降级** | 服务间 Feign 调用 | 下游不可用时快速失败，防止雪崩 |
| **网关限流** | 入口流量 | 控制各 API 的 QPS，防止恶意请求 |

### 1.2 保护了什么

**熔断降级（4 个 Feign 客户端）：**

| 上游服务 | Feign 客户端 | 下游服务 | 降级行为 |
|---------|-------------|---------|---------|
| `bbs-post-service` | `SectionClient` | `bbs-section-service` | 返回错误响应，业务层跳过版块信息 |
| `bbs-comment-service` | `PostClient` | `bbs-post-service` | 返回错误响应，评论创建失败 |
| `bbs-interaction-service` | `PostClient` | `bbs-post-service` | 返回错误响应，互动操作失败 |
| `bbs-search-service` | `PostClient` | `bbs-post-service` | **返回空列表**，搜索功能降级可用 |

**网关限流（3 个 API 分组）：**

| API 分组 | 限流阈值 | 保护目标 |
|---------|---------|---------|
| `auth_api` (`/api/auth/**`) | 20 QPS | 防止登录/注册接口被爆破 |
| `post_api` (`/api/posts/**`) | 50 QPS | 防止帖子接口被刷 |
| `search_api` (`/api/search/**`) | 20 QPS | 防止搜索接口滥用 |

---

## 二、生效原理

### 2.1 整体架构

```
                     ┌─────────────────────────────┐
                     │        API Gateway          │
                     │       (bbs-gateway)          │
                     │                              │
                     │  auth_api: 20 QPS            │
                     │  post_api: 50 QPS            │
                     │  search_api: 20 QPS          │
                     └──────┬──────────────────────┘
                            │
          ┌─────────────────┼────────────────────┐
          ▼                 ▼                    ▼
   ┌────────────┐   ┌────────────┐   ┌──────────────────┐
   │ post-svc   │   │ comment-svc│   │ interaction-svc   │
   │            │   │            │   │                   │
   │ Feign ──▶  │   │ Feign ──▶  │   │ Feign ──▶         │
   │ section-svc │   │ post-svc   │   │ post-svc          │
   │ ⚡ fallback  │   │ ⚡ fallback │   │ ⚡ fallback       │
   └────────────┘   └────────────┘   └──────────────────┘
                                              │
                                      ┌───────▼────────┐
                                      │  search-svc     │
                                      │  Feign ──▶      │
                                      │  post-svc       │
                                      │  ⚡ fallback     │
                                      │  → 空列表        │
                                      └────────────────┘
```

### 2.2 Sentinel 核心概念

| 概念 | 本项目中的应用 | 作用 |
|------|--------------|------|
| **资源** | Feign 方法调用 / API 路径 | 被 Sentinel 保护的对象 |
| **熔断** | Feign 调用失败达到阈值后，直接返回降级结果 | 防止雪崩传播 |
| **降级** | `FallbackFactory` 返回兜底数据 | 服务不可用时的备用逻辑 |
| **限流** | `GatewayFlowRule` 控制 API 分组 QPS | 限制入口流量 |
| **规则** | 代码中 `@PostConstruct` 定义 | 熔断/限流的配置规则 |

### 2.3 三层配置

#### 第一层：依赖引入（pom.xml）

**业务服务（4 个服务）：**

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

添加的服务：`bbs-post-service`、`bbs-comment-service`、`bbs-interaction-service`、`bbs-search-service`

**网关（bbs-gateway）：**

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
</dependency>
```

网关额外需要 `sentinel-spring-cloud-gateway-adapter` 来适配 Spring Cloud Gateway 的响应式模型。

#### 第二层：应用配置（application.yml）

```yaml
feign:
  sentinel:
    enabled: true    # 为 Feign 客户端启用 Sentinel 熔断
```

配置在 4 个业务服务的 `application.yml` 中，启用后 Feign 调用的每个方法都自动成为 Sentinel 资源。

#### 第三层：Java 配置

**熔断降级：** 为每个 Feign 客户端编写 `FallbackFactory`

```java
// 写法：实现 FallbackFactory<T> 接口
@Component
public class SectionClientFallbackFactory implements FallbackFactory<SectionClient> {

    @Override
    public SectionClient create(Throwable cause) {
        // 返回一个匿名实现，方法内编写降级逻辑
        return id -> new ApiResponse<>(-1, "section service unavailable: " + cause.getMessage(), null);
    }
}
```

**网关限流：** 定义 API 分组和限流规则

```java
@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void init() {
        initApiGroups();   // 1. 按路径定义 API 分组
        initGatewayRules(); // 2. 为每个分组设定限流规则
    }

    private void initApiGroups() {
        Set<ApiDefinition> definitions = Set.of(
            new ApiDefinition("auth_api")
                .setPredicateItems(Set.of(
                    new ApiPathPredicateItem()
                        .setPattern("/api/auth/**")
                        .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )),
            // ... post_api, search_api
        );
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = Set.of(
            new GatewayFlowRule("auth_api")
                .setCount(20)        // 20 QPS
                .setIntervalSec(1),
            new GatewayFlowRule("post_api")
                .setCount(50)        // 50 QPS
                .setIntervalSec(1),
            new GatewayFlowRule("search_api")
                .setCount(20)        // 20 QPS
                .setIntervalSec(1)
        );
        GatewayRuleManager.loadRules(rules);
    }
}
```

---

## 三、熔断降级工作流程

### 3.1 正常调用流程

```
CommentService.create(...)
  │
  ├── 1. 参数校验
  ├── 2. 调用 PostClient.getMeta(postId)
  │       │
  │       ├── Feign 构建 HTTP 请求 → 负载均衡 → post-service
  │       │
  │       └── 返回 ApiResponse.ok({ id, authorId, sectionId, title })
  │
  ├── 3. 校验帖子存在 → 创建评论入库
  └── 4. 发布 RabbitMQ 通知消息
```

### 3.2 熔断降级流程

```
CommentService.create(...)
  │
  ├── 1. 参数校验
  ├── 2. 调用 PostClient.getMeta(postId)
  │       │
  │       ├── post-service 不可用（宕机/网络超时）
  │       │
  │       ├── Sentinel 熔断器打开
  │       │
  │       ├── FallbackFactory.create() 被调用
  │       │   └── 返回 ApiResponse(-1, "post service unavailable: ...", null)
  │       │
  │       └── CommentService 收到错误响应
  │
  ├── 3. 判断：响应 code ≠ 0
  │       └── 抛出异常：帖子服务不可用
  │
  └── 4. 请求结束，评论创建失败
```

关键点：**快速失败** — 不等待超时，立即返回降级结果，释放线程资源。

### 3.3 搜索服务的特殊降级

搜索服务是唯一**不直接返回错误**的降级策略：

```java
// PostClientFallbackFactory.java (search-service)
return new PostClient() {
    @Override
    public ApiResponse<List<Map<String, Object>>> search(String q) {
        return ApiResponse.ok(List.of());  // 返回空列表
    }

    @Override
    public ApiResponse<List<String>> suggest(String q) {
        return ApiResponse.ok(List.of());  // 返回空列表
    }
};
```

这样当 post-service 不可用时，搜索功能**仍然可用**，只是搜不到帖子正文内容（搜索结果仅包含已缓存的数据）。这种设计称为**柔性降级**。

### 3.4 FallbackFactory 与 FeignClient 的集成

每个 Feign 客户端通过 `fallbackFactory` 属性关联降级工厂：

```java
@FeignClient(
    name = "bbs-section-service",
    fallbackFactory = SectionClientFallbackFactory.class  // ← 关联降级工厂
)
public interface SectionClient {
    @GetMapping("/api/sections/{id}")
    ApiResponse<Map<String, Object>> getSection(@PathVariable("id") Long id);
}
```

`FallbackFactory` 接收 `Throwable cause` 参数，可以获取失败原因（超时、连接拒绝、熔断等），实现精细化降级。

---

## 四、网关限流工作流程

### 4.1 限流判断流程

```
请求到达网关
  │
  ├── 匹配路由：/api/auth/login → auth 路由
  │
  ├── API 分组匹配：
  │     └── /api/auth/** 匹配 auth_api 分组
  │
  ├── Sentinel 检查 auth_api 的限流规则：
  │     ├── 当前 QPS < 20 → 放行，转发到下游服务
  │     └── 当前 QPS ≥ 20 → 拒绝，返回 429 Too Many Requests
  │
  └── 请求结束
```

### 4.2 限流阈值为什么这样设

| API 分组 | 阈值 | 依据 |
|---------|------|------|
| `auth_api` | 20 QPS | 认证接口是外部暴露的入口，20 QPS 足以应对正常注册/登录流量，防止暴力破解 |
| `post_api` | 50 QPS | 帖子是核心业务，读写频繁，50 QPS 正常业务峰值 |
| `search_api` | 20 QPS | 搜索是计算密集型（数据库 LIKE + 应用层过滤），需要严格保护 |

阈值设定原则：**比数据库能承受的峰值低 30%~50%**，在 Sentinel 层提前拦截，保护后端。

### 4.3 API 分组与路由的区别

网关本身有路由配置（`spring.cloud.gateway.routes`），为什么还要定义 API 分组？

```
路由：  /api/auth/**         → 转发到 bbs-auth-service
                  ↓ 一个路由可能包含多个路径
API 分组： auth_api  →  /api/auth/**
           post_api  →  /api/posts/**
           search_api → /api/search/**
```

路由负责**流量分发**，API 分组负责**流量控制**。一个 API 分组可以跨多个路由，也可以只覆盖某个路由的子集。

---

## 五、对比总结

### 5.1 改造前后对比

| 维度 | 改造前 | 改造后 |
|------|--------|--------|
| 下游服务宕机 | Feign 默认超时等待（60s），线程阻塞 | Sentinel 快速失败（毫秒级），线程释放 |
| 恶意请求 | 无防御，直接打到服务 | 网关限流，超阈值直接拒绝 |
| 降级策略 | 无降级，直接抛异常 | FallbackFactory 兜底返回 |
| 配置方式 | 无 | 代码化配置（`@PostConstruct`） |

### 5.2 与 @SentinelResource 的对比

本项目选择 **FallbackFactory** 而非 `@SentinelResource`：

| 方案 | 作用层面 | 优点 | 缺点 |
|------|---------|------|------|
| `FallbackFactory` | Feign 客户端 | 自动覆盖所有 Feign 方法，无需逐个注解 | 仅对 Feign 调用生效 |
| `@SentinelResource` | 任意方法 | 可作用于任意方法，粒度灵活 | 每个方法都要加注解，需要手动定义 blockHandler/fallback |

本项目所有服务间调用都通过 Feign，因此 `FallbackFactory` 是最合适的选择。

### 5.3 与 Resilience4j / Hystrix 的对比

| 维度 | Hystrix | Resilience4j | Sentinel |
|------|---------|--------------|----------|
| 熔断降级 | ✅ | ✅ | ✅ |
| 网关限流 | ❌ | ❌ | ✅ |
| 动态规则配置 | 需要整合 | 需要整合 | 原生支持 |
| 运维监控 | 需要 Turbine | 需要 Micrometer | 可选 Dashboard |
| Spring Cloud 兼容性 | 维护模式 | 需手动整合 | Alibaba 生态原生支持 |

选择 Sentinel 的核心原因：**网关限流能力 + Spring Cloud Alibaba 生态原生集成**。

---

## 六、验证熔断限流

### 6.1 验证熔断降级

```powershell
# 1. 启动所有服务（确认正常）

# 2. 停止 section-service
# 在 IDE 中停止 bbs-section-service 进程

# 3. 访问需要版块信息的帖子接口
curl http://localhost:8888/api/posts/section/1

# 预期：帖子接口仍然返回正常（帖子和评论数据），但版块信息为空
# 实际：post-service 调用 SectionClient 时触发熔断，返回降级结果

# 4. 重新启动 section-service
# 再次访问，恢复正常
```

### 6.2 验证网关限流

```powershell
# 准备一个压测脚本 bypass-sentinel.ps1:
for ($i=0; $i -lt 50; $i++) {
    curl -X POST http://localhost:8888/api/auth/login `
        -H "Content-Type: application/json" `
        -d '{"username":"user","password":"123"}' `
        -s | Select-String "message"
}

# 运行后观察：
# # 前 20 个请求 → 正常响应（{"code":0,"message":"ok",...}）
# # 后 30 个请求 → 被 Sentinel 限流（HTTP 429）
#
# 因为 auth_api 的限流阈值为 20 QPS
```

### 6.3 查看 Sentinel 日志

```powershell
# 每个被 Sentinel 保护的服务的日志目录
# ${user.home}/logs/csp/

# 查看限流日志
cat ~/logs/csp/bbs-gateway.log
# 可以看到 Blocked 记录
```

---

## 七、代码位置

### 熔断降级

| 文件 | 作用 |
|------|------|
| `bbs-post-service/.../client/SectionClient.java` | 版块服务 Feign 客户端 |
| `bbs-post-service/.../client/SectionClientFallbackFactory.java` | 版块服务降级工厂 |
| `bbs-comment-service/.../client/PostClient.java` | 帖子服务 Feign 客户端 |
| `bbs-comment-service/.../client/PostClientFallbackFactory.java` | 帖子服务降级工厂（返回错误） |
| `bbs-interaction-service/.../client/PostClient.java` | 帖子服务 Feign 客户端 |
| `bbs-interaction-service/.../client/PostClientFallbackFactory.java` | 帖子服务降级工厂（返回错误） |
| `bbs-search-service/.../client/PostClient.java` | 帖子服务 Feign 客户端（含 search/suggest） |
| `bbs-search-service/.../client/PostClientFallbackFactory.java` | 帖子服务降级工厂（**返回空列表**—柔性降级） |
| `bbs-post-service/.../application.yml` | `feign.sentinel.enabled: true` |
| `bbs-comment-service/.../application.yml` | `feign.sentinel.enabled: true` |
| `bbs-interaction-service/.../application.yml` | `feign.sentinel.enabled: true` |
| `bbs-search-service/.../application.yml` | `feign.sentinel.enabled: true` |

### 网关限流

| 文件 | 作用 |
|------|------|
| `bbs-gateway/.../config/GatewaySentinelConfig.java` | 定义 API 分组和限流规则 |
| `bbs-gateway/pom.xml` | `sentinel-spring-cloud-gateway-adapter` 依赖 |
| `bbs-gateway/.../application.yml` | 网关路由配置 |
