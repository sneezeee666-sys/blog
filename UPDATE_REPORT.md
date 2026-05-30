# Blog 项目优化更新报告

> **项目名称**: 奇点日记 (Blog)  
> **版本**: v1.0 → v2.0  
> **更新日期**: 2026-05-29  
> **更新人**: AI 助手 (张佳琪)  

---

## 📋 更新概要

本次更新针对 5 项功能需求进行了全面优化，涉及后端 Java 代码、前端 HTML 页面、数据库结构三个方面。所有改动均保持对已有数据的向后兼容，不影响现有用户和文章数据。

---

## 🗂️ 文件变更清单

### 新增文件（4个）

| 文件 | 说明 |
|------|------|
| `src/main/java/com/nuist/blog/common/UserPrincipal.java` | 自定义认证主体，包含 userId、username、role |
| `src/main/java/com/nuist/blog/dto/ArticleVO.java` | 文章视图对象，在 Article 基础上附加 authorName |
| `src/main/java/com/nuist/blog/dto/CommentVO.java` | 评论视图对象，在 Comment 基础上附加 username |
| `src/main/java/com/nuist/blog/config/DataInitializer.java` | 数据库初始化器，启动时自动迁移旧表+创建管理员 |
| `src/main/resources/db_migration.sql` | 手动迁移 SQL 参考脚本 |

### 修改文件（14个）

| 文件 | 改动内容 |
|------|----------|
| `entity/User.java` | 新增 `role` 字段（USER/ADMIN） |
| `common/JwtUtil.java` | JWT 生成时注入 role，新增 getUsernameFromToken / getRoleFromToken |
| `config/SecurityConfig.java` | 过滤器解析 JWT 时构建 UserPrincipal 替代裸 Long |
| `service/UserService.java` | login 方法适配 role，register 默认角色 USER |
| `service/impl/UserServiceImpl.java` | 新增 @PostConstruct 初始化管理员账号 |
| `service/ArticleService.java` | getById 返回 ArticleVO，page 返回 Page\<ArticleVO\>，delete 增加 isAdmin 参数 |
| `service/impl/ArticleServiceImpl.java` | 批量查询作者名填充 authorName；管理员可删任何文章 |
| `service/CommentService.java` | pageByArticleId 返回 CommentVO，新增 delete 方法 |
| `service/impl/CommentServiceImpl.java` | 批量查询用户名填充 username；实现权限删除逻辑 |
| `controller/ArticleController.java` | getCurrentUser() 返回 UserPrincipal；delete 传 isAdmin |
| `controller/CommentController.java` | 新增 DELETE /{commentId} 端点；getCurrentUser() 使用 UserPrincipal |
| `resources/schema.sql` | user 表增加 role 列，预设管理员 root 账号 |
| `resources/application.properties` | 未改动 |
| `resources/static/index.html` | 前端全面重写（见下方详述） |

---

## 🔧 需求 1：放大版字体阅读选项

### 实现方式
- 使用 CSS 变量 `:root` 控制全局字体大小
- 在页面头部 Header 添加字体切换按钮（🔍/🔎 图标）
- 点击按钮在 `normal` 和 `large` 两种模式间切换

### 技术细节
```css
:root {
    --base-font: 14px;
    --content-font: 15px;
    --line-height: 1.8;
}
body.large-font {
    --base-font: 18px;
    --content-font: 19px;
    --line-height: 2.0;
}
```
- 字体设置通过 `localStorage` 持久化，刷新页面保持生效
- 大字体模式下文章正文、评论内容、标题同步放大
- 适合手机小屏阅读场景

---

## 🔧 需求 2：分页功能（每页 10 篇）

### 实现方式
- 使用 Element Plus 的 `<el-pagination>` 组件
- 布局含「上一页、页码、下一页、跳转到指定页」
- API 层面已有分页支持，前端补充调用逻辑

### 技术细节
```javascript
const currentPage = ref(1);
const pageSize = ref(10);
const totalArticles = ref(0);

const loadArticles = async () => {
    const res = await fetch(`/api/articles?page=${currentPage.value}&size=${pageSize.value}`);
    const data = await res.json();
    articles.value = data.data.records;
    totalArticles.value = data.data.total;
};
```
- 后端 `ArticleServiceImpl.page()` 返回的 `Page` 对象包含 `total`（总记录数）
- 翻页时自动重新请求对应页码的文章列表

---

## 🔧 需求 3：优化评论功能

### 3.1 显示注册用户名
- **问题**: 原代码显示 `用户{{ comment.userId }}`（如"用户1""用户2"）
- **方案**: 新建 `CommentVO` 继承 `Comment`，新增 `username` 字段
- **实现**: `CommentServiceImpl.pageByArticleId()` 在查询分页后，批量收集所有 userId，一次性查询用户表获取用户名
- **效果**: 评论显示为实际注册用户名，如"张三""root"

### 3.2 本人可删除评论
- 后端新增 `DELETE /api/articles/{articleId}/comments/{commentId}` 端点
- 权限判断: `comment.userId === currentUserId` 才允许删除
- 前端每条评论旁显示删除按钮（圆形×图标），仅评论作者可见

```java
// CommentServiceImpl.delete()
if (!isAdmin && !comment.getUserId().equals(userId)) {
    throw new BusinessException(403, "无权限删除：只能删除自己的评论");
}
```

---

## 🔧 需求 4：文章显示作者名 & 本人可删除

### 4.1 显示作者名
- **问题**: 原文章卡片只显示标题、摘要、时间，无作者信息
- **方案**: 新建 `ArticleVO` 继承 `Article`，新增 `authorName` 字段
- **实现**: `ArticleServiceImpl.page()` 批量查询 user 表填充作者名
- **效果**: 文章卡片底部和详情页均展示 `👤 作者名`

### 4.2 本人可删除文章
- 后端已有关联删除端点（`DELETE /api/articles/{id}`），但删除逻辑仅限作者本人
- 前端文章卡片右上角和详情页均添加删除按钮
- 删除前有 `el-popconfirm` 二次确认弹窗

---

## 🔧 需求 5：管理员用户 root / 123456

### 实现方式
- User 实体新增 `role` 字段，取值 `USER`（普通用户）或 `ADMIN`（管理员）
- `DataInitializer` 组件在应用启动时自动：
  1. 检测 user 表是否有 `role` 列，若没有则自动 ALTER TABLE 添加
  2. 检测是否存在 root 用户，若没有则创建（密码 BCrypt 加密）
- JWT 令牌中注入 `role` 字段
- `UserPrincipal` 携带 role，通过 `isAdmin()` 方法判断权限

### 管理员权限
| 操作 | 普通用户 | 管理员 (root) |
|------|----------|---------------|
| 删除自己的文章 | ✅ | ✅ |
| 删除他人的文章 | ❌ | ✅ |
| 删除自己的评论 | ✅ | ✅ |
| 删除他人的评论 | ❌ | ✅ |
| 写文章 | ✅ | ✅ |
| 发表评论 | ✅ | ✅ |

### 初始化机制
```java
// DataInitializer.java
@PostConstruct
public void init() {
    addRoleColumnIfNeeded();   // DDL 自动迁移
    createAdminIfNeeded();      // 创建 root 账号
}
```

---

## 🗄️ 数据库变更

### 对已有数据库的影响
- **新增列**: `user.role VARCHAR(20) DEFAULT 'USER'`
- **新增行**: 管理员账号 root（如不存在）
- **不变**: article、comment、category 表结构完全不变
- **兼容**: 已有用户的 role 字段默认为 'USER'，原有功能不受影响

### 自动迁移（推荐）
应用启动时 `DataInitializer` 自动执行迁移，无需手动操作。

### 手动迁移（可选）
```bash
mysql -u blogadmin -p blog_db < src/main/resources/db_migration.sql
```

---

## 🎨 前端 UI 变更总结

| 位置 | 原版 | 新版 |
|------|------|------|
| 头部导航 | 仅标题+登录 | 标题+字体切换+登录/用户信息+管理员标签 |
| 文章列表 | 标题+摘要+时间 | 标题+删除按钮(条件)+摘要+作者名+时间 |
| 文章列表底部 | 无 | 分页组件（页码+跳转） |
| 文章详情 | "用户N" | 实际用户名 |
| 评论 | 无删除功能 | 可删除自己的评论（确认弹窗） |
| 字体 | 固定大小 | 🔍 一键切换大字体 |

---

## 🚀 部署步骤

1. **替换文件**: 将更新后的 `src/` 目录覆盖到原项目
2. **数据库迁移**: 启动应用，`DataInitializer` 自动处理（或手动执行 `db_migration.sql`）
3. **编译打包**: `mvn clean package -DskipTests`
4. **启动运行**: `java -jar target/blog-0.0.1-SNAPSHOT.jar`
5. **登录管理员**: 用户名 `root`，密码 `123456`

---

## ⚠️ 注意事项

1. 管理员密码 `123456` 为默认值，生产环境建议登录后修改
2. JWT 密钥 `my-super-secret-key-blog-2026-very-long-enough` 为硬编码，生产环境应配置到 application.properties
3. 删除文章时**不会自动删除关联评论**（评论保留在数据库但不可见），如需级联删除可后续扩展
4. 前端 `index.html` 为 SPA 单文件，所有 Element Plus / Vue 3 通过 CDN 引入，需要网络连接

---

## 📊 技术栈

- **后端**: Spring Boot 3.3.0 + MyBatis Plus 3.5.7 + Spring Security + JWT
- **前端**: Vue 3 + Element Plus（CDN）
- **数据库**: MySQL 8.x
- **构建**: Maven

---

*报告完毕。如有问题请随时反馈。*
