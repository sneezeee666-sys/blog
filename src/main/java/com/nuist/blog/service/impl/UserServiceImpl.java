package com.nuist.blog.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.dto.LoginRequest;
import com.nuist.blog.dto.RegisterRequest;
import com.nuist.blog.entity.User;
import com.nuist.blog.mapper.UserMapper;
import com.nuist.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public User register(RegisterRequest req) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // 学习阶段明文，后续可加密
        user.setEmail(req.getEmail());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())
                .eq(User::getPassword, req.getPassword()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}