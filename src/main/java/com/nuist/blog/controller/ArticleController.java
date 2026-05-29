package com.nuist.blog.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.Result;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.entity.Article;
import com.nuist.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    // 暂时硬编码 userId=1 代替登录态，后续接入认证后改为从 token 中获取
    private final Long mockUserId = 1L;

    @PostMapping
    public Result<Article> create(@RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.create(request, mockUserId));
    }

    @GetMapping
    public Result<Page<Article>> page(ArticleQueryRequest request) {
        return Result.success(articleService.page(request));
    }

    @GetMapping("/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        articleService.delete(id, mockUserId);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable Long id, @RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.update(id, request, mockUserId));
    }
}