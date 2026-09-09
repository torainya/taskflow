package com.taskflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.domain.Todo;
import com.taskflow.domain.TodoStatus;
import com.taskflow.dto.todo.TodoCreateRequest;
import com.taskflow.dto.todo.TodoStats;
import com.taskflow.dto.todo.TodoStatusUpdateRequest;
import com.taskflow.dto.todo.TodoUpdateRequest;
import com.taskflow.dto.todo.TodoVO;
import com.taskflow.exception.BizException;
import com.taskflow.mapper.TodoMapper;
import com.taskflow.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 待办业务：所有读写都以 userId 隔离 —— 这是「每个用户只能看到自己的数据」的实现点。
 */
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoMapper todoMapper;

    @Transactional(readOnly = true)
    public PageResult<TodoVO> page(long page, long size, String status, String keyword, Long userId) {
        long currentPage = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), 100);
        TodoStatus statusEnum = parseStatus(status);

        LambdaQueryWrapper<Todo> query = Wrappers.<Todo>lambdaQuery()
                .eq(Todo::getUserId, userId)
                .eq(statusEnum != null, Todo::getStatus, statusEnum)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Todo::getTitle, keyword.trim())
                        .or()
                        .like(Todo::getDescription, keyword.trim()))
                .orderByDesc(Todo::getCreatedAt)
                .orderByDesc(Todo::getId);

        Page<Todo> result = todoMapper.selectPage(new Page<>(currentPage, pageSize), query);
        return PageResult.from(result, TodoVO::from);
    }

    @Transactional(readOnly = true)
    public TodoVO get(Long id, Long userId) {
        return TodoVO.from(requireOwned(id, userId));
    }

    @Transactional
    public TodoVO create(TodoCreateRequest request, Long userId) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(request.title().trim());
        todo.setDescription(trimToNull(request.description()));
        todo.setStatus(TodoStatus.TODO);
        todo.setDueDate(request.dueDate());
        LocalDateTime now = LocalDateTime.now();
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        todoMapper.insert(todo);
        return TodoVO.from(todo);
    }

    @Transactional
    public TodoVO update(Long id, TodoUpdateRequest request, Long userId) {
        Todo todo = requireOwned(id, userId);
        todo.setTitle(request.title().trim());
        todo.setDescription(trimToNull(request.description()));
        todo.setStatus(request.status());
        todo.setDueDate(request.dueDate());
        todo.setUpdatedAt(LocalDateTime.now());
        todoMapper.updateById(todo);
        return TodoVO.from(todo);
    }

    @Transactional
    public TodoVO changeStatus(Long id, TodoStatusUpdateRequest request, Long userId) {
        Todo todo = requireOwned(id, userId);
        todo.setStatus(request.status());
        todo.setUpdatedAt(LocalDateTime.now());
        todoMapper.updateById(todo);
        return TodoVO.from(todo);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Todo todo = requireOwned(id, userId);
        todoMapper.deleteById(todo.getId());
    }

    @Transactional(readOnly = true)
    public TodoStats stats(Long userId) {
        return TodoStats.from(todoMapper.countByStatus(userId));
    }

    /** 校验归属：不存在或不属于当前用户，一律返回 404（不泄露他人数据的存在性） */
    private Todo requireOwned(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw BizException.notFound("TODO_NOT_FOUND", "待办不存在或无权访问");
        }
        return todo;
    }

    private TodoStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return TodoStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BizException.badRequest("INVALID_STATUS", "status 仅支持 TODO / IN_PROGRESS / DONE");
        }
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
