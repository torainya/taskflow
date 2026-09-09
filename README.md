# TaskFlow · 任务待办

> 一个「麻雀虽小、五脏俱全」的 Java 全栈示例项目 —— 用来体验**一个网站从 0 到 1 再到大版本演进**的完整生命周期。业务是最简单的任务待办，工程能力一项不少。

[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-blue)](.github/workflows/ci.yml)

---

## 一、为什么做这个项目

- **麻雀虽小**：业务只做「任务待办」，几十行核心逻辑；
- **五脏俱全**：注册登录、JWT 鉴权、数据隔离、CRUD + 分页 + 筛选 + 统计、参数校验、统一错误码、数据库迁移、接口文档、健康检查、单元/集成测试、CI 流水线、容器化一键部署、种子数据……一个不少；
- **技术主流**：下面选型均为 2026-09 时的当前稳定主线版本；
- **适合个人维护**：单体 + 前后端分离各一个目录、无微服务/消息队列等重依赖，一个人完全 hold 得住。

## 二、技术栈（版本均取当前主线）

| 层 | 选型 | 版本 | 说明 |
| --- | --- | --- | --- |
| 语言 | Java | 21 (LTS) | 主流 LTS，生态最稳 |
| 后端框架 | Spring Boot | 4.1.1 | 当前最新稳定版（3.5 已 EOL，Boot 4 基于 Spring Framework 7 / Security 7） |
| 持久层 | MyBatis-Plus | 3.5.17 | 国内主流；Boot 4 用官方 `mybatis-plus-spring-boot4-starter` |
| 安全 | Spring Security + JWT (jjwt 0.12) | - | 无状态鉴权 |
| 数据库 | PostgreSQL 16（生产形态） | 16 | docker compose 一键启动 |
| 本地库 | H2（PostgreSQL 兼容模式） | - | 零依赖跑测试与本地开发 |
| 迁移 | Flyway | Boot 托管 | `V1__init.sql` 版本化建表 |
| 接口文档 | springdoc-openapi | 3.1.1 | Swagger UI 在线调试 |
| 可观测 | Spring Boot Actuator | - | `/actuator/health` 健康检查 |
| 测试 | JUnit 5 + Mockito + MockMvc | - | 单测不需要数据库 |
| 前端 | Vue 3 + Vite + Pinia + Vue Router + Element Plus | 3.5 / 8 / 4 / 5 / 2.14 | 当前主流前端全家桶 |
| HTTP | Axios | 1.20 | 拦截器统一带 token / 报错 |
| DevOps | Docker Compose + Nginx + GitHub Actions | - | 一键起全栈 + CI |

**取舍说明**：刻意不引入 Redis、消息队列、微服务拆分、K8s —— 个人 demo 阶段它们只会增加维护负担。等业务真的需要（如缓存热点、异步任务）再演进，这条路在 `docs/development-lifecycle.md` 里也给了指引。

## 三、目录结构

```
taskflow/
├─ backend/                    # Spring Boot 后端
│  ├─ src/main/java/com/taskflow/
│  │  ├─ controller/           # 表现层（只做参数接收与响应）
│  │  ├─ service/              # 业务层（含事务、数据隔离）
│  │  ├─ mapper/               # MyBatis-Plus Mapper（含注解 SQL）
│  │  ├─ domain/               # 实体与枚举
│  │  ├─ dto/                  # 请求/响应对象（不把实体直接外抛）
│  │  ├─ common/               # 通用（分页结果等）
│  │  ├─ exception/ + web/     # 业务异常 + 全局异常处理
│  │  ├─ security/             # JWT 过滤器 / 鉴权错误
│  │  └─ config/               # 安全 / MyBatis-Plus / OpenAPI / 配置属性
│  └─ src/main/resources/
│     ├─ application.yml       # 默认连 PostgreSQL
│     ├─ application-h2.yml    # 本地零依赖 profile
│     └─ db/migration/         # Flyway 迁移脚本
├─ frontend/                   # Vue3 前端
│  └─ src/{views,stores,router,api}
├─ deploy/                     # Dockerfile × 2 + nginx.conf
├─ docs/development-lifecycle.md   # 全生命周期实践指南（必读）
├─ docker-compose.yml          # 一键启动 PostgreSQL + 后端 + 前端
└─ .github/workflows/ci.yml    # 三阶段 CI
```

## 四、快速开始

### 方式 A：一键容器化（最接近生产，推荐先体验这个）

前置：安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 并启动。

```bash
cd taskflow
docker compose up -d --build
```

| 入口 | 地址 |
| --- | --- |
| 前端界面 | http://localhost:8081 |
| 后端 API 文档（Swagger） | http://localhost:8080/swagger-ui.html |
| 健康检查 | http://localhost:8080/actuator/health |

演示账号（启动时自动播种，可注册新账号）：`demo` / `Demo123456`

### 方式 B：本地开发 · 零依赖数据库（没装 Docker 也能跑）

> 已装 IntelliJ IDEA 2025.x 的话零配置：`File → Open` 选择 `backend/pom.xml`（用自带 JBR JDK21），Run Configuration 的 **Active Profiles 填 `h2`** 后直接跑 `TaskflowApplication`。命令行方式如下：

后端（需 JDK 21 + Maven，或用 IDEA 直接跑）：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

前端（需 Node 18+）：

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173 （Vite 已把 `/api` 代理到 8080，无跨域问题）。

### 方式 C：标准前后端分离（后端连 Docker 里的 PostgreSQL）

```bash
docker compose up -d db          # 只起数据库
cd backend && mvn spring-boot:run # 默认 profile 即连 localhost:5432
cd frontend && npm install && npm run dev
```

## 五、API 一览（细节见 Swagger）

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册（自动登录返回 token） | 公开 |
| POST | `/api/auth/login` | 登录 | 公开 |
| GET | `/api/todos` | 分页查询（status/keyword 过滤） | 登录 |
| GET | `/api/todos/stats` | 各状态数量统计 | 登录 |
| GET | `/api/todos/{id}` | 详情（越权返回 404） | 登录 |
| POST | `/api/todos` | 新建 | 登录 |
| PUT | `/api/todos/{id}` | 整体更新 | 登录 |
| PATCH | `/api/todos/{id}/status` | 仅流转状态 | 登录 |
| DELETE | `/api/todos/{id}` | 删除 | 登录 |

错误统一为 `{ "code", "message", "timestamp" }`，例如 `401 / UNAUTHORIZED`、`409 / USERNAME_TAKEN`、`400 / VALIDATION_ERROR`、`404 / TODO_NOT_FOUND`。

## 六、测试

```bash
cd backend
mvn test          # 不需要 Docker / 数据库
```

覆盖三层：

1. **单元测试**（Mockito）：`AuthServiceTest`、`TodoServiceTest` —— 校验密码、重复用户名、越权 404、状态流转等；
2. **全链路集成测试**（MockMvc + H2）：`TaskflowApplicationTests` —— 未登录 401 → 注册 → 登录 → 建/查/改/状态/删 → 校验 400 → 用户间越权 404，模拟真实 HTTP 请求；
3. **前端**：`npm run build` 生产构建即冒烟验证（可在 CI 中看到）。

## 七、CI / CD

`.github/workflows/ci.yml` 三段流水线，推 main 或开 PR 即跑：

1. **backend**：`mvn test`（单测 + H2 集成测试）；
2. **frontend**：`npm run build` 构建验证；
3. **images**：`docker compose build` 校验两份 Dockerfile 可产出镜像。

> CD 建议：镜像推到 Docker Hub/GHCR → 服务器 `docker compose pull && up -d`，或用轻量面板（如 1Panel / Portainer）。上线清单见 `docs/development-lifecycle.md` 第八章。

## 八、环境变量

| 变量 | 默认 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskflow` | 数据库地址 |
| `DB_USERNAME` / `DB_PASSWORD` | `taskflow` | 数据库账号（compose 里同 `POSTGRES_*`） |
| `JWT_SECRET` | 开发默认值 | **生产必须覆盖**，≥32 字节 |
| `JWT_EXPIRE_HOURS` | `24` | token 有效期 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | 允许的前端来源 |
| `SEED_DEMO_DATA` | `true` | 首次启动播种演示账号与示例待办 |

## 九、常见问题

- **5432 端口被占**：`docker compose up -d db` 前把 compose 里 `DB_PORT` 改成如 `5433:5432`，后端 `DB_URL` 相应调整。
- **数据库连不上**：确认容器健康（`docker compose ps`）；后端在数据库健康后才启动，偶发需 `docker compose restart backend`。
- **改 JWT 密钥后老 token 失效**：正常现象，重新登录即可。
- **IDEA 直接跑后端**：Run Configuration 的 Active Profiles 填 `h2` 即可零依赖启动。

## 十、把它变成你自己的项目

想接着练手？`docs/development-lifecycle.md` 给出从「需求 → 选型 → 开发 → 测试 → CI → 发布 → 监控」的完整方法论，并内置一组循序渐进的功能练习（标签分类、回收站、分享链接、缓存热点、导出报表……）。
