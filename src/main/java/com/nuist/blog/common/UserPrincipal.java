package com.nuist.blog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 自定义认证主体，包含 userId、username、role
 * 替代原来 SecurityContext 中只存 Long userId 的做法
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private String role;

    /** 是否为管理员 */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
