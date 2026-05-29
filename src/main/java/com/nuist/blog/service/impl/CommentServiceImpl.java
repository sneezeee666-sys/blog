package com.nuist.blog.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.dto.CommentCreateRequest;
import com.nuist.blog.entity.Comment;
import com.nuist.blog.mapper.CommentMapper;
import com.nuist.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;

    @Override
    public Comment create(Long articleId, Long userId, CommentCreateRequest request) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public Page<Comment> pageByArticleId(Long articleId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime);
        return commentMapper.selectPage(new Page<>(page, size), wrapper);
    }
}