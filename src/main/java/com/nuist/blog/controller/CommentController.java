package com.nuist.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.common.Result;
import com.nuist.blog.common.UserPrincipal;
import com.nuist.blog.dto.CommentCreateRequest;
import com.nuist.blog.dto.CommentVO;
import com.nuist.blog.entity.Comment;
import com.nuist.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public Result<Comment> create(@PathVariable Long articleId,
                                  @RequestBody CommentCreateRequest request) {
        UserPrincipal principal = getCurrentUser();
        return Result.success(commentService.create(articleId, principal.getUserId(), request));
    }

    @GetMapping
    public Result<Page<CommentVO>> page(@PathVariable Long articleId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.pageByArticleId(articleId, page, size));
    }

    @DeleteMapping("/{commentId}")
    public Result<?> delete(@PathVariable Long articleId,
                            @PathVariable Long commentId) {
        UserPrincipal principal = getCurrentUser();
        commentService.delete(commentId, principal.getUserId(), principal.isAdmin());
        return Result.success(null);
    }

    private UserPrincipal getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        throw new BusinessException(401, "未登录");
    }
}
