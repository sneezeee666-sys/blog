package com.nuist.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.dto.ArticleVO;
import com.nuist.blog.entity.Article;

public interface ArticleService {
    Article create(ArticleCreateRequest request, Long userId);
    ArticleVO getById(Long id);
    Page<ArticleVO> page(ArticleQueryRequest request);
    void delete(Long id, Long userId, boolean isAdmin);
    Article update(Long id, ArticleCreateRequest request, Long userId);
}
