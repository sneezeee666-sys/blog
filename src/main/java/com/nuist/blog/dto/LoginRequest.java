package com.nuist.blog.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}