package com.nuist.blog.controller;
import com.nuist.blog.common.Result;
import com.nuist.blog.dto.LoginRequest;
import com.nuist.blog.dto.LoginResponse;
import com.nuist.blog.dto.RegisterRequest;
import com.nuist.blog.entity.User;
import com.nuist.blog.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/{id}")
    public Result<User> getProfile(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}