
```markdown
# Blog 博客系统 —— 开发逻辑总结文档

> **项目名称**：奇点日记（Blog）
> **项目定位**：基于 Spring Boot 的轻量级博客系统，支持用户注册登录、文章发布与评论互动。
> **技术栈**：Java 17 + Spring Boot 3.3 + MyBatis-Plus + Spring Security + JWT + MySQL + Vue 3 + Element Plus

---

## 目录

1. [开发起点与流程](#1-开发起点与流程)
2. [包结构说明](#2-包结构说明)
3. [核心功能模块](#3-核心功能模块)
4. [技术栈与配置](#4-技术栈与配置)
5. [API 一览](#5-api-一览)
6. [前端界面说明](#6-前端界面说明)

---

## 1. 开发起点与流程

整个项目的开发遵循 **自底向上** 的分层架构思路，推荐开发顺序如下：

```

数据模型层（Entity） → 数据访问层（Mapper） → 业务逻辑层（Service/Impl）
→ 控制层（Controller） → 前端界面（静态 HTML / Vue SPA）
```
### 1.1 数据库初始化

**起点**：首先规划数据库表结构。项目提供两个 SQL 文件：

- **`src/main/resources/schema.sql`**：完整的建表脚本，包含 `user`、`category`、`article`、`comment` 四张表，并插入默认管理员账号 `root / 123456`。
- **`src/main/resources/db_migration.sql`**：增量迁移脚本（v2.0），用于在已有数据库上追加 `role` 字段和初始管理员数据，所有操作均为幂等。

**DDL 自动初始化**：项目还通过 `DataInitializer.java`（`@PostConstruct`）在应用启动时自动检测并补齐 `user.role` 列和 `root` 管理员账号，实现双保险的幂等初始化策略。

### 1.2 实体类（Entity）

根据数据库表定义对应的 Java POJO 类，使用 **Lombok `@Data`** 简化代码，配合 **MyBatis-Plus 注解** 完成 ORM 映射。

| 实体类 | 对应表 | 关键字段 |
|--------|--------|---------|
| `User` | `user` | id, username, password, email, role, createTime |
| `Article` | `article` | id, title, content, summary, categoryId, userId, createTime, updateTime |
| `Category` | `category` | id, name |
| `Comment` | `comment` | id, articleId, userId, content, createTime |

**自动填充策略**：通过 `MyMetaObjectHandler`（实现 MyBatis-Plus 的 `MetaObjectHandler` 接口），在插入时自动填充 `createTime`，在更新时自动填充 `updateTime`。

### 1.3 数据访问层（Mapper）

每个实体类对应一个 Mapper 接口，继承 `BaseMapper<T>` 即可获得基础的 CRUD 方法，无需编写任何 XML 映射文件。

```
java
// 示例：ArticleMapper
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {}
```
在启动类 `BlogApplication` 上通过 `@MapperScan("com.nuist.blog.mapper")` 一次性扫描所有 Mapper。

### 1.4 业务逻辑层（Service / Impl）

分为 **接口（Interface）** 和 **实现类（Impl）** 两层，遵循面向接口编程的原则。

- **Service 接口**：定义业务方法签名，如 `ArticleService` 中的 `create`、`page`、`getById`、`update`、`delete`。
- **ServiceImpl 实现类**：注入 Mapper 和其他依赖，实现具体的业务逻辑，如权限校验、数据组装、异常抛出等。

### 1.5 控制层（Controller）

作为 RESTful API 的入口，接收 HTTP 请求并调用 Service 层。所有 Controller 统一返回 `Result<T>` 包装对象，实现前后端数据格式一致。

### 1.6 前端界面

项目的前端是一个 **单页应用（SPA）**，内嵌于 `src/main/resources/static/index.html` 中，采用 **Vue 3 Composition API + Element Plus CDN** 方式开发，不依赖前端构建工具（如 Vite / Webpack），直接由 Spring Boot 作为静态资源提供服务。

---

## 2. 包结构说明

```

com.nuist.blog
├── common/          # 公共工具与基础设施
│   ├── BusinessException.java      # 自定义业务异常（含 code + message）
│   ├── GlobalExceptionHandler.java # 全局异常处理器（@RestControllerAdvice）
│   ├── JwtUtil.java                # JWT 令牌工具类（生成/解析/校验）
│   ├── Result.java                 # 统一 API 响应封装（code + message + data）
│   └── UserPrincipal.java          # 自定义认证主体（封装 userId + username + role）
│
├── config/          # Spring Boot 配置类
│   ├── DataInitializer.java        # 数据库初始化器（幂等初始化 role 列 + 管理员账号）
│   ├── MyMetaObjectHandler.java    # MyBatis-Plus 自动填充处理器（createTime/updateTime）
│   ├── MybatisPlusConfig.java      # MyBatis-Plus 配置（分页插件）
│   └── SecurityConfig.java         # Spring Security 配置（JWT 过滤器链 + 密码编码器）
│
├── controller/      # Web 控制层（RESTful API 入口）
│   ├── ArticleController.java      # 文章 CRUD 接口
│   ├── CommentController.java       # 评论管理接口
│   └── UserController.java         # 用户注册/登录/信息查询接口
│
├── dto/             # 数据传输对象（请求/响应封装）
│   ├── ArticleCreateRequest.java   # 创建/更新文章请求体
│   ├── ArticleQueryRequest.java    # 文章分页查询参数（categoryId, keyword, page, size）
│   ├── ArticleVO.java              # 文章视图对象（继承 Article，附加 authorName）
│   ├── CommentCreateRequest.java   # 创建评论请求体
│   ├── CommentVO.java              # 评论视图对象（继承 Comment，附加 username）
│   ├── LoginRequest.java           # 登录请求体
│   ├── LoginResponse.java          # 登录响应（User + JWT token）
│   └── RegisterRequest.java        # 注册请求体
│
├── entity/          # 实体类（与数据库表一一映射）
│   ├── Article.java
│   ├── Category.java
│   ├── Comment.java
│   └── User.java
│
├── mapper/          # MyBatis-Plus 数据访问接口
│   ├── ArticleMapper.java
│   ├── CategoryMapper.java
│   ├── CommentMapper.java
│   └── UserMapper.java
│
├── service/         # 业务逻辑接口
│   ├── ArticleService.java
│   ├── CommentService.java
│   └── UserService.java
│
└── service/impl/    # 业务逻辑实现类
├── ArticleServiceImpl.java
├── CommentServiceImpl.java
└── UserServiceImpl.java
```
### 各层职责定位

| 层次 | 职责 | 典型操作 |
|------|------|---------|
| **Entity** | 数据模型映射，与数据库表一一对应 | 定义字段、MyBatis-Plus 注解 |
| **Mapper** | 数据访问层，提供基础的数据库 CRUD | 继承 `BaseMapper`，无需写 SQL |
| **Service** | 业务逻辑层，封装核心业务规则 | 参数校验、权限判断、数据组装 |
| **Controller** | 控制层，接收 HTTP 请求，调用 Service | 参数解析、路由映射、调用 Service |
| **DTO** | 数据传输，解耦请求/响应与实体 | 按需定义字段，避免暴露实体敏感信息 |
| **Common** | 公共基础设施 | 异常处理、JWT 工具、统一响应 |
| **Config** | 框架配置与初始化 | 安全配置、分页插件、数据初始化 |

---

## 3. 核心功能模块

### 3.1 用户注册与登录（JWT 认证）

**注册** (`POST /api/users/register`)
- 前端提交 `RegisterRequest`（username, password, email）
- `UserServiceImpl.register()` 校验用户名是否已存在，若存在则抛 `BusinessException(400, "用户名已存在")`
- 密码使用 `BCryptPasswordEncoder` 加密存储
- 新注册用户默认角色为 `USER`

**登录** (`POST /api/users/login`)
- 前端提交 `LoginRequest`（username, password）
- 校验用户名和密码（通过 `PasswordEncoder.matches()`）
- 校验通过后，使用 `JwtUtil.generateToken()` 生成 JWT，其中 payload 包含 `userId`、`username`、`role`
- 返回 `LoginResponse`（用户信息 + JWT token）

**JWT 认证流程**
1. 用户在登录成功后获取 JWT token，前端将其存储在 `localStorage` 中
2. 每次请求受保护接口时，前端在 `Authorization` 头携带 `Bearer <token>`
3. `SecurityConfig.JwtAuthenticationFilter` 从请求头中提取 token
4. 解析出 `userId`、`username`、`role` 并构造 `UserPrincipal` 对象
5. 将 `UsernamePasswordAuthenticationToken` 设置到 `SecurityContextHolder` 中
6. 后续 Controller 通过 `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` 获取当前用户

**角色权限**
- `UserPrincipal` 提供 `isAdmin()` 方法判断是否为管理员
- 文章和评论的删除操作中，**管理员可以删除任何内容**，普通用户只能删除自己的内容

### 3.2 文章增删改查

**创建文章** (`POST /api/articles`)
- 需登录认证
- 接收 `ArticleCreateRequest`（title, content, summary, categoryId）
- 设置 `userId` 为当前登录用户 ID
- 通过 `articleMapper.insert()` 写入数据库

**文章分页列表** (`GET /api/articles?page=&size=&categoryId=&keyword=`)
- 无需登录即可访问（在 SecurityConfig 中放行）
- 支持按分类 `categoryId` 和关键词 `keyword`（模糊匹配标题）过滤
- 使用 MyBatis-Plus 分页插件，按创建时间降序排列
- **批量查询作者名优化**：先从分页结果中提取所有不重复的 `userId`，再通过 `userMapper.selectBatchIds()` 一次性查询，最后组装到 `ArticleVO` 的 `authorName` 字段，避免 N+1 查询

**文章详情** (`GET /api/articles/{id}`)
- 根据 ID 查询单篇文章，并附上作者名

**更新文章** (`PUT /api/articles/{id}`)
- 仅文章作者本人可修改（校验 `userId` 是否匹配）
- 使用 `BeanUtils.copyProperties()` 将请求数据复制到现有文章

**删除文章** (`DELETE /api/articles/{id}`)
- 权限校验：管理员可删除任何文章，普通用户只能删除自己的文章
- 文章不存在时抛出 `BusinessException(404, "文章不存在")`

### 3.3 评论管理

**创建评论** (`POST /api/articles/{articleId}/comments`)
- 需登录认证
- 接收 `CommentCreateRequest`（content）
- 设置 `articleId` 和当前用户 `userId`

**评论列表** (`GET /api/articles/{articleId}/comments?page=&size=`)
- 无需登录即可查看
- 按创建时间降序排列，同样采用批量查询用户名优化

**删除评论** (`DELETE /api/articles/{articleId}/comments/{commentId}`)
- 权限逻辑与删除文章一致：管理员可删任意评论，普通用户只能删自己的评论

### 3.4 分类（Category）

项目中已定义 `Category` 实体和 `CategoryMapper`（`BaseMapper<Category>`），数据库含 `category` 表，但目前未提供 Controller 级别的分类管理接口。分类信息通过文章中的 `categoryId` 字段关联，可用于文章列表的筛选查询。

### 3.5 前端界面（Vue 3 + Element Plus）

前端是一个通过 CDN 引入 Vue 3 和 Element Plus 的 **单页应用**，嵌入在 `index.html` 中，由 Spring Boot 作为静态资源直接提供服务。

**页面布局**：
- **顶部导航栏**：左侧显示博客名称"奇点日记"与字体切换按钮；右侧显示注册/登录按钮（未登录）或用户信息与写文章按钮（已登录）
- **文章列表**：卡片式布局，展示文章标题、摘要、作者、时间；支持分页
- **功能对话框**：
  - **注册/登录对话框**：表单提交，调用对应 API
  - **写文章对话框**：标题 + 摘要 + 内容（textarea），发布后刷新列表
  - **文章详情对话框**：展示文章正文 + 评论区，支持发表评论和删除评论

**权限控制（前端）**：
- 文章和评论的删除按钮仅对**作者本人**或**管理员**可见
- 未登录用户可浏览文章和评论，但不能发表内容和执行删除操作

**用户体验优化**：
- 字体大小切换（普通/大字体），偏好存储在 `localStorage`
- 登录状态持久化（token 和用户信息存储在 `localStorage`）
- 使用 ElMessage 提供友好的操作反馈（成功/错误提示）

---

## 4. 技术栈与配置

### 4.1 技术栈一览

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.3.0 | 应用框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架（增强 MyBatis） |
| Spring Security | 3.3.0 | 认证与授权框架 |
| JWT (jjwt) | 0.12.5 | 无状态令牌认证 |
| MySQL | - | 关系型数据库 |
| HikariCP | - | 数据库连接池 |
| Lombok | - | 简化 POJO 代码 |
| SpringDoc OpenAPI | 2.6.0 | API 文档自动生成 |
| Vue 3 | 3.x | 前端框架（CDN 引入） |
| Element Plus | 2.x | 前端 UI 组件库（CDN 引入） |

### 4.2 关键配置文件

#### `application.properties` — 核心应用配置

```

数据库连接：MySQL 8.0+，编码 utf-8，时区 Asia/Shanghai
MyBatis-Plus：SQL 日志输出、驼峰命名映射、主键自增策略
SpringDoc：Swagger UI 路径 /swagger-ui.html，API 文档路径 /v3/api-docs
服务器端口：8080
日志级别：DEBUG（com.nuist / org.mybatis / org.springframework.jdbc / com.zaxxer.hikari）
```
#### `pom.xml` — Maven 依赖管理

父工程 `spring-boot-starter-parent:3.3.0`，关键依赖：

- `spring-boot-starter-web` — Web 服务
- `mybatis-plus-spring-boot3-starter:3.5.7` — MyBatis-Plus
- `mysql-connector-j` — MySQL 驱动
- `lombok` — 代码简化
- `springdoc-openapi-starter-webmvc-ui:2.6.0` — API 文档
- `spring-boot-starter-security` — 安全认证
- `jjwt-api/impl/jackson:0.12.5` — JWT 令牌

#### `SecurityConfig.java` — Spring Security 配置

- **CSRF 禁用**：RESTful API 无需 CSRF 保护
- **无状态会话**：使用 `SessionCreationPolicy.STATELESS`
- **公开接口放行**：
  - `POST /api/users/register` 和 `POST /api/users/login`
  - `GET /api/articles/**` 和 `GET /api/articles/{id}/comments/**`
  - Swagger UI 和主页
- **其他接口需认证**：通过自定义 `JwtAuthenticationFilter` 解析请求头中的 JWT
- **密码编码器**：`BCryptPasswordEncoder`

#### `MybatisPlusConfig.java` — MyBatis-Plus 插件

- 注册 `PaginationInnerInterceptor` 分页拦截器（适配 MySQL）

#### `DataInitializer.java` — 数据初始化

- 应用启动时自动执行
- 检测 `user` 表是否存在 `role` 列，若不存在则通过 DDL 添加
- 检测 `root` 管理员是否存在，若不存在则创建（密码 `123456`，BCrypt 加密）

#### `MyMetaObjectHandler.java` — 自动填充

- 插入时自动填充 `createTime` 和 `updateTime`
- 更新时自动填充 `updateTime`

### 4.3 数据库表结构

```
sql
-- 用户表
CREATE TABLE `user` (
`id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
`username`    VARCHAR(50)  NOT NULL UNIQUE,
`password`    VARCHAR(255) NOT NULL,
`email`       VARCHAR(100),
`role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE `category` (
`id`   BIGINT AUTO_INCREMENT PRIMARY KEY,
`name` VARCHAR(50) NOT NULL UNIQUE
);

-- 文章表
CREATE TABLE `article` (
`id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
`title`       VARCHAR(200) NOT NULL,
`content`     TEXT NOT NULL,
`summary`     VARCHAR(500),
`category_id` BIGINT,
`user_id`     BIGINT NOT NULL,
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 评论表
CREATE TABLE `comment` (
`id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
`article_id`  BIGINT NOT NULL,
`user_id`     BIGINT NOT NULL,
`content`     TEXT NOT NULL,
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);
```
---

## 5. API 一览

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/users/register` | 无需 | 用户注册 |
| POST | `/api/users/login` | 无需 | 用户登录，返回 JWT |
| GET | `/api/users/{id}` | 无要求 | 获取用户信息 |
| POST | `/api/articles` | 需要 | 创建文章 |
| GET | `/api/articles` | 无需 | 文章分页列表（支持分类/关键词筛选） |
| GET | `/api/articles/{id}` | 无需 | 文章详情 |
| PUT | `/api/articles/{id}` | 需要 | 更新文章（仅作者） |
| DELETE | `/api/articles/{id}` | 需要 | 删除文章（作者/管理员） |
| POST | `/api/articles/{articleId}/comments` | 需要 | 发表评论 |
| GET | `/api/articles/{articleId}/comments` | 无需 | 评论分页列表 |
| DELETE | `/api/articles/{articleId}/comments/{commentId}` | 需要 | 删除评论（作者/管理员） |

---

## 6. 前端界面说明

前端采用 **Vue 3 Composition API + Element Plus**，通过 CDN 方式引入，页面整体作为 Spring Boot 的静态资源（`/index.html`）提供服务。

### 页面功能区域

1. **顶部导航** — 博客名称、字体切换、用户状态（登录/注册按钮 或 用户信息+写文章按钮）
2. **文章列表** — 卡片式展示，标题可点击进入详情，管理员/作者可见删除按钮
3. **分页组件** — Element Plus `el-pagination`，支持页码跳转
4. **注册/登录弹窗** — 表单验证与 API 调用
5. **写文章弹窗** — 标题、摘要、内容输入，发布后刷新列表
6. **文章详情弹窗** — 正文展示 + 评论区（发表评论、删除评论），支持管理员/作者删除文章

### 前端状态管理

- 登录 token 和用户信息持久化到 `localStorage`
- 字体偏好（正常/大字体）持久化到 `localStorage`
- 页面加载时自动恢复登录状态和字体设置

---

> **总结**：本项目是一个典型的 **前后端融合单体应用**，后端严格遵循 Controller → Service → Mapper → Entity 的分层架构，通过 JWT 实现无状态认证，MyBatis-Plus 大幅简化了数据访问代码。前端以嵌入式 SPA 的形式提供完整的博客交互体验。整体设计轻量、清晰，适合作为学习 Spring Boot 全栈开发的参考项目。
```


以上就是完整的 `DEVELOPMENT_SUMMARY.md` 文件内容，文档涵盖了：

1. **开发起点与流程** — 从数据库设计 → Entity → Mapper → Service → Controller → 前端界面的完整链路
2. **包结构说明** — 每个包的职责、功能、典型操作，以及完整目录树
3. **核心功能模块** — 用户注册登录（JWT 认证流程）、文章 CRUD（分页/分类/批量查询优化）、评论管理、分类说明、前端界面交互
4. **技术栈与配置** — 技术版本一览、5 个关键配置类详解、完整的数据库表结构 SQL
5. **API 一览表** — 11 个 RESTful 接口的方法、路径、认证要求及说明
6. **前端界面说明** — 页面区域、状态管理、权限控制、用户体验优化