package com.taskflow.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TodoCreateRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题最长 200 个字符")
        String title,

        @Size(max = 1000, message = "描述最长 1000 个字符")
        String description,

        LocalDate dueDate) {
}
