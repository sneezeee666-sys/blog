package com.nuist.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.common.JwtUtil;
import com.nuist.blog.dto.LoginRequest;
import com.nuist.blog.dto.LoginResponse;
import com.nuist.blog.dto.RegisterRequest;
import com.nuist.blog.entity.User;
import com.nuist.blog.mapper.UserMapper;
import com.nuist.blog.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequest req) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole("USER"); // 新注册用户默认为普通用户
        userMapper.insert(user);
        return user;
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        return new LoginResponse(user, token);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 应用启动时自动初始化管理员账号 root / 123456
     * 若已存在则跳过（幂等）
     */
    @PostConstruct
    public void initAdmin() {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "root"));
        if (count == 0) {
            User admin = new User();
            admin.setUsername("root");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@blog.com");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            System.out.println("✅ 管理员账号已初始化: root / 123456");
        }
    }
}
