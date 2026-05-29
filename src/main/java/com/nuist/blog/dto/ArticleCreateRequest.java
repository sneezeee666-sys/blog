package com.nuist.blog.dto;
import lombok.Data;

@Data
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
}