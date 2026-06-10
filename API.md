# BBS 论坛系统 API 文档

**基础 URL:** `http://gateway:8888/api`（各微服务直连端口见下文）

**统一响应格式:**

```json
{
  "code": 0,           // 0 成功，-1 失败
  "message": "ok",
  "data": { ... }      // 具体的响应数据
}
```

---

## 1. 认证服务 (bbs-auth-service) — 端口 9001

### POST /api/auth/register

注册新用户。

**请求体:**

```json
{
  "username": "string (3-30 字符, 必填)",
  "email": "string (合法邮箱, 必填)",
  "password": "string (6-100 字符, 必填)"
}
```

**响应:** `ApiResponse<Void>`

### POST /api/auth/login

登录获取 JWT Token。

**请求体:**

```json
{
  "username": "string (必填)",
  "password": "string (必填)"
}
```

**响应:**

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt_string",
    "expiresIn": 3600000
  }
}
```

### GET /api/auth/health

健康检查。

**响应:** `ApiResponse<String>`

---

## 2. 用户服务 (bbs-user-service) — 端口 9002

### GET /api/users/{id}

获取用户资料。

**路径参数:** `id` — 用户 ID

**响应:** `ApiResponse<Map<String, Object>>` （包含 id, displayName, profile 等）

### GET /api/users/me

获取当前登录用户信息（从请求头 `X-User-Id` 识别）。

**请求头:** `X-User-Id: <userId>`

**响应:** `ApiResponse<Map<String, Object>>`

### PUT /api/users/{id}

更新用户资料。

**路径参数:** `id` — 用户 ID

**请求体:**

```json
{
  "displayName": "可选",
  "profile": { ... }
}
```

**响应:** `ApiResponse<?>`

---

## 3. 权限服务 (bbs-permission-service) — 端口 9003

### GET /api/permissions/roles

获取所有角色列表。

**响应:** `ApiResponse<List<String>>` 如 `["ROLE_USER", "ROLE_MOD", "ROLE_ADMIN"]`

### POST /api/permissions/check

检查角色是否有某个操作的权限。

**请求体:**

```json
{
  "role": "ROLE_MOD",
  "action": "delete_post"
}
```

**响应:** `ApiResponse<Boolean>`

---

## 4. 版块服务 (bbs-section-service) — 端口 9004

### GET /api/sections

获取所有版块列表。

**响应:** `ApiResponse<List<Section>>`

### GET /api/sections/{id}

获取单个版块详情。

**路径参数:** `id` — 版块 ID

**响应:** `ApiResponse<Section>`

### POST /api/sections

创建新版块。

**请求体:**

```json
{
  "title": "string (必填)",
  "description": "string",
  "orderIndex": 0,
  "visibility": "PUBLIC"
}
```

**响应:** `ApiResponse<Section>`

### PUT /api/sections/{id}

更新版块。

**路径参数:** `id` — 版块 ID

**请求体:** 同 POST

**响应:** `ApiResponse<Section>`

### DELETE /api/sections/{id}

删除版块。

**路径参数:** `id` — 版块 ID

**响应:** `ApiResponse<Void>`

**Section 实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| title | String | 标题 |
| description | String | 描述 |
| orderIndex | Integer | 排序号 |
| visibility | String | 可见性 (PUBLIC/...) |

---

## 5. 帖子服务 (bbs-post-service) — 端口 9005

### POST /api/posts

创建帖子。

**请求体:**

```json
{
  "sectionId": 1,
  "authorId": 1,
  "title": "string (必填)",
  "content": "string (必填)"
}
```

**响应:** `ApiResponse<Post>`

### GET /api/posts/{id}

获取帖子详情。

**路径参数:** `id` — 帖子 ID

**响应:** `ApiResponse<Post>`

### PUT /api/posts/{id}

更新帖子。

**路径参数:** `id` — 帖子 ID

**请求体:** 同 POST

**响应:** `ApiResponse<Post>`

### DELETE /api/posts/{id}

删除帖子。

**路径参数:** `id` — 帖子 ID

**响应:** `ApiResponse<Void>`

### POST /api/posts/{id}/pin?value=true

置顶/取消置顶帖子。

**路径参数:** `id` — 帖子 ID

**查询参数:** `value` — boolean

**响应:** `ApiResponse<Void>`

### POST /api/posts/{id}/feature?value=true

加精/取消加精帖子。

**路径参数:** `id` — 帖子 ID

**查询参数:** `value` — boolean

**响应:** `ApiResponse<Void>`

### GET /api/posts/{id}/exists

检查帖子是否存在。

**路径参数:** `id` — 帖子 ID

**响应:** `ApiResponse<Boolean>`

### GET /api/posts/{id}/meta

获取帖子元信息。

**路径参数:** `id` — 帖子 ID

**响应:** `ApiResponse<Map<String, Object>>`

### GET /api/posts/section/{sectionId}

获取某版块下的所有帖子。

**路径参数:** `sectionId` — 版块 ID

**响应:** `ApiResponse<List<Post>>`

**Post 实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| sectionId | Long | 所属版块 ID |
| authorId | Long | 作者 ID |
| title | String | 标题 |
| content | String | 内容 |
| pinned | Boolean | 是否置顶 |
| featured | Boolean | 是否加精 |
| viewCount | Long | 浏览数 |
| createdAt | Instant | 创建时间 |
| updatedAt | Instant | 更新时间 |

---

## 6. 评论服务 (bbs-comment-service) — 端口 9006

### POST /api/posts/{postId}/comments

发表评论。

**路径参数:** `postId` — 帖子 ID

**请求体:**

```json
{
  "authorId": 1,
  "parentId": null,
  "content": "string (必填)"
}
```

> `parentId` 为 null 表示顶级评论，否则为回复某条评论的 ID。

**响应:** `ApiResponse<Comment>`

### GET /api/posts/{postId}/comments

获取帖子的所有评论。

**路径参数:** `postId` — 帖子 ID

**响应:** `ApiResponse<List<Comment>>`

### DELETE /api/comments/{id}

删除评论。

**路径参数:** `id` — 评论 ID

**响应:** `ApiResponse<Void>`

**Comment 实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| postId | Long | 所属帖子 ID |
| authorId | Long | 作者 ID |
| parentId | Long | 父评论 ID (null 为顶级) |
| content | String | 内容 |
| depth | Integer | 回复层级 |
| deleted | Boolean | 是否已删除 |
| createdAt | Instant | 创建时间 |

---

## 7. 互动服务 (bbs-interaction-service) — 端口 9007

### POST /api/interactions/posts/{postId}/like?userId=1

点赞帖子。

**路径参数:** `postId` — 帖子 ID

**查询参数:** `userId` — 用户 ID（可选，可从请求头获取）

**请求头:** `X-User-Id: <userId>`

**响应:** `ApiResponse<Void>`

### POST /api/interactions/posts/{postId}/favorite?userId=1

收藏帖子。

**参数:** 同上

**响应:** `ApiResponse<Void>`

### POST /api/interactions/users/{targetUserId}/follow?userId=1

关注用户。

**路径参数:** `targetUserId` — 被关注用户 ID

**查询参数:** `userId` — 当前用户 ID

**响应:** `ApiResponse<Void>`

### GET /api/interactions/users/{targetUserId}/followers

获取某用户的粉丝 ID 列表。

**路径参数:** `targetUserId` — 用户 ID

**响应:** `ApiResponse<Set<Long>>`

### GET /api/interactions/me/followers

获取当前用户的粉丝 ID 列表。

**请求头:** `X-User-Id: <userId>`

**响应:** `ApiResponse<Set<Long>>`

---

## 8. 通知服务 (bbs-notification-service) — 端口 9008

### GET /api/notifications?userId=1

获取用户的通知列表。

**查询参数:** `userId` — 用户 ID

**响应:** `ApiResponse<List<Notification>>`

### POST /api/notifications/system

发送系统通知。

**请求体:**

```json
{
  "userId": 1,
  "type": "string (必填)",
  "payload": "string"
}
```

**响应:** `ApiResponse<Void>`

**Notification 实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 接收用户 ID |
| type | String | 通知类型 |
| payload | String | 通知内容 (JSON) |
| read | Boolean | 是否已读 |
| createdAt | Instant | 创建时间 |

---

## 9. 文件服务 (bbs-file-service) — 端口 9009

### POST /api/files/avatar

上传头像。

**请求格式:** `multipart/form-data`

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| file | MultipartFile | 头像文件 (必填) |
| ownerId | Long | 拥有者 ID (可选) |

**响应:** `ApiResponse<FileMeta>`

### POST /api/files/posts/{postId}/image

上传帖子图片。

**路径参数:** `postId` — 帖子 ID

**请求格式:** `multipart/form-data`

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| file | MultipartFile | 图片文件 (必填) |
| ownerId | Long | 拥有者 ID (可选) |

**响应:** `ApiResponse<FileMeta>`

### GET /api/files/{id}

获取文件元信息。

**路径参数:** `id` — 文件 ID

**响应:** `ApiResponse<FileMeta>`

### DELETE /api/files/{id}

删除文件。

**路径参数:** `id` — 文件 ID

**响应:** `ApiResponse<Void>`

**FileMeta 实体字段:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| ownerType | String | 拥有者类型 (avatar/post_image/...) |
| ownerId | Long | 拥有者 ID |
| filename | String | 原始文件名 |
| storagePath | String | 存储路径 |
| contentType | String | MIME 类型 |
| size | Long | 文件大小 (字节) |
| createdAt | Instant | 创建时间 |

---

## 10. 搜索服务 (bbs-search-service) — 端口 9010

### GET /api/search?q=keyword&userId=1

全文搜索帖子。

**查询参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| q | String | 搜索关键词 (必填) |
| userId | Long | 用户 ID (可选，用于记录搜索历史) |

**响应:** `ApiResponse<List<Map<String, Object>>>`

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "postId": 1,
      "title": "匹配的标题",
      "snippet": "内容片段..."
    }
  ]
}
```

### GET /api/search/suggest?q=keyword

搜索建议（基于搜索历史和帖子标题）。

**查询参数:** `q` — 关键词 (必填)

**响应:** `ApiResponse<List<String>>`

```json
{
  "code": 0,
  "message": "ok",
  "data": ["建议1", "建议2"]
}
```

---

## 服务端口汇总

| 服务 | 端口 |
|------|------|
| bbs-gateway | 8888 |
| bbs-auth-service | 9001 |
| bbs-user-service | 9002 |
| bbs-permission-service | 9003 |
| bbs-section-service | 9004 |
| bbs-post-service | 9005 |
| bbs-comment-service | 9006 |
| bbs-interaction-service | 9007 |
| bbs-notification-service | 9008 |
| bbs-file-service | 9009 |
| bbs-search-service | 9010 |

> 所有服务均通过 Gateway (8888) 统一暴露，路径前缀为 `/api`。直连时使用各自端口。
