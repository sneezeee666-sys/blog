package com.nuist.blog.service;
import com.nuist.blog.dto.LoginRequest;
import com.nuist.blog.dto.RegisterRequest;
import com.nuist.blog.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    User login(LoginRequest request);
    User getById(Long id);
}