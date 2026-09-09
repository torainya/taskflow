package com.taskflow.domain;

/**
 * 待办状态机：TODO → IN_PROGRESS → DONE（允许任意跳转）
 * <p>存储到数据库时使用枚举名（大写），列类型 varchar(20)。</p>
 */
public enum TodoStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
