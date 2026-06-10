# BBS Microservices

> 基于 Spring Boot 3 + Spring Cloud Alibaba + Nacos 的微服务架构 BBS 论坛后端系统，10 个独立微服务 + 统一网关，提供完整的论坛功能。

## 技术栈

Java 17 / Spring Boot 3.2 / Spring Cloud 2023 / Spring Cloud Alibaba / Nacos / Sentinel / RabbitMQ / Redis / Elasticsearch / MyBatis / MySQL / OpenFeign / SpringDoc OpenAPI / Docker

---

## 架构图

```
                        ┌─────────────────────────────────────────────────┐
                        │          Spring Cloud Gateway (8888)            │
                        │   JWT Auth  │  Sentinel Rate Limit  │  CORS     │
                        └──┬────┬────┬────┬────┬────┬────┬────┬────┬─────┘
                           │    │    │    │    │    │    │    │    │
              ┌────────────┘  ┌─┘  ┌─┘  ┌─┘  ┌─┘  ┌─┘  ┌─┘  ┌─┘  └────────────┐
              ▼               ▼    ▼    ▼    ▼    ▼    ▼    ▼    ▼               ▼
        ┌──────────┐    ┌──────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────────┐ ┌──────┐
        │  Auth    │    │ User │ │Post│ │Sec-│ │Com-│ │Inte│ │Noti│ │  File  │ │Search│
        │ (9001)   │    │(9002)│ │tion│ │tion│ │ment│ │ract│ │fy  │ │(9009)  │ │(9010)│
        └──────────┘    └──────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────────┘ └──────┘
         +Security              ▲         ▲      ▲      ▲                   ▲
                                │ Feign   │Feign │Feign │Feign              │Feign
                                │         │      │      │                   │
                          ┌─────┘    ┌────┘      └──────┘                   │
                     ┌────┴────┐ ┌───┴────────────┐              ┌─────────┴──────────┐
                     │ Redis   │ │  RabbitMQ      │              │  MySQL (FULLTEXT)   │
                     │(7.2)    │ │ (3.13)         │              │                     │
                     │ Caching │ │ Async Events   │              │ Search Index        │
                     └─────────┘ └────────────────┘              └─────────────────────┘
```

---

## 项目结构

| 服务 | 端口 | 职责 |
|------|------|------|
| `bbs-gateway` | 8888 | 统一入口网关：路由转发、JWT 鉴权、Sentinel 限流、CORS |
| `bbs-auth-service` | 9001 | 用户注册、登录、JWT 签发 |
| `bbs-user-service` | 9002 | 用户资料 CRUD |
| `bbs-permission-service` | 9003 | 角色与权限校验 |
| `bbs-section-service` | 9004 | 版块管理 |
| `bbs-post-service` | 9005 | 帖子发布/编辑/删除/置顶/加精/浏览计数 |
| `bbs-comment-service` | 9006 | 评论与回复 |
| `bbs-interaction-service` | 9007 | 点赞/收藏/关注 |
| `bbs-notification-service` | 9008 | 消息通知（RabbitMQ 消费者） |
| `bbs-file-service` | 9009 | 文件上传下载 |
| `bbs-search-service` | 9010 | 全文搜索与搜索建议 |
| `bbs-common` | — | 公共 DTO、工具类 |

---

## 亮点

### 微服务治理体系
基于 Nacos 实现服务注册发现与配置管理，Spring Cloud Gateway 统一入口路由，OpenFeign + LoadBalancer 实现服务间声明式调用与负载均衡，构建完整的微服务治理底座。

### 多级缓存设计
基于 Redis 7.2 + Spring Cache 抽象构建多级缓存，对热点帖子（10min TTL）、版块（30min TTL）、用户资料（60min TTL）差异化缓存。采用失效模式避免缓存与数据库不一致，浏览计数特殊处理绕过缓存防止热点 Key 频繁失效。

### 异步消息解耦
引入 RabbitMQ 替换原有 @Async + Feign 同步调用方案，将评论、点赞、收藏、关注等行为产生的通知通过 Topic Exchange 异步投递。消息持久化保证不丢失，消费者自动 ACK + 重试机制确保最终一致性，评论服务不再依赖通知服务的可用性。

### 服务韧性设计
集成 Sentinel 对 4 个服务间 OpenFeign 调用配置熔断降级，通过 FallbackFactory 实现优雅降级（搜索服务降级返回空结果而非报错）；在网关层对认证（20 QPS）、发帖（50 QPS）、搜索（20 QPS）接口实施差异化限流，防止雪崩效应。

### API 文档标准化
集成 SpringDoc OpenAPI 3.0，10 个微服务自动生成 OpenAPI 规范文档，通过 Swagger UI 提供交互式接口文档，告别手写 API 文档时代。

### Elasticsearch 全文搜索
基于 Elasticsearch 8.x + IK Analysis 中文分词插件构建全文搜索，搜索服务直连 ES 执行 multi_match 查询，支持标题加权（title^3）、内容摘要截取、模糊匹配。帖子创建/更新/删除时由 post-service 写时同步至 ES 索引，MySQL 仍为数据主库，ES 作搜索索引，互不影响。

### 容器化基础设施
Docker Compose 一键启动 Nacos + MySQL 8.0 + Redis 7.2 + RabbitMQ 3.13 四套基础设施，支持环境变量化配置。

---

## 快速启动

```powershell
# 1. 启动基础设施
docker compose up -d

# 2. 初始化数据库
mysql -uroot -proot < sql/auth/schema.sql
mysql -uroot -proot < sql/auth/seed.sql
mysql -uroot -proot < sql/forum/schema.sql
mysql -uroot -proot < sql/forum/seed.sql
mysql -uroot -proot < sql/notification/schema.sql
mysql -uroot -proot < sql/file/schema.sql

# 3. 编译
.\mvnw.cmd -DskipTests clean package

# 4. 启动服务
.\mvnw.cmd -pl bbs-gateway spring-boot:run
# 启动其他服务...
```

---

## 详细文档

- [API 文档](API.md)
- [Redis 缓存教程](REDIS_TUTORIAL.md)
- [RabbitMQ 消息队列教程](RABBITMQ_TUTORIAL.md)
- [Sentinel 熔断限流教程](SENTINEL_TUTORIAL.md)
