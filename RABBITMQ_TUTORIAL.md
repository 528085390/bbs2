# RabbitMQ 消息队列教程

## 一、RabbitMQ 在项目中的作用

### 1.1 为什么需要消息队列

论坛的通知场景存在典型的**异步写**需求：

| 场景 | 触发动作 | 通知目标 | 实时性要求 |
|------|---------|---------|-----------|
| 评论帖子 | 用户发表评论 | 帖子作者 | 秒级可接受 |
| 点赞帖子 | 用户点赞 | 帖子作者 | 秒级可接受 |
| 关注用户 | 用户关注 | 被关注者 | 秒级可接受 |

改造前的方案存在以下问题：

**问题一：同步 Feign 调用耦合**
```
评论服务 → Feign HTTP 调用 → 通知服务 → @Async → MySQL
```
评论服务必须等待通知服务的 HTTP 响应，虽然通知入库是异步的，但 HTTP 请求本身是同步阻塞的。如果通知服务短暂不可用，评论也会失败。

**问题二：@Async 不可靠**
```java
@Async  // 使用 Spring 线程池异步执行
public void push(...) {
    // 如果服务在这里重启，通知直接丢失
    notificationRepository.insert(notification);
}
```
- 服务重启 → 线程池中的任务全部丢失
- 无重试机制 → 失败后不会自动恢复
- 无持久化 → 消息只存在于内存

### 1.2 RabbitMQ 解决了什么

| 问题 | @Async | RabbitMQ |
|------|--------|----------|
| 消息持久化 | ❌ 内存 | ✅ 磁盘（可配置） |
| 自动重试 | ❌ | ✅ 消费者确认机制 |
| 服务解耦 | ❌ Feign 直接调用 | ✅ 通过 Exchange 解耦 |
| 削峰填谷 | ❌ 直接压入线程池 | ✅ 消息堆积在 Queue |
| 消息顺序 | ❌ | ✅ 单队列有序 |

### 1.3 改造了什么

```
改造前:
  CommentService ──Feign HTTP──▶ NotificationService ──@Async──▶ MySQL
  InteractionService ──Feign HTTP──▶ NotificationService ──@Async──▶ MySQL

改造后:
  CommentService ──publish──▶ RabbitMQ ──consume──▶ NotificationService ──▶ MySQL
  InteractionService ──publish──▶ RabbitMQ ──consume──▶ NotificationService ──▶ MySQL
```

---

## 二、生效原理

### 2.1 整体架构

```
                    ┌──────────────────┐
                    │   RabbitMQ       │
                    │   (3.13)         │
                    │                  │
                    │ bbs.notification │
                    │ (Topic Exchange) │
                    └────────┬─────────┘
                             │
                   routing key: "notification.*"
                             │
                    ┌────────▼─────────┐
                    │ bbs.notification │
                    │ .queue           │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │ NotificationConsumer │
                    │ @RabbitListener      │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │ NotificationService │
                    │ push() → MySQL      │
                    └────────────────────┘
```

三个角色：

| 角色 | 服务 | 职责 |
|------|------|------|
| **生产者** | comment-service | 发表评论后发布通知事件 |
| **生产者** | interaction-service | 点赞/收藏/关注后发布通知事件 |
| **消费者** | notification-service | 从队列拉取消息，写入数据库 |

### 2.2 RabbitMQ 核心概念

| 概念 | 本项目中的实例 | 作用 |
|------|--------------|------|
| **Producer** | CommentService / InteractionService | 发布消息到 Exchange |
| **Exchange** | `bbs.notification` (Topic) | 根据 Routing Key 路由消息到 Queue |
| **Routing Key** | `notification.comment` / `notification.interaction` | 消息的路由标签 |
| **Queue** | `bbs.notification.queue` | 存储待消费的消息 |
| **Binding** | `notification.*` → Queue | 将 Exchange 与 Queue 绑定，指定感兴趣的消息 |
| **Consumer** | NotificationConsumer | 从 Queue 拉取并处理消息 |
| **Message** | NotificationEvent (JSON) | 实际传输的数据 |

### 2.3 三层配置

#### 第一层：基础设施（docker-compose.yml）

```yaml
rabbitmq:
  image: rabbitmq:3.13-management
  environment:
    - RABBITMQ_DEFAULT_USER=admin
    - RABBITMQ_DEFAULT_PASS=123456
  ports:
    - "5672:5672"    # AMQP 协议端口
    - "15672:15672"  # 管理后台端口
```

- `5672`：应用程序连接 RabbitMQ 的端口
- `15672`：浏览器访问管理后台的端口（`http://localhost:15672`）
- `management` 标签包含管理插件

> 如果你本地已安装 RabbitMQ，使用默认 `guest/guest` 账号即可，无需启动 Docker 容器。

#### 第二层：应用配置（application.yml）

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:127.0.0.1}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

三个服务（notification / comment / interaction）各自有相同的配置，通过环境变量支持不同环境的切换。

#### 第三层：Java 配置

**消费者端（notification-service）—— 声明 Exchange、Queue、Binding：**

```java
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("bbs.notification");
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue("bbs.notification.queue");
    }

    @Bean
    public Binding notificationBinding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with("notification.*");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

**生产者端（comment-service / interaction-service）—— 配置序列化：**

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
```

`Jackson2JsonMessageConverter` 确保 `NotificationEvent` 对象被自动序列化为 JSON 存入消息体，消费者收到后自动反序列化回 Java 对象。

---

## 三、工作流程

### 3.1 消息发布流程（以评论通知为例）

```
用户发表评论
  │
  ▼
CommentService.create(postId, request)
  │  1. Feign 调用 post-service 验证帖子存在
  │  2. 评论入库
  │
  ├── 判断：评论作者 ≠ 帖子作者？
  │     │
  │     └── true → 发布消息
  │           │
  │           ▼
  │     rabbitTemplate.convertAndSend(
  │       exchange    = "bbs.notification",
  │       routingKey  = "notification.comment",
  │       message     = new NotificationEvent(
  │                       userId    = postAuthorId,
  │                       type      = "COMMENT",
  │                       payload   = "postId=1,commentId=2,fromUserId=3"
  │                     )
  │     )
  │           │
  │           ▼
  │     RabbitMQ 收到消息
  │     TopicExchange 匹配 routingKey "notification.*"
  │     → 投递到绑定的 Queue
  │           │
  │           ▼
  │     Queue 持久化存储消息
  │
  └── 返回评论结果给前端（立即响应，不等待通知处理完成）
```

关键点：**消息发布成功后立即返回**，不需要等待消费者处理完成，实现完全的异步解耦。

### 3.2 消息消费流程

```
NotificationConsumer
  │  @RabbitListener(queues = "bbs.notification.queue")
  │
  ├── RabbitMQ 推送消息给消费者
  │
  ├── Jackson2JsonMessageConverter
  │   自动将 JSON 反序列化为 NotificationEvent
  │
  ├── notificationService.push(event)
  │     ├── 创建 Notification 对象
  │     ├── 设置 userId / type / payload / read
  │     └── notificationRepository.insert() → MySQL
  │
  └── 消息处理成功 → 自动发送 ACK → RabbitMQ 删除消息
```

### 3.3 消息确认机制

默认情况下，Spring AMQP 使用 **auto（自动确认）** 模式：

```
消费者收到消息 → 自动 ACK → RabbitMQ 删除消息
```

如果消费者抛出异常：
```
消费者收到消息 → 处理异常 → RabbitMQ 重新投递消息（默认无限重试）
```

这意味着：
- 如果通知入库失败（如数据库临时不可用），消息会自动重试
- 不会因为一次失败而丢失消息
- 重试间隔由 `spring.rabbitmq.listener.simple.retry` 配置控制

### 3.4 消息持久化

Queue 声明为持久化（默认行为）：

```java
new Queue("bbs.notification.queue");  // durable = true 默认
```

Exchange 同样持久化：

```java
new TopicExchange("bbs.notification");  // durable = true 默认
```

即使 RabbitMQ 重启，未消费的消息仍然保留。

### 3.5 三种通知类型

| 触发场景 | 生产者 | Routing Key | type 字段 | payload 示例 |
|---------|--------|-------------|-----------|-------------|
| 发表评论 | comment-service | `notification.comment` | `COMMENT` | `postId=1,commentId=2,fromUserId=3` |
| 点赞帖子 | interaction-service | `notification.interaction` | `LIKE` | `postId=1,fromUserId=3` |
| 收藏帖子 | interaction-service | `notification.interaction` | `FAVORITE` | `postId=1,fromUserId=3` |
| 关注用户 | interaction-service | `notification.interaction` | `FOLLOW` | `fromUserId=3` |

所有互动通知共用 `notification.interaction` 这个 Routing Key，在消费者端通过 `type` 字段区分具体行为。

---

## 四、代码细节

### 4.1 共享事件模型

```java
// bbs-common/src/main/java/com/li/bbs/common/mq/NotificationEvent.java
public record NotificationEvent(
    Long userId,      // 通知接收方用户 ID
    String type,      // 通知类型：COMMENT / LIKE / FAVORITE / FOLLOW
    String payload    // 通知内容：携带业务关键信息的键值对字符串
) {}
```

`NotificationEvent` 放在 `bbs-common` 中，生产者和消费者共用同一个类，避免重复定义。

### 4.2 生产者发布消息

评论服务（CommentService.java）：
```java
rabbitTemplate.convertAndSend(
    "bbs.notification",       // Exchange 名称
    "notification.comment",   // Routing Key
    new NotificationEvent(     // 消息体（自动 JSON 序列化）
        postAuthorId,
        "COMMENT",
        "postId=" + postId + ",commentId=" + comment.getId() + ",fromUserId=" + request.authorId()
    )
);
```

`convertAndSend` 方法将对象自动转换为 `Message`（包含 JSON 序列化的 body + headers），然后发布到指定 Exchange。

### 4.3 消费者消费消息

```java
@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "bbs.notification.queue")
    public void handleNotification(NotificationEvent event) {
        notificationService.push(event.userId(), event.type(), event.payload());
    }
}
```

`@RabbitListener` 注解使方法成为消息监听器，持续从指定队列拉取消息。方法参数 `NotificationEvent` 由 `Jackson2JsonMessageConverter` 自动反序列化。

---

## 五、验证消息队列

### 5.1 通过管理后台查看

启动服务后，访问 `http://localhost:15672`

```
用户名: guest
密码:   guest
```

在管理后台可以看到：
- **Connections**：三个服务与 RabbitMQ 的连接
- **Channels**：每个连接对应的通道
- **Exchanges**：`bbs.notification` Exchange
- **Queues**：`bbs.notification.queue` 队列（含 Ready / Unacked / Total 消息数）

### 5.2 端到端验证

```powershell
# 注册用户
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"username":"author","password":"123","email":"a@b.com"}'

# 登录获取 token
$token = (curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"author","password":"123"}' | ConvertFrom-Json).data.token

# 创建帖子
$postId = (curl -X POST http://localhost:8080/api/posts `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer $token" `
  -d '{"sectionId":1,"authorId":1,"title":"test","content":"hello"}' | ConvertFrom-Json).data.id

# 模拟另一个用户评论
curl -X POST "http://localhost:8080/api/posts/$postId/comments" `
  -H "Content-Type: application/json" `
  -d '{"authorId":2,"content":"nice post!"}'

# 查看 RabbitMQ 管理后台或检查通知表
```

### 5.3 可靠性验证

停止 notification-service 后发表评论，再重新启动 notification-service：
- 评论应该成功创建（评论服务不需要通知服务可用）
- 消息暂存在 RabbitMQ Queue 中
- 通知服务重启后自动拉取未消费的消息并入库
- 最终用户仍能收到通知（**最终一致性**）

---

## 六、对比总结

| 维度 | @Async 方案 | RabbitMQ 方案 |
|------|------------|--------------|
| 消息持久化 | ❌ | ✅ RabbitMQ 磁盘存储 |
| 服务解耦 | ❌ | ✅ 通过 Exchange/Queue 解耦 |
| 失败重试 | ❌ | ✅ 自动重试 |
| 削峰 | ❌ | ✅ 队列缓冲 |
| 管理监控 | ❌ | ✅ 15672 管理后台 |
| 代码复杂度 | 低 | 中 |
| 运维成本 | 无 | 需维护 RabbitMQ 服务 |

## 七、代码位置

| 文件 | 作用 |
|------|------|
| `docker-compose.yml` | 定义 RabbitMQ 容器 |
| `bbs-common/.../common/mq/NotificationEvent.java` | 通知事件共享 DTO |
| `bbs-notification-service/pom.xml` | `spring-boot-starter-amqp` 依赖 |
| `bbs-notification-service/.../application.yml` | RabbitMQ 连接配置 |
| `bbs-notification-service/.../config/RabbitMQConfig.java` | 声明 Exchange / Queue / Binding |
| `bbs-notification-service/.../consumer/NotificationConsumer.java` | `@RabbitListener` 消息消费者 |
| `bbs-comment-service/pom.xml` | `spring-boot-starter-amqp` 依赖 |
| `bbs-comment-service/.../application.yml` | RabbitMQ 连接配置 |
| `bbs-comment-service/.../config/RabbitMQConfig.java` | RabbitTemplate + JSON 序列化 |
| `bbs-comment-service/.../service/CommentService.java` | 发布评论通知消息 |
| `bbs-interaction-service/pom.xml` | `spring-boot-starter-amqp` 依赖 |
| `bbs-interaction-service/.../application.yml` | RabbitMQ 连接配置 |
| `bbs-interaction-service/.../config/RabbitMQConfig.java` | RabbitTemplate + JSON 序列化 |
| `bbs-interaction-service/.../service/InteractionService.java` | 发布点赞/收藏/关注通知消息 |
