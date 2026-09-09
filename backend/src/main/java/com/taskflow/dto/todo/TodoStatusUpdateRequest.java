package com.taskflow.dto.todo;

import com.taskflow.domain.TodoStatus;
import jakarta.validation.constraints.NotNull;

public record TodoStatusUpdateRequest(@NotNull(message = "状态不能为空") TodoStatus status) {
}
