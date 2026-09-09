package com.taskflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 tf_user
 */
@Data
@TableName("tf_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希后的密码，绝不存明文 */
    private String password;

    private String nickname;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
