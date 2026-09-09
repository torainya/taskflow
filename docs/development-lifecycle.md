# 全生命周期实践指南（DevLifecycle）

> 这份文档回答一个核心问题：**一个「能上线的网站」是怎么一步步被做出来的？**
> 以 TaskFlow 为标本，从需求到运维，每一阶段给出「方法论 + 本项目里的落地位置」，并附一组循序渐进的练习。

---

## 0. 总览：一个网站的生命周期

```
需求 → 技术选型 → 工程初始化 → 编码(分层/规范) → 测试 → Code Review
     → CI → 打包/容器化 → 部署 → 监控/日志 → 版本演进(回到需求)
                    ↑_______________________|
```

每一环 TaskFlow 都有真实落点，找不到对应文件就是还没做 —— 那正是给你的练习（见第 10 章）。

---

## 1. 需求与范围（PRD）

**方法论**：先写一页纸需求再动手。回答三个问题：给谁用？解决什么？第一版必须有什么 / 可以砍什么？

**TaskFlow 的 PRD（示意）**：

| 项 | 内容 |
| --- | --- |
| 用户故事 | 作为个人用户，我能创建待办、标记状态，并且只能看到自己的数据 |
| MVP 范围 | 注册/登录、待办 CRUD、分页筛选、统计、演示数据 |
| 明确不做 | 协作共享、附件、提醒推送、团队空间（后续版本再说） |
| 验收口径 | 前端可完成「注册 → 建任务 → 改状态 → 删除」全流程；接口带鉴权 |

**练习**：为「标签分类」功能写一页 PRD（字段、接口、页面改动、验收标准）。

---

## 2. 技术选型（决策记录）

**方法论**：个人项目选型的优先级建议 —— **生态主流 > 个人熟悉度 > 满足业务 > 最小依赖**。每个关键选型记一行"为什么"（ADR，Architecture Decision Record）。

**TaskFlow 的选型记录**：

| 决策 | 选择 | 理由（一句话） |
| --- | --- | --- |
| 框架 | Spring Boot 4.1 | 当前最新稳定主线（3.5 已于 2026-06 EOL），Framework 7 / Security 7 全系配套 |
| 语言 | Java 21 | 主流 LTS；Boot 4 支持 17~26，21 生态最稳（Lombok/工具链全兼容） |
| ORM | MyBatis-Plus 3.5.17 + `spring-boot4-starter` | 国内主流；单表 CRUD 免写 SQL、内置分页插件；Boot 4 官方 starter 自 3.5.13 起提供 |
| 数据库 | PostgreSQL（生产）/ H2 PG 模式（本地/测试） | 生产同构数据库 + 零依赖开发，一套 SQL 两边跑（建表刻意只用标准子集） |
| 迁移 | Flyway | 表结构像代码一样版本化管理，CI/生产一致 |
| 鉴权 | Spring Security + JWT 无状态 | 前后端分离 + 容器化场景最合适，无 Session 粘滞问题 |
| 文档 | springdoc-openapi 3.x | Boot 4 配套线；Swagger UI 即接口调试台 |
| 前端 | Vue3 + Vite + Pinia + Router + Element Plus | 国内社区与资料最丰富；Pinia/Router 为官方推荐 |
| 构建/部署 | Docker Compose + Nginx | 单机即生产形态，个人服务器一条 `compose up` 搞定 |

**练习**：把「要不要上 Redis 缓存统计接口」「要不要拆微服务」各写一段 ADR，给出你的结论。

---

## 3. 工程初始化与编码规范

**方法论**：骨架先行，规范写进代码而非口头。

- **分层与依赖方向**：`controller → service → mapper/domain`，禁止反向与跨层调用。
  - controller：参数接收、`@Valid`、DTO 出入 —— 见 `controller/TodoController.java`
  - service：业务规则与事务（`@Transactional`）—— 见 `service/TodoService.java`
  - mapper：SQL（含注解 SQL 示例）—— 见 `mapper/TodoMapper.java#countByStatus`
  - **实体不直接外抛**：一律转 `dto/...VO`，避免实体字段变更污染接口契约
- **异常与错误码**：业务抛 `BizException(status, code, message)`，`GlobalExceptionHandler` 统一转 JSON；越权一律 404（不暴露他人资源存在性）
- **命名/格式**：`.editorconfig` 统一缩进；Java 包名 `com.taskflow.*`；REST 资源用复数名词
- **Git 工作流（个人也建议养成）**：
  - 主干 `main` 永远可发布；新功能开 `feature/xxx` 分支
  - commit 用规范前缀：`feat:` `fix:` `docs:` `test:` `refactor:` `chore:`（Conventional Commits）
  - 合回主干走 PR（即使一个人，也给自己留一次 review 的机会）

**练习**：给 TodoService 加一个 `@Transactional` 的多步骤业务（如"一键完成并归档"），观察异常时数据是否回滚。

---

## 4. 本地开发体验

| 场景 | 命令 | 说明 |
| --- | --- | --- |
| 有 Docker，想连真库 | `docker compose up -d db` | 只起 PostgreSQL |
| 没装 Docker | `mvn spring-boot:run -Dspring-boot.run.profiles=h2` | H2 PG 模式，秒起 |
| 前端热更新 | `cd frontend && npm run dev` | Vite 代理 `/api` → 8080 |
| 调试接口 | Swagger UI / IDEA HTTP Client / curl | 见 README 第五章 |

**练习**：写一个 `backend.http`（IDEA HTTP Client）文件，把注册→登录→建待办串起来，成为可重复的手工冒烟脚本。

---

## 5. 质量保障（测试策略）

**方法论**：测试金字塔 —— 大量快的单元测试 + 少量真的集成测试。

| 层 | 工具 | TaskFlow 示例 | 特点 |
| --- | --- | --- | --- |
| 单元测试 | JUnit 5 + Mockito | `AuthServiceTest` / `TodoServiceTest` | 毫秒级，无数据库 |
| 集成测试 | MockMvc + H2 | `TaskflowApplicationTests` | 真 HTTP 链路：鉴权→注册→CRUD→越权 |

关键点：**测试不依赖 Docker**（H2 内存库），所以 CI 和任何机器上 `mvn test` 都能跑。未来若想验证 PostgreSQL 专有行为，可引入 Testcontainers 作为可选 profile。

**练习**：
1. 给 `TodoService.page` 补一个"关键词命中描述（而非标题）"的用例；
2. 跑一次 `mvn test -q`，观察：改坏一个 Service 方法 → 哪些测试先拦住你（这就是测试的价值）。

---

## 6. CI / CD（自动化的大门）

`github/workflows/ci.yml` 当前三件事：**后端测试 → 前端构建 → 镜像可构建**。推 `main` 或 PR 自动触发。

进阶方向（按需加，别一次堆满）：
1. **制品**：后端 jar / 前端 dist 上传 Artifact，PR 里可下载验证
2. **安全扫描**：`OWASP Dependency-Check` 查依赖漏洞；`gitleaks` 防密钥入库
3. **发布 CD**：tag `v*` 触发 → 构建镜像推送 GHCR → 服务器 webhook 拉取重启
4. **真库集成测试**：CI 里跑一个 Postgres service 容器，替换 H2 跑关键用例

**练习**：给 CI 加一个 `backend` job 的测试覆盖率报告（`jacoco` 插件 + 上传 artifact）。

---

## 7. 打包与部署（单机即生产）

**目标形态**（TaskFlow 的 `docker-compose.yml` 已是此形态）：

```
浏览器 → Nginx(:8081，静态+Vue，/api 反代) → Spring Boot(:8080) → PostgreSQL(:5432)
```

- `deploy/backend.Dockerfile`：Maven 多阶段构建 → 瘦 JRE 运行（分层缓存依赖，重建很快）
- `deploy/frontend.Dockerfile`：Node 构建 → Nginx 托管静态资源 + SPA history 回退
- 上线检查单：改 `JWT_SECRET`（`.env`）→ 关种子数据 `SEED_DEMO_DATA=false` → 数据库挂持久卷 → 设 `restart: unless-stopped`

**备份与回滚**：升级前 `docker compose exec db pg_dump -U taskflow taskflow > backup.sql`；镜像保留上一 tag，回滚 = 指回旧 tag 重启。

**练习**：买一台轻量云服务器（或本机装 Docker），把 compose 跑起来，再从外网访问一次 —— 完成第一次"部署"。

---

## 8. 运行监控与安全

**监控（当前已具备）**：
- 健康检查：`/actuator/health`（compose 与 Nginx 已用）
- 日志：容器 `docker compose logs -f backend`；日志分级已配好（业务包 INFO）

**安全清单（务必逐条过）**：
- [ ] 生产 `JWT_SECRET` 已覆盖且≥32字节随机值
- [ ] 数据库账号不是超级用户、密码已改
- [ ] `SEED_DEMO_DATA=false`（演示账号下线）
- [ ] 越权测试：用 A 账号 token 访问 B 的资源应 404（集成测试已覆盖该断言）
- [ ] CORS 白名单收敛到真实域名
- [ ] 依赖漏洞：`mvn org.owasp:dependency-check-maven:check`（可挂 CI）

**练习**：给后端日志加 requestId（MDC + Logback 过滤器），让每条日志都能串起一次请求 —— 这是可观测性的第一课。

---

## 9. 版本管理

- **语义化版本**：`0.1.0-SNAPSHOT`（pom.xml / package.json 同步）
  - `MAJOR`：破坏性变更（如换 ORM、改接口契约）
  - `MINOR`：新增功能向后兼容（如加"标签"）
  - `PATCH`：修 bug
- **变更记录**：`CHANGELOG.md` 按版本记录（可用 `git log --oneline` 辅助生成）
- **升级大版本的正确姿势**（以 Spring Boot 为例）：先升到当前小版本最新 → 清掉 Deprecated 告警 → 再跨大版本 → 全量跑测试。**切忌跳级**（官方迁移指南如此要求）

---

## 10. 演进练习（Roadmap）

按难度递增，任选一条路把 TaskFlow 变成你自己的作品：

| # | 功能 | 涉及层 | 会练到什么 |
| --- | --- | --- | --- |
| 1 | 待办打标签 + 按标签筛选 | 后端(迁移/查询) + 前端 | Flyway 加表、多表查询、前端联动 |
| 2 | 回收站（软删除 7 天） | 后端 | 逻辑删除、定时任务(`@Scheduled`) |
| 3 | 过期提醒邮件 | 后端 | 集成邮件、异步、失败重试 |
| 4 | 统计接口上 Redis 缓存 | 后端 | 缓存一致性、失效策略 |
| 5 | 导出 Excel 报表 | 后端 + 前端 | 文件下载、字节流 |
| 6 | 把待办"分享为公开链接" | 全栈 | 只读 token、限流、防刷 |
| 7 | 前端国际化 / 暗色主题 | 前端 | Element Plus 主题定制 |
| 8 | 加 CI 真库集成测试 | DevOps | Testcontainers、GitHub Actions service |

每完成一个，走一遍：**分支 → 编码 → 单测 → PR → CI 绿 → 发版** —— 你会自然形成肌肉记忆，这正是"体验全生命周期"的终点。

---

*建议顺序：先读 README 把项目跑起来 → 照着第 5 章做测试练习 → 再挑第 10 章第一个功能完整走一遍流程。*
