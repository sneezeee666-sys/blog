package com.nuist.blog.dto;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
}