package com.nuist.blog.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.dto.CommentCreateRequest;
import com.nuist.blog.entity.Comment;

public interface CommentService {
    Comment create(Long articleId, Long userId, CommentCreateRequest request);
    Page<Comment> pageByArticleId(Long articleId, int page, int size);
}