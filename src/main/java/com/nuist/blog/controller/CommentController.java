package com.nuist.blog.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.Result;
import com.nuist.blog.dto.CommentCreateRequest;
import com.nuist.blog.entity.Comment;
import com.nuist.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 硬编码 userId=1 临时方案，将来改认证
    private final Long mockUserId = 1L;

    @PostMapping
    public Result<Comment> create(@PathVariable Long articleId,
                                  @RequestBody CommentCreateRequest request) {
        return Result.success(commentService.create(articleId, mockUserId, request));
    }

    @GetMapping
    public Result<Page<Comment>> page(@PathVariable Long articleId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.pageByArticleId(articleId, page, size));
    }
}