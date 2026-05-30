package com.nuist.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.common.Result;
import com.nuist.blog.common.UserPrincipal;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.dto.ArticleVO;
import com.nuist.blog.entity.Article;
import com.nuist.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping
    public Result<Article> create(@RequestBody ArticleCreateRequest request) {
        UserPrincipal principal = getCurrentUser();
        return Result.success(articleService.create(request, principal.getUserId()));
    }

    @GetMapping
    public Result<Page<ArticleVO>> page(ArticleQueryRequest request) {
        return Result.success(articleService.page(request));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> getById(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id,
                                  @RequestBody ArticleCreateRequest request) {
        UserPrincipal principal = getCurrentUser();
        return Result.success(articleService.update(id, request, principal.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        UserPrincipal principal = getCurrentUser();
        articleService.delete(id, principal.getUserId(), principal.isAdmin());
        return Result.success(null);
    }

    /** 从 SecurityContext 获取当前登录用户（含 role） */
    private UserPrincipal getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        throw new BusinessException(401, "未登录");
    }
}
