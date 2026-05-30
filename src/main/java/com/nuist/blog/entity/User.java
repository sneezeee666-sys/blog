package com.nuist.blog.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    /** 角色: USER=普通用户, ADMIN=管理员 */
    private String role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
