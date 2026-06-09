# BBS API 文档

> 基于 Spring Cloud Gateway 微服务架构的论坛系统
>
> **基础路径**: `http://<gateway-host>:8888`
>
> **统一响应格式** (`ApiResponse<T>`):
> ```json
> { "code": 0, "message": "ok", "data": <T> }
> ```
> - `code: 0` 成功, `code: -1` 业务失败, `code: 401` 未授权
>
> **认证方式**: `Authorization: Bearer <token>` (除 `/api/auth/**` 和 `/api/sections` 外默认需要认证)
>
> **认证请求头** (网关自动注入下游):
> - `X-User-Id`: 当前登录用户 ID
> - `X-Username`: 当前登录用户名

---

## 1. 认证服务 (`bbs-auth-service`)

### 1.1 用户注册

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "username": "string (3-30字符, 必填)",
  "email": "string (邮箱格式, 必填)",
  "password": "string (6-100字符, 必填)"
}
```

**Response:** `ApiResponse<Void>`

### 1.2 用户登录

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "username": "string (必填)",
  "password": "string (必填)"
}
```

**Response:** `ApiResponse<LoginResponse>`
```json
{
  "token": "string (JWT令牌)",
  "expiresIn": "number (过期时间戳, 毫秒)"
}
```

### 1.3 健康检查

```
GET /api/auth/health
```

**Response:** `ApiResponse<string>` → `"auth-up"`

---

## 2. 用户服务 (`bbs-user-service`)

### 2.1 获取用户信息

```
GET /api/users/{id}
```

**Path 参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |

**Response:** `ApiResponse<Map<String, Object>>`
```json
{
  "id": "number",
  "displayName": "string",
  "...":"其他profile字段 (动态JSON)"
}
```

### 2.2 获取当前登录用户

```
GET /api/users/me
```

**请求头:** `Authorization: Bearer <token>`

**Response:** `ApiResponse<Map<String, Object>>` (同上，未登录返回 401)

### 2.3 更新用户信息

```
PUT /api/users/{id}
```

**Path 参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 ID |

**Request Body (JSON object, 可选):**
```json
{
  "displayName": "string (显示名称, 可选)",
  "...": "其他自定义profile字段"
}
```

**Response:** `ApiResponse<Map<String, Object>>` (同 GET 响应格式)

---

## 3. 帖子服务 (`bbs-post-service`)

### 3.1 创建帖子

```
POST /api/posts
```

**Request Body:**
```json
{
  "sectionId": "number (必填)",
  "authorId": "number (必填)",
  "title": "string (必填)",
  "content": "string (必填)"
}
```

**Response:** `ApiResponse<Post>`
```json
{
  "id": "number",
  "sectionId": "number",
  "authorId": "number",
  "title": "string",
  "content": "string",
  "pinned": "boolean",
  "featured": "boolean",
  "viewCount": "number",
  "createdAt": "string (ISO-8601)",
  "updatedAt": "string (ISO-8601)"
}
```

### 3.2 获取帖子详情（增加浏览数）

```
GET /api/posts/{id}
```

**Response:** `ApiResponse<Post>`

### 3.3 更新帖子

```
PUT /api/posts/{id}
```

**Request Body:** 同创建帖子

**Response:** `ApiResponse<Post>`

### 3.4 删除帖子

```
DELETE /api/posts/{id}
```

**Response:** `ApiResponse<Void>`

### 3.5 置顶/取消置顶帖子

```
POST /api/posts/{id}/pin?value=true
```

**Query 参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| value | boolean | `true` 置顶, `false` 取消置顶 |

**Response:** `ApiResponse<Void>`

### 3.6 精华/取消精华帖子

```
POST /api/posts/{id}/feature?value=true
```

**Query 参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| value | boolean | `true` 设为精华, `false` 取消精华 |

**Response:** `ApiResponse<Void>`

### 3.7 检查帖子是否存在

```
GET /api/posts/{id}/exists
```

**Response:** `ApiResponse<boolean>`

### 3.8 获取帖子元信息

```
GET /api/posts/{id}/meta
```

**Response:** `ApiResponse<Map<String, Object>>`
```json
{
  "id": "number",
  "authorId": "number",
  "sectionId": "number",
  "title": "string"
}
```

### 3.9 按版块获取帖子列表

```
GET /api/posts/section/{sectionId}
```

**Response:** `ApiResponse<List<Post>>`

### 3.10 内部搜索帖子

```
GET /api/posts/internal/search?q=<keyword>
```

**用途:** 其他微服务内部调用

**Response:** `ApiResponse<List<Map<String, Object>>>`
```json
[
  {
    "id": "number",
    "title": "string",
    "content": "string"
  }
]
```

### 3.11 内部搜索建议

```
GET /api/posts/internal/suggest?q=<keyword>
```

**Response:** `ApiResponse<List<string>>`

---

## 4. 评论服务 (`bbs-comment-service`)

### 4.1 创建评论

```
POST /api/posts/{postId}/comments
```

**Path 参数:**
| 参数 | 类型 | 说明 |
|------|------|------|
| postId | Long | 帖子 ID |

**Request Body:**
```json
{
  "authorId": "number (必填)",
  "parentId": "number (可选, 回复的评论ID)",
  "content": "string (必填)"
}
```

**Response:** `ApiResponse<Comment>`
```json
{
  "id": "number",
  "postId": "number",
  "authorId": "number",
  "parentId": "number (可为null)",
  "content": "string",
  "depth": "number (0=根评论, >0=回复)",
  "deleted": "boolean",
  "createdAt": "string (ISO-8601)"
}
```

### 4.2 获取帖子评论列表

```
GET /api/posts/{postId}/comments
```

**Response:** `ApiResponse<List<Comment>>`

### 4.3 删除评论

```
DELETE /api/comments/{id}
```

**Response:** `ApiResponse<Void>`

---

## 5. 版块服务 (`bbs-section-service`)

### 5.1 获取版块列表

```
GET /api/sections
```

**Response:** `ApiResponse<List<Section>>`
```json
[
  {
    "id": "number",
    "title": "string",
    "description": "string (可为null)",
    "orderIndex": "number",
    "visibility": "string (PUBLIC/PRIVATE, 默认PUBLIC)"
  }
]
```

### 5.2 获取版块详情

```
GET /api/sections/{id}
```

**Response:** `ApiResponse<Section>`

### 5.3 创建版块

```
POST /api/sections
```

**Request Body:**
```json
{
  "title": "string (必填)",
  "description": "string (可选)",
  "orderIndex": "number (可选, 默认0)",
  "visibility": "string (可选, 默认PUBLIC)"
}
```

**Response:** `ApiResponse<Section>`

### 5.4 更新版块

```
PUT /api/sections/{id}
```

**Request Body:** 同创建版块

**Response:** `ApiResponse<Section>`

### 5.5 删除版块

```
DELETE /api/sections/{id}
```

**Response:** `ApiResponse<Void>`

---

## 6. 互动服务 (`bbs-interaction-service`)

### 6.1 点赞帖子

```
POST /api/interactions/posts/{postId}/like?userId=<userId>
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| postId | Path | Long | 帖子 ID |
| userId | Query | Long | 可选, 不传则从请求头获取 |

**Response:** `ApiResponse<Void>`

### 6.2 收藏帖子

```
POST /api/interactions/posts/{postId}/favorite?userId=<userId>
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| postId | Path | Long | 帖子 ID |
| userId | Query | Long | 可选 |

**Response:** `ApiResponse<Void>`

### 6.3 关注用户

```
POST /api/interactions/users/{targetUserId}/follow?userId=<userId>
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| targetUserId | Path | Long | 被关注用户 ID |
| userId | Query | Long | 可选 |

**Response:** `ApiResponse<Void>`

### 6.4 获取用户粉丝列表

```
GET /api/interactions/users/{targetUserId}/followers
```

**Response:** `ApiResponse<Set<Long>>` (用户 ID 集合)

### 6.5 获取当前登录用户粉丝列表

```
GET /api/interactions/me/followers
```

**请求头:** `Authorization: Bearer <token>`

**Response:** `ApiResponse<Set<Long>>` (用户 ID 集合, 未登录返回 401)

---

## 7. 文件服务 (`bbs-file-service`)

### 7.1 上传头像

```
POST /api/files/avatar
```

**Content-Type:** `multipart/form-data`

| 参数 | 类型 | 说明 |
|------|------|------|
| file | MultipartFile | 头像文件 (必填) |
| ownerId | Long | 所有者 ID (可选) |

**Response:** `ApiResponse<FileMeta>`
```json
{
  "id": "number",
  "ownerType": "string (AVATAR)",
  "ownerId": "number",
  "filename": "string",
  "storagePath": "string",
  "contentType": "string",
  "size": "number (字节)",
  "createdAt": "string (ISO-8601)"
}
```

### 7.2 上传帖子图片

```
POST /api/files/posts/{postId}/image
```

**Content-Type:** `multipart/form-data`

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| postId | Path | Long | 帖子 ID |
| file | RequestPart | MultipartFile | 图片文件 (必填) |
| ownerId | Query | Long | 可选 |

**Response:** `ApiResponse<FileMeta>`

### 7.3 获取文件信息

```
GET /api/files/{id}
```

**Response:** `ApiResponse<FileMeta>`

### 7.4 删除文件

```
DELETE /api/files/{id}
```

**Response:** `ApiResponse<Void>`

---

## 8. 搜索服务 (`bbs-search-service`)

### 8.1 搜索

```
GET /api/search?q=<keyword>&userId=<userId>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| q | string | 搜索关键词 (必填) |
| userId | Long | 用户 ID (可选, 用于记录搜索历史) |

**Response:** `ApiResponse<List<Map<String, Object>>>`

### 8.2 搜索建议

```
GET /api/search/suggest?q=<keyword>
```

**Response:** `ApiResponse<List<string>>`

---

## 9. 通知服务 (`bbs-notification-service`)

### 9.1 获取通知列表

```
GET /api/notifications?userId=<userId>
```

| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID (可选) |

**Response:** `ApiResponse<List<Notification>>`
```json
[
  {
    "id": "number",
    "userId": "number",
    "type": "string",
    "payload": "string (可为null)",
    "read": "boolean",
    "createdAt": "string (ISO-8601)"
  }
]
```

### 9.2 发送系统通知

```
POST /api/notifications/system
```

**Request Body:**
```json
{
  "userId": "number (必填)",
  "type": "string (必填)",
  "payload": "string (可选)"
}
```

**Response:** `ApiResponse<Void>`

### 9.3 内部评论通知

```
POST /api/notifications/internal/comment
```

**用途:** 微服务内部调用

**Request Body:**
```json
{
  "userId": "number",
  "type": "string",
  "payload": "string"
}
```

**Response:** `ApiResponse<Void>`

### 9.4 内部互动通知

```
POST /api/notifications/internal/interaction
```

**用途:** 微服务内部调用

**Request Body:**
```json
{
  "userId": "number",
  "type": "string",
  "payload": "string"
}
```

**Response:** `ApiResponse<Void>`

---

## 10. 权限服务 (`bbs-permission-service`)

### 10.1 获取角色列表

```
GET /api/permissions/roles
```

**Response:** `ApiResponse<List<string>>`
```json
["ROLE_USER", "ROLE_MOD", "ROLE_ADMIN"]
```

### 10.2 检查权限

```
POST /api/permissions/check
```

**Request Body:**
```json
{
  "role": "string (可选, 默认ROLE_USER)",
  "action": "string (可选, 默认read)"
}
```

**Response:** `ApiResponse<boolean>`
- `read` 动作始终允许
- `ROLE_ADMIN` / `ROLE_MOD` 允许所有动作
- 其他角色根据具体行为判断

---

## 网关路由一览

| 服务 | 路由前缀 | 公开路径 |
|------|----------|----------|
| bbs-auth-service | `/api/auth/**` | 是 |
| bbs-user-service | `/api/users/**` | 否 |
| bbs-permission-service | `/api/permissions/**` | 否 |
| bbs-section-service | `/api/sections/**` | **是** |
| bbs-comment-service | `/api/posts/{postId}/comments/**`, `/api/comments/**` | 否 |
| bbs-post-service | `/api/posts/**` | 否 |
| bbs-interaction-service | `/api/interactions/**` | 否 |
| bbs-notification-service | `/api/notifications/**` | 否 |
| bbs-file-service | `/api/files/**` | 否 |
| bbs-search-service | `/api/search/**` | 否 |

> 公开路径（无需 JWT 认证）: `/api/auth/`, `/api/sections`
> OPTIONS 预检请求所有路径均放行
