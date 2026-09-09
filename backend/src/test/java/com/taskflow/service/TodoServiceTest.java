package com.taskflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.common.PageResult;
import com.taskflow.domain.Todo;
import com.taskflow.domain.TodoStatus;
import com.taskflow.dto.todo.TodoCreateRequest;
import com.taskflow.dto.todo.TodoStatusUpdateRequest;
import com.taskflow.dto.todo.TodoVO;
import com.taskflow.exception.BizException;
import com.taskflow.mapper.TodoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoMapper todoMapper;

    private TodoService todoService;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        todoService = new TodoService(todoMapper);
    }

    @Test
    void create_bindsUserId_andDefaultsToTodo() {
        TodoCreateRequest request = new TodoCreateRequest("学 Spring Boot 4", "看看新特性", LocalDate.now());

        todoService.create(request, USER_ID);

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(todoMapper).insert(captor.capture());
        Todo saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getStatus()).isEqualTo(TodoStatus.TODO);
        assertThat(saved.getTitle()).isEqualTo("学 Spring Boot 4");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void accessForeignTodo_throwsNotFound() {
        Todo foreign = new Todo();
        foreign.setId(1L);
        foreign.setUserId(999L);
        when(todoMapper.selectById(1L)).thenReturn(foreign);

        assertThatThrownBy(() -> todoService.get(1L, USER_ID))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", "TODO_NOT_FOUND");
        // 越权场景下不允许任何写操作
        assertThatThrownBy(() -> todoService.delete(1L, USER_ID))
                .isInstanceOf(BizException.class);
        verify(todoMapper, never()).deleteById(anyLong());
    }

    @Test
    void changeStatus_updatesOnlyStatusAndTimestamp() {
        Todo owned = new Todo();
        owned.setId(7L);
        owned.setUserId(USER_ID);
        owned.setTitle("旧标题");
        owned.setStatus(TodoStatus.TODO);
        owned.setUpdatedAt(LocalDateTime.now().minusDays(1));
        when(todoMapper.selectById(7L)).thenReturn(owned);

        todoService.changeStatus(7L, new TodoStatusUpdateRequest(TodoStatus.DONE), USER_ID);

        assertThat(owned.getStatus()).isEqualTo(TodoStatus.DONE);
        assertThat(owned.getTitle()).isEqualTo("旧标题");
        verify(todoMapper).updateById(owned);
    }

    @Test
    void page_mapsMybatisPlusPageToPageResult() {
        Todo a = todo(1L, "第一件事");
        Page<Todo> page = new Page<>(1, 10);
        page.setRecords(List.of(a));
        page.setTotal(1);
        when(todoMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<TodoVO> result = todoService.page(1, 10, null, null, USER_ID);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("第一件事");
    }

    @Test
    void invalidStatusFilter_throwsBadRequest() {
        assertThatThrownBy(() -> todoService.page(1, 10, "archive", null, USER_ID))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_STATUS");
    }

    private static Todo todo(Long id, String title) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setUserId(USER_ID);
        todo.setTitle(title);
        todo.setStatus(TodoStatus.TODO);
        return todo;
    }
}
