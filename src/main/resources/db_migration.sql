-- ============================================
-- Blog 项目更新迁移脚本（v2.0）
-- 用途：在已有数据库上追加 v2.0 所需字段和数据
-- 影响范围：新增 role 列、插入管理员账号
-- 特性：所有语句均为幂等（可重复执行不出错）
-- ============================================

-- 1. 给 user 表增加 role 列（若已存在则跳过）
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `role` VARCHAR(20) NOT NULL DEFAULT 'USER'
    COMMENT '用户角色: USER=普通用户, ADMIN=管理员';

-- 2. 插入管理员账号 root / 123456（密码使用 BCrypt 加密）
--    若已存在则忽略
INSERT IGNORE INTO `user` (`username`, `password`, `email`, `role`, `create_time`)
VALUES ('root',
        '$2b$10$vKIm0ehEJUviHmEq.s4D8eDVIz58x7HqIDgDwwyJ8N5zQxyMM4f16',
        'admin@blog.com',
        'ADMIN',
        NOW());
