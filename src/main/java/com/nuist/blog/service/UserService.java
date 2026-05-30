package com.nuist.blog.service;
import com.nuist.blog.dto.LoginRequest;
import com.nuist.blog.dto.LoginResponse;
import com.nuist.blog.dto.RegisterRequest;
import com.nuist.blog.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request);  // 返回 LoginResponse
    User getById(Long id);
}