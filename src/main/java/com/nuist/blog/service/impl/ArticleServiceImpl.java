package com.nuist.blog.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.entity.Article;
import com.nuist.blog.mapper.ArticleMapper;
import com.nuist.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;

    @Override
    public Article create(ArticleCreateRequest req, Long userId) {
        Article article = new Article();
        BeanUtils.copyProperties(req, article);
        article.setUserId(userId);
        articleMapper.insert(article);
        return article;
    }

    @Override
    public Article getById(Long id) {
        return articleMapper.selectById(id);
    }

    @Override
    public Page<Article> page(ArticleQueryRequest req) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (req.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, req.getCategoryId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, req.getKeyword());
        }
        wrapper.orderByDesc(Article::getCreateTime);
        return articleMapper.selectPage(new Page<>(req.getPage(), req.getSize()), wrapper);
    }

    @Override
    public void delete(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        if (!article.getUserId().equals(userId)) throw new BusinessException(403, "无权限删除");
        articleMapper.deleteById(id);
    }

    @Override
    public Article update(Long id, ArticleCreateRequest req, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        if (!article.getUserId().equals(userId)) throw new BusinessException(403, "无权限修改");
        BeanUtils.copyProperties(req, article);
        article.setId(id);
        articleMapper.updateById(article);
        return article;
    }
}