package com.nuist.blog.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.entity.Article;

public interface ArticleService {
    Article create(ArticleCreateRequest request, Long userId);
    Article getById(Long id);
    Page<Article> page(ArticleQueryRequest request);
    void delete(Long id, Long userId);
    Article update(Long id, ArticleCreateRequest request, Long userId);
}