package com.taskflow.dto.todo;

import com.taskflow.domain.Todo;
import com.taskflow.domain.TodoStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办视图对象：领域实体 → 对外 DTO（只暴露需要的字段）。
 */
public record TodoVO(
        Long id,
        String title,
        String description,
        TodoStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TodoVO from(Todo todo) {
        return new TodoVO(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                todo.getDueDate(),
                todo.getCreatedAt(),
                todo.getUpdatedAt());
    }
}
