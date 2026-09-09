package com.taskflow.service;

import com.taskflow.domain.Todo;
import com.taskflow.domain.TodoStatus;
import com.taskflow.domain.User;
import com.taskflow.mapper.TodoMapper;
import com.taskflow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 演示数据初始化：首次启动（用户表为空）时创建 demo 账号与几条示例待办。
 * 生产环境通过 SEED_DEMO_DATA=false 关闭。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final TodoMapper todoMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (userMapper.selectCount(null) > 0) {
            return; // 已有数据则跳过，保证幂等
        }
        User demo = new User();
        demo.setUsername("demo");
        demo.setPassword(passwordEncoder.encode("Demo123456"));
        demo.setNickname("演示账号");
        LocalDateTime now = LocalDateTime.now();
        demo.setCreatedAt(now);
        demo.setUpdatedAt(now);
        userMapper.insert(demo);

        insertSample(demo.getId(), "注册一个账号", "体验注册、登录与 JWT 鉴权全流程", TodoStatus.DONE, LocalDate.now().minusDays(1), now);
        insertSample(demo.getId(), "跑通第一个后端接口", "用 Swagger UI 或 curl 调用 GET /api/todos", TodoStatus.DONE, LocalDate.now(), now);
        insertSample(demo.getId(), "给项目加 CI 流水线", "把 GitHub Actions 跑绿", TodoStatus.IN_PROGRESS, LocalDate.now().plusDays(2), now);
        insertSample(demo.getId(), "部署到一台云服务器", "Nginx + Docker Compose 一把梭", TodoStatus.TODO, LocalDate.now().plusDays(7), now);

        log.info("已写入演示账号 demo / Demo123456 及示例待办（关闭方式：SEED_DEMO_DATA=false）");
    }

    private void insertSample(Long userId, String title, String description, TodoStatus status,
                              LocalDate dueDate, LocalDateTime now) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setStatus(status);
        todo.setDueDate(dueDate);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        todoMapper.insert(todo);
    }
}
