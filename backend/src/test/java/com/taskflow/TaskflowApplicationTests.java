package com.taskflow;

import com.taskflow.dto.auth.LoginRequest;
import com.taskflow.dto.auth.RegisterRequest;
import com.taskflow.dto.todo.TodoCreateRequest;
import com.taskflow.dto.todo.TodoStatusUpdateRequest;
import com.taskflow.dto.todo.TodoUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全链路集成测试：H2(PostgreSQL 兼容模式) 内存库，无需 Docker / 外部数据库。
 * 覆盖：鉴权 401 → 注册 → 登录 → 新建/分页/详情/更新/状态流转/统计/删除 → 校验 400 → 越权 404。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class TaskflowApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- 鉴权 ----------

    @Test
    void unauthenticatedRequest_returns401Json() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void register_thenLogin_thenCrudFullFlow() throws Exception {
        // 1. 注册
        String token = register("alice", "Passw0rd123", "爱丽丝");
        assertThat(token).isNotBlank();

        // 2. 重复注册 → 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest("alice", "Passw0rd123", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_TAKEN"));

        // 3. 密码错误 → 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("alice", "wrong-pass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));

        // 4. 新建两条待办
        long firstId = createTodo(token, "写周报", "覆盖本周三条重点", LocalDate.now().plusDays(1));
        createTodo(token, "读《凤凰架构》", null, null);

        // 5. 分页查询 + 关键字过滤
        mockMvc.perform(get("/api/todos").header("Authorization", bearer(token))
                        .param("page", "1").param("size", "10").param("keyword", "周报"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("写周报"));

        // 6. 详情
        mockMvc.perform(get("/api/todos/{id}", firstId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));

        // 7. 状态流转
        mockMvc.perform(patch("/api/todos/{id}/status", firstId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TodoStatusUpdateRequest(com.taskflow.domain.TodoStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 8. 整体更新
        mockMvc.perform(put("/api/todos/{id}", firstId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TodoUpdateRequest(
                                "写周报（修订）", null,
                                com.taskflow.domain.TodoStatus.DONE, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("写周报（修订）"))
                .andExpect(jsonPath("$.description").doesNotExist());

        // 9. 统计
        mockMvc.perform(get("/api/todos/stats").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(1))
                .andExpect(jsonPath("$.total").value(2));

        // 10. 删除
        mockMvc.perform(delete("/api/todos/{id}", firstId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/todos/stats").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void userCannotTouchOthersTodos() throws Exception {
        String bob = register("bob", "Passw0rd123", "鲍勃");
        long bobTodoId = createTodo(bob, "鲍勃的私事", null, null);

        String carol = register("carol", "Passw0rd123", null);
        // 越权读 / 改 / 删别人的待办 → 统一 404
        mockMvc.perform(get("/api/todos/{id}", bobTodoId).header("Authorization", bearer(carol)))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/todos/{id}/status", bobTodoId)
                        .header("Authorization", bearer(carol))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TodoStatusUpdateRequest(com.taskflow.domain.TodoStatus.DONE))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/todos/{id}", bobTodoId).header("Authorization", bearer(carol)))
                .andExpect(status().isNotFound());

        // 本人仍然可见（未被误删）
        mockMvc.perform(get("/api/todos/{id}", bobTodoId).header("Authorization", bearer(bob)))
                .andExpect(status().isOk());
    }

    @Test
    void invalidCreate_returnsValidationError() throws Exception {
        String token = register("dave", "Passw0rd123", null);
        // 空标题 → 400 参数校验错误
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // 状态流转传入非法枚举值 → 400
        long todoId = createTodo(token, "合法的一条", null, null);
        mockMvc.perform(patch("/api/todos/{id}/status", todoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOT_A_STATUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    // ---------- helpers ----------

    private String register(String username, String password, String nickname) throws Exception {
        return registerRaw(username, password, nickname);
    }

    private String registerRaw(String username, String password, String nickname) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest(username, password, nickname))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private long createTodo(String token, String title, String description, LocalDate dueDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/todos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TodoCreateRequest(title, description, dueDate))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
