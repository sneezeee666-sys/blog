package com.nuist.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nuist.blog.entity.User;
import com.nuist.blog.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化器 —— 应用启动时自动执行
 * 1. 为已有 user 表添加 role 列（如不存在）
 * 2. 创建管理员账号 root / 123456（如不存在）
 * 
 * 所有操作幂等，可重复执行不会出错
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        addRoleColumnIfNeeded();
        createAdminIfNeeded();
    }

    /** 为 user 表添加 role 列（安全的 DDL，已存在则跳过） */
    private void addRoleColumnIfNeeded() {
        try {
            // 先尝试查询，如果列已存在则不执行
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role'",
                Integer.class
            );
            log.info("✅ role 列已存在，跳过迁移");
        } catch (Exception e) {
            // 列不存在或查询失败，尝试添加
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' " +
                    "COMMENT '用户角色: USER=普通用户, ADMIN=管理员'");
                log.info("✅ role 列已添加");
            } catch (Exception ex) {
                log.warn("⚠️ 添加 role 列失败（可能已存在）: {}", ex.getMessage());
            }
        }
    }

    /** 创建管理员账号（已存在则跳过） */
    private void createAdminIfNeeded() {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "root"));
        if (count == 0) {
            User admin = new User();
            admin.setUsername("root");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@blog.com");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("✅ 管理员账号已创建: root / 123456");
        } else {
            log.info("✅ 管理员账号已存在，跳过创建");
        }
    }
}
