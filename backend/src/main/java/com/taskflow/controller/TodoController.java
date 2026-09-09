package com.taskflow.controller;

import com.taskflow.common.PageResult;
import com.taskflow.dto.todo.TodoCreateRequest;
import com.taskflow.dto.todo.TodoStats;
import com.taskflow.dto.todo.TodoStatusUpdateRequest;
import com.taskflow.dto.todo.TodoUpdateRequest;
import com.taskflow.dto.todo.TodoVO;
import com.taskflow.security.AuthUser;
import com.taskflow.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "待办", description = "任务待办的增删改查 / 状态流转 / 分页统计（需登录）")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "分页查询我的待办（可按状态与关键字过滤）")
    @GetMapping
    public PageResult<TodoVO> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal AuthUser user) {
        return todoService.page(page, size, status, keyword, user.id());
    }

    @Operation(summary = "各状态数量统计")
    @GetMapping("/stats")
    public TodoStats stats(@AuthenticationPrincipal AuthUser user) {
        return todoService.stats(user.id());
    }

    @Operation(summary = "待办详情")
    @GetMapping("/{id}")
    public TodoVO get(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return todoService.get(id, user.id());
    }

    @Operation(summary = "新建待办")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoVO create(@Valid @RequestBody TodoCreateRequest request, @AuthenticationPrincipal AuthUser user) {
        return todoService.create(request, user.id());
    }

    @Operation(summary = "整体更新（标题/描述/截止日期/状态）")
    @PutMapping("/{id}")
    public TodoVO update(@PathVariable Long id,
                         @Valid @RequestBody TodoUpdateRequest request,
                         @AuthenticationPrincipal AuthUser user) {
        return todoService.update(id, request, user.id());
    }

    @Operation(summary = "仅流转状态")
    @PatchMapping("/{id}/status")
    public TodoVO changeStatus(@PathVariable Long id,
                               @Valid @RequestBody TodoStatusUpdateRequest request,
                               @AuthenticationPrincipal AuthUser user) {
        return todoService.changeStatus(id, request, user.id());
    }

    @Operation(summary = "删除待办")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        todoService.delete(id, user.id());
    }
}
