# BBS Microservices (Nacos)

这是论坛后端的微服务版本骨架，包含以下服务：

- `bbs-gateway`：统一入口网关
- `bbs-auth-service`：注册/登录/JWT
- `bbs-user-service`：用户资料
- `bbs-permission-service`：角色与权限校验
- `bbs-section-service`：板块管理
- `bbs-post-service`：帖子管理（发布/编辑/删除/置顶/加精/浏览计数）
- `bbs-comment-service`：评论与回复
- `bbs-interaction-service`：点赞/收藏/关注
- `bbs-notification-service`：通知（异步）
- `bbs-file-service`：文件上传与删除
- `bbs-search-service`：搜索与建议
- `bbs-common`：公共类

## 1. 启动基础设施

```powershell
docker compose up -d
```

默认 Nacos 地址：`127.0.0.1:8848`，MySQL：`127.0.0.1:3306`，Redis：`127.0.0.1:6379`。

## 2. 初始化数据库

```powershell
mysql -uroot -proot < sql/auth/schema.sql
mysql -uroot -proot < sql/auth/seed.sql
mysql -uroot -proot < sql/forum/schema.sql
mysql -uroot -proot < sql/forum/seed.sql
mysql -uroot -proot < sql/notification/schema.sql
mysql -uroot -proot < sql/file/schema.sql
```

## 3. 编译

```powershell
.\mvnw.cmd -DskipTests clean package
```

## 4. 启动服务（最小顺序）

```powershell
.\mvnw.cmd -pl bbs-auth-service spring-boot:run
.\mvnw.cmd -pl bbs-user-service spring-boot:run
.\mvnw.cmd -pl bbs-permission-service spring-boot:run
.\mvnw.cmd -pl bbs-section-service spring-boot:run
.\mvnw.cmd -pl bbs-post-service spring-boot:run
.\mvnw.cmd -pl bbs-comment-service spring-boot:run
.\mvnw.cmd -pl bbs-interaction-service spring-boot:run
.\mvnw.cmd -pl bbs-notification-service spring-boot:run
.\mvnw.cmd -pl bbs-file-service spring-boot:run
.\mvnw.cmd -pl bbs-search-service spring-boot:run
.\mvnw.cmd -pl bbs-gateway spring-boot:run
```

## 5. 通过网关访问

- 注册：`POST http://localhost:8080/api/auth/register`
- 登录：`POST http://localhost:8080/api/auth/login`
- 板块：`GET/POST/PUT/DELETE http://localhost:8080/api/sections`
- 帖子：`POST/GET http://localhost:8080/api/posts`
- 评论：`POST http://localhost:8080/api/posts/{postId}/comments`
- 互动：`POST http://localhost:8080/api/interactions/posts/{postId}/like?userId=1`
- 通知：`GET http://localhost:8080/api/notifications`
- 文件：`POST http://localhost:8080/api/files/avatar` (multipart)
- 搜索：`GET http://localhost:8080/api/search?q=keyword`

## 功能特性

- **多级缓存**：基于 Redis + Spring Cache 的多级缓存，热点帖子、版块、用户资料缓存命中，显著降低 DB 压力
- **服务鉴权**：网关层 JWT 统一鉴权，业务服务通过请求头获取用户上下文
- **服务间通信**：基于 OpenFeign 的声明式服务调用，Nacos 服务发现与负载均衡

## 后续规划

- 将通知异步改造为 MQ（如 RocketMQ / RabbitMQ）
- 引入 Sentinel 实现服务熔断降级与限流
- 容器化各微服务

