package com.nuist.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuist.blog.common.BusinessException;
import com.nuist.blog.dto.ArticleCreateRequest;
import com.nuist.blog.dto.ArticleQueryRequest;
import com.nuist.blog.dto.ArticleVO;
import com.nuist.blog.entity.Article;
import com.nuist.blog.entity.User;
import com.nuist.blog.mapper.ArticleMapper;
import com.nuist.blog.mapper.UserMapper;
import com.nuist.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    @Override
    public Article create(ArticleCreateRequest req, Long userId) {
        Article article = new Article();
        BeanUtils.copyProperties(req, article);
        article.setUserId(userId);
        articleMapper.insert(article);
        return article;
    }

    @Override
    public ArticleVO getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) return null;
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        User user = userMapper.selectById(article.getUserId());
        vo.setAuthorName(user != null ? user.getUsername() : "未知用户");
        return vo;
    }

    @Override
    public Page<ArticleVO> page(ArticleQueryRequest req) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (req.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, req.getCategoryId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, req.getKeyword());
        }
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> articlePage = articleMapper.selectPage(
                new Page<>(req.getPage(), req.getSize()), wrapper);

        // 批量查询作者信息，填充 authorName
        List<Long> userIds = articlePage.getRecords().stream()
                .map(Article::getUserId).distinct().toList();
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        List<ArticleVO> voList = articlePage.getRecords().stream().map(article -> {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            vo.setAuthorName(userMap.getOrDefault(article.getUserId(), "未知用户"));
            return vo;
        }).toList();

        Page<ArticleVO> voPage = new Page<>(req.getPage(), req.getSize(), articlePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void delete(Long id, Long userId, boolean isAdmin) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        // 管理员可以删除任何文章，普通用户只能删除自己的文章
        if (!isAdmin && !article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限删除：只能删除自己的文章");
        }
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
