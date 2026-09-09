package com.taskflow.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办表 tf_todo
 * <p>说明：description / dueDate 使用 ALWAYS 更新策略，否则 MyBatis-Plus
 * 默认的 NOT_NULL 策略会导致「把字段改回空值」不生效。</p>
 */
@Data
@TableName("tf_todo")
public class Todo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 归属用户（数据隔离维度） */
    private Long userId;

    private String title;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;

    /** TODO / IN_PROGRESS / DONE，见 {@link TodoStatus} */
    private TodoStatus status;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
