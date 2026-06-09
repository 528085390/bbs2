# Redis 缓存教程

## 一、Redis 在项目中的作用

### 1.1 为什么需要缓存

论坛业务存在明显的**冷热数据**分布：

| 场景 | 读写比例 | 特点 |
|------|---------|------|
| 帖子详情 | 读多写少 | 热门帖子被大量用户反复查看 |
| 版块列表 | 极少变化 | 版块增删改极少发生 |
| 用户资料 | 读多写少 | 用户查看他人资料频繁，但修改不频繁 |
| 评论列表 | 读多写少 | 帖子评论被反复阅读 |

每次请求都查询 MySQL 会导致：
- 数据库连接被快速耗尽
- 相同数据被反复查询，浪费 IO
- 高并发下数据库成为瓶颈

Redis 作为**内存级缓存**，查询速度是 MySQL 的 **10~100 倍**（微秒级 vs 毫秒级），能有效吸收热点请求。

### 1.2 缓存了什么

| 缓存内容 | 缓存 Key | TTL | 所属服务 |
|---------|----------|-----|---------|
| 帖子详情 | `bbs:posts:{id}` | 10 分钟 | bbs-post-service |
| 版块下的帖子列表 | `bbs:posts:section:{sectionId}` | 5 分钟 | bbs-post-service |
| 版块列表 | `bbs:sections:list` | 30 分钟 | bbs-section-service |
| 版块详情 | `bbs:sections:{id}` | 30 分钟 | bbs-section-service |
| 用户资料 | `bbs:users:{id}` | 60 分钟 | bbs-user-service |

TTL（过期时间）按数据变化频率设定：
- **帖子**变化较快（被编辑、置顶、加精），TTL 较短
- **版块**极少变化，TTL 较长
- **用户资料**也较少变化，TTL 最长

---

## 二、生效原理

### 2.1 整体架构

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│  Controller  │────▶│   Service    │────▶│   Repository │────▶  MySQL
└──────────┘     │  (@Cacheable) │     └──────────┘
                 │               │
                 │  ┌─────────┐  │
                 │  │  Redis  │◀─┼──── 先查缓存，命中则返回
                 │  └─────────┘  │     未命中则查 DB 并写入缓存
                 └──────────────┘
```

核心机制：**Spring Cache 抽象** + **Spring Data Redis**。

### 2.2 Spring Cache 抽象

Spring Cache 是一套**声明式缓存注解**，不绑定具体缓存实现：

| 注解 | 作用 |
|------|------|
| `@Cacheable` | 方法执行前先查缓存，命中直接返回；未命中执行方法并将返回值写入缓存 |
| `@CacheEvict` | 方法执行后清除指定缓存 |
| `@CachePut` | 方法执行后将返回值更新到缓存 |
| `@Caching` | 组合多个缓存注解 |

### 2.3 三层配置

#### 第一层：基础设施（docker-compose.yml）

```yaml
redis:
  image: redis:7.2
  ports:
    - "6379:6379"
```

Redis 7.2 容器，暴露 6379 端口。

#### 第二层：应用配置（application.yml）

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:123456}
```

环境变量化配置，默认连接本地 Redis。

#### 第三层：Java 配置（RedisCacheConfig.java）

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        var serializer = new GenericJackson2JsonRedisSerializer();
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(...)   // 使用 StringRedisSerializer
                .serializeValuesWith(...) // 使用 JSON 序列化，替代 JDK 序列化
                .disableCachingNullValues(); // 不缓存 null，防止缓存穿透

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(Map.of(
                        "bbs:posts", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                        "bbs:posts:section", defaultConfig.entryTtl(Duration.ofMinutes(5))
                ))
                .build();
    }
}
```

三个服务各自有一份 `RedisCacheConfig`，按业务特点设定不同的 TTL。

**关键点**：
- `@EnableCaching`：启用 Spring Cache 注解功能
- `GenericJackson2JsonRedisSerializer`：使用 JSON 格式存储，可读性强，避免 JDK 序列化的 `Serializable` 依赖
- `disableCachingNullValues()`：防止缓存空值导致缓存穿透

---

## 三、工作流程

### 3.1 读缓存流程（以帖子详情为例）

```
GET /api/posts/{id}
  │
  ▼
PostController.get(id)
  │
  ▼
PostService.get(id)
  │  @Cacheable(value = "bbs:posts", key = "#id")
  │
  ├──▶ Redis 查询 Key = "bbs:posts::1"
  │     │
  │     ├── 命中 → 直接返回缓存的 Post 对象（反序列化）
  │     │
  │     └── 未命中 → 执行方法体
  │                   │
  │                   ▼
  │             PostRepository.selectById(1)  → 查 MySQL
  │                   │
  │                   ▼
  │             返回 Post 对象
  │             ↓ 自动写入 Redis（序列化为 JSON）
  │
  ▼
返回 ApiResponse.ok(post)
```

### 3.2 写失效流程（以更新帖子为例）

```
PUT /api/posts/{id}
  │
  ▼
PostService.update(id, request)
  │  @Caching(evict = {
  │      @CacheEvict("bbs:posts", key = "#id"),
  │      @CacheEvict("bbs:posts:section", allEntries = true)
  │  })
  │
  ├──▶ 执行方法体
  │     ├── PostRepository.selectById(id) 查库
  │     ├── 更新字段
  │     └── PostRepository.update(post) 写库
  │
  ├──▶ 缓存失效（方法执行后触发）
  │     ├── 删除 Redis Key "bbs:posts::1"
  │     └── 删除 Redis Key "bbs:posts:section::*"
  │
  ▼
下次读取时缓存未命中 → 重新查库 → 重新缓存
```

### 3.3 为什么用失效而不是更新

写操作后**清除缓存**而非直接更新，原因：
1. 更新后的数据格式可能与缓存格式不同（如关联查询）
2. 避免并发写时缓存与数据库不一致
3. 简单可靠，下次读取时自动回填

### 3.4 浏览计数特殊处理

```java
public Post getAndIncreaseView(Long id) {
    Post post = postRepository.selectById(id);
    postRepository.increaseViewCount(id);
    post.setViewCount(post.getViewCount() + 1);
    return post;
}
```

浏览计数的特点是**每次读取都触发写**。如果每次增加计数都失效缓存，热门帖子会不断穿透到数据库，失去缓存意义。因此：
- `getAndIncreaseView` **不走缓存**，每次直接读库
- `get`（纯查询）**走缓存**，`@Cacheable` 注解
- 浏览数有短时间的滞后，但对论坛业务是可接受的

### 3.5 TTL 过期策略

每个缓存 Key 都设置了 TTL（Time-To-Live），到期自动删除：
- 防止冷数据长期占用内存
- 被动兜底：即使写失效遗漏，TTL 到期后也会自动刷新

---

## 四、缓存设计要点

### 4.1 缓存穿透

**问题**：查询一个不存在的 ID（如 `GET /api/posts/99999`），每次都穿透到数据库。

**防范**：`disableCachingNullValues()` + 业务层判断。如果帖子不存在，直接抛异常，不缓存 null。

### 4.2 缓存雪崩

**问题**：大量缓存同时过期，请求全部打到数据库。

**防范**：
- 不同业务设置不同 TTL（帖子 10min、用户 60min）
- TTL 分散自然避免同时过期

### 4.3 缓存击穿

**问题**：热点 Key 在过期瞬间被大量并发请求同时穿透。

**防范**：Spring Cache 的 `@Cacheable` 底层通过 `synchronized` 保证单个 Key 的查询在未命中时只有一个线程查库，其他线程等待。

---

## 五、验证缓存生效

启动服务后，调用接口两次，观察响应时间差异：

```powershell
# 第一次（未命中缓存）
curl http://localhost:8080/api/posts/1
# 响应时间 ≈ 50ms（查 MySQL）

# 第二次（命中缓存）
curl http://localhost:8080/api/posts/1
# 响应时间 ≈ 2ms（查 Redis）
```

也可以直接查 Redis 验证：

```powershell
redis-cli -a 123456
127.0.0.1:6379> keys bbs:*
127.0.0.1:6379> get "bbs:posts::1"
```

---

## 六、在项目中的代码位置

| 文件 | 作用 |
|------|------|
| `docker-compose.yml` | 定义 Redis 容器 |
| `bbs-post-service/src/main/resources/application.yml` | Redis 连接配置 |
| `bbs-section-service/src/main/resources/application.yml` | Redis 连接配置 |
| `bbs-user-service/src/main/resources/application.yml` | Redis 连接配置 |
| `bbs-post-service/.../config/RedisCacheConfig.java` | 帖子服务缓存管理器和 TTL |
| `bbs-section-service/.../config/RedisCacheConfig.java` | 版块服务缓存管理器和 TTL |
| `bbs-user-service/.../config/RedisCacheConfig.java` | 用户服务缓存管理器和 TTL |
| `bbs-post-service/.../service/PostService.java` | 帖子缓存注解（`@Cacheable` / `@Caching` / `@CacheEvict`） |
| `bbs-section-service/.../service/SectionService.java` | 版块缓存注解 |
| `bbs-user-service/.../service/UserService.java` | 用户缓存注解 |
