package com.nuist.blog.dto;

import com.nuist.blog.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论视图对象 —— 在 Comment 基础上附加发表者用户名
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {
    private String username;
}
