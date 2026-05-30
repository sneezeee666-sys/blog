package com.nuist.blog.dto;

import com.nuist.blog.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章视图对象 —— 在 Article 基础上附加作者名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleVO extends Article {
    private String authorName;
}
