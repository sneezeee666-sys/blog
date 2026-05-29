package com.nuist.blog.dto;
import lombok.Data;

@Data
public class ArticleQueryRequest {
    private Long categoryId;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}