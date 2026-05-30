package com.nuist.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.dto.CommentCreateRequest;
import com.nuist.blog.dto.CommentVO;
import com.nuist.blog.entity.Comment;
import com.nuist.blog.entity.User;
import com.nuist.blog.mapper.CommentMapper;
import com.nuist.blog.mapper.UserMapper;
import com.nuist.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

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
    public Page<CommentVO> pageByArticleId(Long articleId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(
                new Page<>(page, size), wrapper);

        // 批量查询发表者用户名
        List<Long> userIds = commentPage.getRecords().stream()
                .map(Comment::getUserId).distinct().toList();
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        List<CommentVO> voList = commentPage.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime());
            vo.setUsername(userMap.getOrDefault(comment.getUserId(), "未知用户"));
            return vo;
        }).toList();

        Page<CommentVO> voPage = new Page<>(page, size, commentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void delete(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");
        // 管理员可以删除任何评论，普通用户只能删除自己的评论
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限删除：只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
    }
}
