# 技术计划 —— Gradescope Spring Boot

> **状态**: 动态文档 | **最后更新**: 2026-06-25  
> **用途**: 本项目唯一的权威来源（Single Source of Truth）。所有架构决策、功能需求、技术栈选择和编码规范均记录于此。如有疑问，优先查阅本文档。本文档随项目演进持续更新。

---

## 目录

1. [项目愿景](#1-项目愿景)
2. [技术栈](#2-技术栈)
3. [系统架构](#3-系统架构)
4. [核心概念与术语](#4-核心概念与术语)
5. [按模块划分的功能需求](#5-按模块划分的功能需求)
   - [5.11 前端页面模块](#511-前端页面模块)
6. [数据库设计](#6-数据库设计)
7. [API 设计规范](#7-api-设计规范)
   - [7.4 前后端交互规范](#74-前后端交互规范)
8. [安全策略](#8-安全策略)
   - [8.5 前端安全](#85-前端安全)
9. [代码规范与分层规则](#9-代码规范与分层规则)
   - [9.4 前端代码规范](#94-前端代码规范)
10. [测试策略](#10-测试策略)
    - [10.4 前端测试](#104-前端测试)
11. [演进路线图](#11-演进路线图)
12. [文档变更日志](#12-文档变更日志)

---

## 1. 项目愿景

构建一个**类 Gradescope 的教学管理系统**，面向高校教学场景。系统支持多角色（学生、助教、教师、管理员），覆盖课程全生命周期管理、作业发布、多版本提交、迟交检测、基于评分标准的打分以及课程资料分发。

**目标用户**: 高校教师、助教、学生及系统管理员。

**核心价值**:
- 简化的作业提交流程与版本追踪。
- 透明的评分机制，支持评语和评分标准。
- 基于角色的访问控制，确保课程间数据隔离。
- 所有评分操作的可审计追踪。

---

## 2. 技术栈

### 2.1 当前技术栈（已锁定）

| 层级 | 技术 | 版本 | 选用理由 |
|------|------|------|----------|
| 后端语言 | Java | 17 | 长期支持版本，现代语言特性，行业标准。 |
| 后端框架 | Spring Boot | 3.3.5 | 生态成熟，内置自动配置。 |
| ORM | MyBatis | 3.0.3 | 基于 XML 的 SQL，便于复杂查询的精细控制。 |
| 数据库 | MySQL | 8.x | 满足 ACID，大规模验证，支持 UTF-8 MB4。 |
| 安全 | Spring Security | 6.x | Spring 生态标准，过滤器链模型。 |
| 认证令牌 | JJWT | 0.12.7 | 紧凑、无状态、REST API 行业标准。 |
| 构建工具 | Maven | 3.8+ | 依赖管理，插件生态丰富。 |
| 工具 | Lombok | 1.18.30 | 减少样板代码；需配置注解处理器。 |
| 校验 | Jakarta Validation | — | 通过 `@Valid` 进行标准 Bean 校验。 |
| **前端框架** | **Vue 3** | **3.4+** | **渐进式框架，Composition API 逻辑清晰，生态丰富。** |
| **前端构建** | **Vite** | **5.x** | **极速冷启动，原生 ESM，现代前端标准。** |
| **前端组件库** | **Element Plus** | **2.7+** | **面向 Vue 3 的企业级组件库，与教学管理场景契合。** |
| **前端路由** | **Vue Router** | **4.x** | **官方路由，支持导航守卫与懒加载。** |
| **前端状态** | **Pinia** | **2.x** | **官方推荐，TypeScript 友好，比 Vuex 更轻量。** |
| **HTTP 客户端** | **Axios** | **1.7+** | **拦截器支持统一 Token 注入与错误处理。** |
| **前端语言** | **TypeScript** | **5.x** | **静态类型检查，提升可维护性，工业标准。** |

### 2.2 未来引入技术（已规划）

| 技术 | 用途 | 预计里程碑 |
|------|------|-----------|
| **PageHelper**（MyBatis）| 列表接口分页 | 里程碑 4 |
| **Redis** | 会话缓存、限流、热点数据缓存 | 里程碑 6 及以后 |
| **本地文件存储 / MinIO** | 作业与提交文件存储 | 里程碑 7 |
| **Spring AOP** | 日志、审计追踪、方法级权限检查 | 里程碑 5 |
| **MapStruct**（可选）| 类型安全的 DTO/VO/Entity 转换，替代 `BeanUtils` | 里程碑 3 |
| **Nginx** | 前端静态资源托管、反向代理、跨域统一处理 | 里程碑 8 |
| **Docker Compose** | 一键拉起前后端 + MySQL 完整环境 | 里程碑 10 |

### 2.3 明确排除的技术

- **JPA / Hibernate**: 明确排除。选用 MyBatis XML 以保证 SQL 透明度和复杂查询控制能力。
- **Redis**（当前阶段）: 在文件上传或高频缓存需求出现前暂不引入。
- **MongoDB**: 关系型数据模型契合业务领域；无需文档数据库。
- **Kafka / RabbitMQ**: 当前规模无实时事件流需求。

---

## 3. 系统架构

### 3.1 分层架构（严格）

```
┌─────────────────────────────────────────────────────────────┐
│                      前端层（Frontend）                       │  ← Vue 3 SPA，Element Plus 组件
│         （页面路由、状态管理、表单校验、文件上传）              │
├─────────────────────────────────────────────────────────────┤
│                      Axios HTTP 客户端                        │  ← 统一拦截 JWT、错误处理、loading
├─────────────────────────────────────────────────────────────┤
│                      后端控制层（Controller）                  │  ← HTTP 出入、DTO 校验、URL 映射
│                 （REST API，@RestController）                  │
├─────────────────────────────────────────────────────────────┤
│                      后端业务层（Service）                     │  ← 业务逻辑、事务边界
│                   （接口 + 实现类，@Service）                   │
├─────────────────────────────────────────────────────────────┤
│                      后端数据访问层（Mapper）                   │  ← 数据访问、SQL 执行
│                     （接口 + XML，@Mapper）                     │
├─────────────────────────────────────────────────────────────┤
│                      实体层（Entity）                         │  ← 数据库映射
│                （POJO，字段与数据库列对应）                      │
└─────────────────────────────────────────────────────────────┘
         ↕
┌─────────────────────────────────────────────────────────────┐
│              DTO / VO / Converter 层                         │  ← 跨层数据传输对象
│           （请求 DTO、响应 VO、转换器负责转换）                  │
└─────────────────────────────────────────────────────────────┘
```

**前端架构细节**:
- **单页应用（SPA）**: Vue 3 + Vue Router，所有页面无刷新跳转。
- **状态管理**: Pinia 存储用户信息、JWT 令牌、当前课程上下文。
- **组件分层**:
  - `views/`: 页面级组件，对应路由。
  - `components/`: 可复用业务组件（如作业卡片、提交列表）。
  - `layouts/`: 布局组件（侧边栏导航、顶部栏）。
- **HTTP 层封装**: `src/api/` 目录，按模块划分 `auth.ts`、`course.ts`、`assignment.ts` 等，统一处理 `Authorization` 头部和 401/403 响应。
- **路由守卫**: `beforeEach` 校验本地 JWT 有效性，无令牌则跳转登录页；根据角色动态生成侧边栏菜单。

**规则**:
- 控制层 **禁止** 直接调用 Mapper。
- 控制层 **禁止** 执行复杂对象转换；使用 Converter/BeanUtils。
- 业务层 **禁止** 将实体对象暴露给控制层；必须转换为 VO。
- 数据访问层 **禁止** 包含业务逻辑；仅限 CRUD 和查询操作。

### 3.2 安全架构

```
HTTP 请求
    → JwtAuthenticationFilter（提取并校验 JWT）
    → SecurityContextHolder（设置认证信息）
    → 鉴权（URL 级 + 方法级角色检查）
    → Controller
```

- 无状态会话（无服务端会话存储）。
- 所有认证信息通过 JWT `Authorization: Bearer <token>` 携带。
- 基于角色的访问控制（RBAC）在 Spring Security 层强制执行。

### 3.3 前后端交互架构

```
┌─────────────┐      Axios (HTTP)       ┌─────────────┐
│   Vue 3     │  ───────────────────→   │ Spring Boot │
│   前端 SPA   │  ←───────────────────   │   后端 API   │
└─────────────┘   JSON + JWT Bearer     └─────────────┘
       ↓                                      ↓
 localStorage                           MySQL 数据库
 (存储 JWT)
```

- **通信协议**: RESTful HTTP/1.1，后续可升级 HTTP/2。
- **数据格式**: 统一 JSON，`Content-Type: application/json`。
- **认证方式**: 前端登录后将 JWT 存入 `localStorage`，每次请求通过 Axios 拦截器注入 `Authorization: Bearer <token>` 头部。
- **跨域处理**: 后端配置 CORS（开发阶段）或前端通过 Nginx 反向代理（生产阶段）。
- **错误处理**: 后端返回结构化 `Result<T>`，前端 Axios 拦截器统一处理 401（跳转登录）、403（无权限提示）、500（服务器错误提示）。

### 3.3 模块边界

| 模块 | 职责 | 关键实体/表 |
|------|------|------------|
| `auth` | 注册、登录、JWT 生命周期、密码管理 | `users` |
| `rbac` | 角色定义、用户角色分配 | `roles`、`user_roles` |
| `course` | 课程增删改查、选课、成员管理 | `courses`、`course_members` |
| `assignment` | 作业发布、附件管理、时间安排 | `assignments`、`assignment_files` |
| `submission` | 学生提交、文件上传、迟交检测 | `submissions`、`submission_files` |
| `grade` | 评分、评分标准、评语、成绩发布 | `grades` |
| `material` | 课程资料上传、下载、版本管理 | `course_materials` |

---

## 4. 核心概念与术语

| 术语 | 定义 |
|------|------|
| **课程（Course）** | 特定学期开设的一门课（例如"CS101 2026 春季"）。 |
| **课程成员（Course Member）** | 以特定角色（`STUDENT`、`TA`、`INSTRUCTOR`）加入课程的用户。 |
| **作业（Assignment）** | 教师/助教在课程内发布的任务。具有截止时间、总分和提交次数限制。 |
| **提交（Submission）** | 学生对作业的响应。支持多版本（通过 `submission_no` 追踪）。 |
| **迟交（Late Submission）** | 任何 `submitted_at > due_time` 的提交。受 `allow_late_submission` 标志控制。 |
| **评分（Grade）** | 评分人（助教/教师）对特定提交给出的分数和可选评语。 |
| **逻辑删除** | 记录永不物理删除；`is_deleted = 1` 标记为不可见。 |
| **DTO** | 数据传输对象：封装传入的 HTTP 请求体。 |
| **VO** | 视图对象：封装传出的 HTTP 响应体。 |

---

## 5. 按模块划分的功能需求

### 5.1 认证与用户管理（`auth` + `user`）

**当前状态**: 基础 JWT 认证已实现，需加固。

**需求清单**:
- [x] 用户注册，强制用户名唯一。
- [x] 用户登录，BCrypt 密码校验。
- [x] JWT 令牌生成（HS256，24 小时有效期）。
- [x] 受保护端点的 JWT 令牌校验。
- [x] `GET /auth/me` 从令牌获取当前用户信息。
- [ ] **全局异常处理**: `@ControllerAdvice` 将所有异常封装为 `Result<T>`。
- [ ] **输入校验**: DTO 上使用 Jakarta Validation 注解（`@NotBlank`、`@Email`、`@Size`）。
- [ ] **密码策略**: 最小长度 8 位，复杂度强制要求。
- [ ] **用户资料更新**: 自助更新邮箱、电话、头像、真实姓名。
- [ ] **用户列表（管理员）**: 分页用户列表，支持角色筛选。
- [ ] **用户状态管理（管理员）**: 禁用/启用账户。

### 5.2 RBAC（基于角色的访问控制）

**当前状态**: 硬编码 `ROLE_USER`。数据库表已存在但未使用。

**需求清单**:
- [ ] 从 `user_roles` + `roles` 表加载真实角色到 JWT 或 `UserDetails`。
- [ ] 角色层级: `ADMIN > INSTRUCTOR > TA > STUDENT`。
- [ ] 通过 `requestMatchers().hasRole(...)` 实现 URL 级授权。
- [ ] 通过 `@PreAuthorize` 实现方法级授权（例如仅限课程 `INSTRUCTOR` 创建作业）。
- [ ] 课程域权限: 用户可以在课程 A 是 `INSTRUCTOR`，同时在课程 B 是 `STUDENT`。

### 5.3 管理员模块

**需求清单**:
- [ ] 管理员仪表盘接口: 用户数、课程数、提交数统计。
- [ ] 管理员可创建/修改系统角色。
- [ ] 管理员可查看所有课程和作业（只读或覆盖权限）。

### 5.4 课程管理（`course`）

**需求清单**:
- [ ] **创建课程**: 教师创建课程，字段包括 `course_code`、`course_name`、`semester`、`description`、`location`、`schedule_info`。
- [ ] **更新课程**: 仅限创建人（`created_by`）或管理员更新。
- [ ] **删除课程**: 逻辑删除；级联隐藏但不破坏数据完整性。
- [ ] **课程列表**: 按角色区分:
  - 学生: 列出已加入的课程。
  - 教师: 列出创建或为成员的课程。
  - 管理员: 列出所有课程。
- [ ] **课程详情**: 完整元数据 + 成员列表。
- [ ] **添加成员**: 教师通过用户名/邮箱添加用户，并指定角色（`STUDENT`、`TA`、`INSTRUCTOR`）。
- [ ] **移除成员**: 从 `course_members` 软移除（`status = 0`）。
- [ ] **唯一约束**: `(course_code, semester)` 必须唯一。

### 5.5 作业管理（`assignment`）

**需求清单**:
- [ ] **创建作业**: 在课程内发布。字段: `title`、`description`、`total_score`、`due_time`、`allow_late_submission`、`max_submission_times`、`status`（草稿/已发布/已关闭）。
- [ ] **更新作业**: 仅在截止时间前或草稿状态时允许。
- [ ] **删除作业**: 逻辑删除。
- [ ] **作业列表**: 按课程查询。学生仅可见已发布作业。
- [ ] **作业详情**: 完整元数据 + 文件附件。
- [ ] **发布/草稿/关闭**: 教师控制状态转换。
- [ ] **附加文件**: 教师上传参考文件（PDF、DOCX、ZIP）到作业。

### 5.6 提交模块（`submission`）

**需求清单**:
- [ ] **提交**: 学生提交作业。系统自动递增 `submission_no`。
- [ ] **提交次数限制检查**: 若 `submission_no > max_submission_times` 则拒绝。
- [ ] **迟交检测**: 若 `submitted_at > due_time` 自动设置 `is_late = 1`；若不允许迟交则拒绝。
- [ ] **草稿保存**: 学生可在最终提交前保存草稿（`status = 0`）。
- [ ] **撤回**: 允许在可配置时间窗口内撤回。
- [ ] **我的提交列表**: 学生查看某作业的个人提交历史。
- [ ] **提交详情**: 学生查看自己的；教师/助教查看课程内任何提交。
- [ ] **附加文件**: 学生随提交上传文件。

### 5.7 评分模块（`grade`）

**需求清单**:
- [ ] **创建评分**: 助教/教师为提交打分。字段: `score`、`comment`、`status`（草稿/最终）。
- [ ] **更新评分**: 仅当非最终状态，或通过明确的"重新评分"流程。
- [ ] **每个提交一条评分**: 通过 `uk_grades_submission_id` 强制。
- [ ] **查看评分**: 学生可查看已发布的个人评分。
- [ ] **评分统计**: 教师查看每作业的平均分、中位数、最高分、最低分。
- [ ] **重新评分申请**（未来）: 学生申请重新评分，教师复核。

### 5.8 文件存储（`file`）

**需求清单**:
- [ ] **上传**: 支持多部分上传。最大大小可配置。
- [ ] **存储策略**: 本地文件系统优先（`/uploads/{course_id}/{assignment_id}/`）。未来: MinIO/OSS。
- [ ] **文件元数据**: 记录 `file_name`、`file_url`、`file_size`、`file_type`、`uploaded_by`。
- [ ] **下载**: 带角色检查的安全下载（仅限课程成员）。
- [ ] **文件类型白名单**: 允许 PDF、DOCX、ZIP、TXT、PNG、JPG。拒绝可执行文件。
- [ ] **病毒扫描**（未来）: 集成 ClamAV 扫描上传文件。

### 5.9 课程资料（`material`）

**需求清单**:
- [ ] **上传资料**: 教师上传课程资料（课件、阅读材料）。
- [ ] **资料列表**: 学生查看已加入课程的资料。
- [ ] **下载资料**: 带角色检查的安全下载。
- [ ] **删除资料**: 上传者或管理员执行逻辑删除。

### 5.10 通知（未来 / 可选）

**需求清单**:
- [ ] 新作业发布时通知学生。
- [ ] 评分发布时通知学生。
- [ ] 收到提交时通知教师。

### 5.11 前端页面模块

**前端定位**: 本项目采用 **Vue 3 单页应用（SPA）** 作为用户交互界面，与后端 REST API 通过 Axios 通信。前端不是简单的"页面拼接"，而是具备完整状态管理、路由守卫、角色化菜单和统一交互风格的独立工程。

**前端技术选型确认**:
| 技术 | 版本 | 职责 |
|------|------|------|
| Vue 3 | 3.4+ | 渐进式框架，Composition API 组织逻辑。 |
| Vite | 5.x | 构建工具，极速冷启动，热更新。 |
| TypeScript | 5.x | 全项目类型安全，接口定义与后端 DTO/VO 对齐。 |
| Element Plus | 2.7+ | UI 组件库（表格、表单、对话框、消息提示）。 |
| Vue Router | 4.x | 前端路由，支持导航守卫和懒加载。 |
| Pinia | 2.x | 状态管理（用户状态、JWT、课程上下文）。 |
| Axios | 1.7+ | HTTP 客户端，拦截器统一处理 Token 和错误。 |

#### 5.11.1 页面清单（按角色与模块）

**公共页面（无角色限制）**:
| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 用户名/密码输入，登录成功后存储 JWT 到 `localStorage` 并跳转仪表盘。 |
| 注册页 | `/register` | 用户名、密码、真实姓名、邮箱、电话、学号/工号。实时校验用户名是否已存在。 |
| 404 页 | `/404` | 友好提示，提供返回首页按钮。 |

**学生页面（`STUDENT` 角色）**:
| 页面 | 路由 | 说明 |
|------|------|------|
| 学生仪表盘 | `/student/dashboard` | 展示已加入课程列表、即将截止的作业、最新成绩。 |
| 课程详情-学生视角 | `/courses/:courseId` | 课程信息、成员列表（仅看自己）、资料列表、作业列表。 |
| 作业详情-学生视角 | `/courses/:courseId/assignments/:assignmentId` | 作业描述、附件下载、提交入口、历史提交记录、最新评分。 |
| 提交作业 | `/courses/:courseId/assignments/:assignmentId/submit` | 文本输入框 + 文件上传组件。显示剩余提交次数和截止时间倒计时。迟交警告（红色提示）。 |
| 我的成绩 | `/student/grades` | 所有课程的成绩汇总，支持按学期筛选。 |
| 个人中心 | `/profile` | 修改个人资料、修改密码、上传头像。 |

**教师/助教页面（`INSTRUCTOR` / `TA` 角色）**:
| 页面 | 路由 | 说明 |
|------|------|------|
| 教师仪表盘 | `/instructor/dashboard` | 管理的课程列表、待评分提交数量、最近学生活动。 |
| 课程管理 | `/instructor/courses` | 创建课程、编辑课程、管理课程成员（添加/移除/修改角色）。 |
| 课程详情-教师视角 | `/instructor/courses/:courseId` | 课程概览、成员管理（完整列表）、资料管理、作业列表。 |
| 作业管理 | `/instructor/courses/:courseId/assignments` | 创建作业、编辑作业、发布/关闭作业、上传参考文件。 |
| 作业详情-教师视角 | `/instructor/courses/:courseId/assignments/:assignmentId` | 作业统计（提交人数、未提交人数）、所有学生提交列表。 |
| 评分页面 | `/instructor/courses/:courseId/assignments/:assignmentId/submissions/:submissionId/grade` | 查看学生提交内容（文本 + 文件），输入分数和评语，保存草稿或发布最终成绩。 |
| 成绩统计 | `/instructor/courses/:courseId/assignments/:assignmentId/statistics` | 分数分布直方图、平均分、中位数、最高分、最低分。 |

**管理员页面（`ADMIN` 角色）**:
| 页面 | 路由 | 说明 |
|------|------|------|
| 管理员仪表盘 | `/admin/dashboard` | 系统统计卡片（用户数、课程数、作业数、提交数）、最近注册用户的图表。 |
| 用户管理 | `/admin/users` | 分页用户表格，支持按角色/状态筛选，启用/禁用账户操作。 |
| 系统角色管理 | `/admin/roles` | 查看和修改系统角色定义（未来扩展）。 |

#### 5.11.2 前端路由与导航守卫

- **路由守卫 (`beforeEach`)**:
  1. 检查 `localStorage` 中是否存在 JWT；不存在则跳转 `/login`（公开页面除外）。
  2. 解析 JWT 中的角色信息，若用户无权限访问目标路由（如学生访问教师页面），则跳转 403 提示页或仪表盘。
  3. 若 JWT 过期（前端通过 `exp`  claim 判断），清除存储并跳转登录页。
- **动态菜单**: 根据用户角色动态渲染侧边栏导航项。学生看不到"课程管理"，教师看不到"用户管理"。
- **面包屑**: 每个页面顶部显示面包屑导航（如：课程 > CS101 > 作业 > 期中项目）。

#### 5.11.3 前端状态管理（Pinia Stores）

| Store | 职责 |
|-------|------|
| `useAuthStore` | 存储 JWT、当前用户基本信息（id, username, roles, avatarUrl）。提供 `login()`、`logout()`、`isAuthenticated()`、`hasRole(role)` 方法。 |
| `useCourseStore` | 存储当前选中的课程上下文（courseId, courseName, myRoleInCourse）。提供 `setCurrentCourse()`、`clearCourse()`。 |
| `useAssignmentStore` | 存储作业列表、当前作业详情、提交状态。 |
| `useNotificationStore` | 存储全局消息（Element Plus `ElMessage` / `ElNotification` 的队列管理）。 |

#### 5.11.4 前端与后端 DTO/VO 对齐

前端 TypeScript 接口必须与后端 DTO/VO 严格对应：

```typescript
// 与后端 Result<T> 对齐
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 与后端 UserVO 对齐
interface UserVO {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone?: string;
  userNo?: string;
  avatarUrl?: string;
  gender?: number;
  status: number;
}
```

- 前端 `src/types/` 目录存放所有 TypeScript 接口，按模块划分文件。
- 任何后端 VO/DTO 字段变更必须同步更新前端对应接口。

#### 5.11.5 前端统一交互规范

- **Loading 状态**: 所有异步操作（API 调用）必须显示 loading（按钮 loading 或全屏遮罩）。
- **空状态**: 列表为空时显示友好空状态插图和提示文字，而非空白页。
- **表单校验**: 提交前前端即时校验（Element Plus `rules`），减少无效请求。
- **确认对话框**: 删除操作必须弹出二次确认对话框（`ElMessageBox.confirm`）。
- **错误提示**: API 返回错误时，顶部弹出 `ElNotification` 显示后端 `message` 内容。
- **成功反馈**: 操作成功后弹出 `ElMessage.success` 提示。

---

## 6. 数据库设计

### 6.1  schema 概览

完整 DDL 见 `mysql.txt`。本节记录**设计意图**和**关系**。

```
users ||--o{ user_roles : 拥有
roles ||--o{ user_roles : 分配给
courses ||--o{ course_members : 包含
users ||--o{ course_members : 加入
courses ||--o{ assignments : 包含
assignments ||--o{ assignment_files : 拥有
assignments ||--o{ submissions : 接收
submissions ||--o{ submission_files : 拥有
submissions ||--o{ grades : 被评分
courses ||--o{ course_materials : 包含
```

### 6.2 关键设计决策

| 决策 | 理由 |
|------|------|
| **逻辑删除（`is_deleted`）** | 保留审计追踪；支持软恢复。 |
| **独立的 `course_members` 表** | 支持同一用户在不同课程拥有不同角色。 |
| **`submission_no` 按（作业、学生）自增** | 无需额外表即可追踪版本历史。 |
| **`uk_grades_submission_id`** | 每个提交一条最终评分；简化数据模型。 |
| **外键使用 RESTRICT** | 防止误删被引用的数据。 |
| **所有表均含审计字段** | `created_at`、`updated_at` 处处可追溯。 |

---

## 7. API 设计规范

### 7.1 URL 约定

| 模式 | 示例 | 用途 |
|------|------|------|
| `GET /{资源}s` | `GET /courses` | 列表（分页） |
| `GET /{资源}s/{id}` | `GET /courses/1` | 详情 |
| `POST /{资源}s` | `POST /courses` | 创建 |
| `PUT /{资源}s/{id}` | `PUT /courses/1` | 全量更新 |
| `PATCH /{资源}s/{id}` | `PATCH /courses/1` | 部分更新 |
| `DELETE /{资源}s/{id}` | `DELETE /courses/1` | 逻辑删除 |
| `POST /{资源}s/{id}/action` | `POST /courses/1/enroll` | 自定义操作 |

### 7.2 请求/响应标准

**成功**:
```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

**错误**:
```json
{
  "code": 400,
  "message": "校验失败: 用户名不能为空",
  "data": null
}
```

**分页**（未来，配合 PageHelper）:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [ ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 7.3 HTTP 状态码

| 状态码 | 用途 |
|--------|------|
| 200 | 通用成功 |
| 201 | 资源创建成功 |
| 400 | 请求错误 / 校验错误 |
| 401 | 未认证（JWT 缺失或无效） |
| 403 | 禁止访问（JWT 有效但角色/权限不足） |
| 404 | 资源未找到 |
| 409 | 冲突（唯一键冲突，例如用户名已存在） |
| 500 | 服务器内部错误 |

### 7.4 前后端交互规范

#### CORS 配置
- **开发阶段**: 后端 `SecurityConfig` 或独立 `CorsConfig` 允许 `http://localhost:5173`（Vite 默认端口），允许头部 `Authorization`、`Content-Type`。
- **生产阶段**: 通过 Nginx 反向代理，前端与后端同域，无需 CORS。

#### Axios 封装规范
- **Base URL**: `import.meta.env.VITE_API_BASE_URL`，开发指向 `http://localhost:8080`。
- **请求拦截器**: 自动从 `localStorage` 读取 `token` 并注入 `Authorization: Bearer <token>`。
- **响应拦截器**:
  - `code === 200`: 直接返回 `response.data.data`。
  - `code === 401`: 清除 token，跳转 `/login`。
  - `code === 403`: 弹出无权限提示，可跳转 403 页面。
  - `code >= 500`: 弹出"服务器错误，请稍后重试"。
- **统一错误处理**: 网络错误（`error.response` 不存在）时弹出"网络连接失败"。

#### 前端 API 模块划分
```
src/api/
├── auth.ts          # 登录、注册、获取当前用户
├── user.ts          # 用户资料、用户列表（管理员）
├── course.ts        # 课程 CRUD、成员管理
├── assignment.ts    # 作业 CRUD、文件附件
├── submission.ts    # 提交、草稿、历史记录
├── grade.ts         # 评分、统计
├── material.ts      # 课程资料上传/下载
└── file.ts          # 通用文件下载
```

---

## 8. 安全策略

### 8.1 认证

- **机制**: JWT（JSON Web Token），HS256。
- **存储**: 客户端（网页应用存 localStorage；移动端存安全存储）。
- **有效期**: 默认 24 小时。后续添加刷新令牌策略。
- **传输**: 每个受保护请求通过 `Authorization: Bearer <token>` 头部传输。

### 8.2 授权

- **RBAC 模型**: 用户拥有全局角色（`ADMIN`）和课程域角色（`INSTRUCTOR`、`TA`、`STUDENT`）。
- **URL 级安全**: 在 `SecurityConfig` 中通过 `requestMatchers` 配置。
- **方法级安全**: 通过 `@PreAuthorize("hasRole('INSTRUCTOR')")` 实现细粒度控制。
- **数据级安全**: 业务层必须校验请求用户是否有权访问目标课程/提交/评分。

### 8.3 密码策略

- 哈希算法: BCrypt，强度 10（默认）。
- 最小长度: 8 个字符。
- 复杂度: 至少包含一个大写字母、一个小写字母、一个数字（在校验层强制）。

### 8.4 输入安全

- **SQL 注入**: 通过 MyBatis 参数化查询（`#{}`）防御。用户输入绝不使用 `${}`。
- **XSS**: 前端责任；后端若渲染 HTML 必须转义输出。
- **文件上传**: 扩展名白名单、大小限制、存储在 Web 根目录之外。

### 8.5 前端安全

- **JWT 存储**: 网页应用使用 `localStorage` 存储 JWT（当前阶段）。后续可升级为 `httpOnly` Cookie + 反向代理方案。
- **XSS 防护**: Vue 3 模板自动转义插值表达式；禁止在组件中使用 `v-html` 渲染用户输入内容。
- **CSRF 防护**: 后端已关闭 CSRF（无状态 API），前端无需额外处理；若未来改用 Cookie 认证，需引入 CSRF Token。
- **路由安全**: 前端路由守卫在页面跳转前校验 JWT 和角色，防止用户通过直接输入 URL 访问无权限页面。
- **敏感信息**: 前端代码中禁止硬编码 API 密钥、数据库密码等敏感配置；所有配置通过 `.env` 文件管理。

---

## 9. 代码规范与分层规则

### 9.1 包结构

```
com.example.gradescopespringboot
├── controller/{模块}/       # 每个资源一个控制器
├── service/{模块}/          # 接口
├── service/impl/{模块}/      # 实现类
├── mapper/                  # MyBatis 接口
├── entity/                  # 数据库实体（1:1 对应表）
├── dto/{模块}/              # 请求 DTO
├── vo/{模块}/               # 响应 VO
├── converter/               # 对象转换器（MapStruct 或手动）
├── common/
│   ├── result/              # Result<T>、ResultCode 枚举
│   ├── exception/           # 自定义异常（BusinessException 等）
│   └── util/                # 通用工具类
├── config/                  # Spring 配置类
└── security/                # 安全相关组件
    ├── filter/
    ├── model/
    ├── service/
    └── util/
```

### 9.2 强制规则

| 编号 | 规则 |  enforcement |
|------|------|-------------|
| 1 | 控制层仅返回 `Result<T>` 或 `Result<List<T>>`。 | 代码审查 |
| 2 | 禁止直接将实体返回给前端。 | 代码审查 |
| 3 | DTO 使用 `@Valid` 进行输入校验。 | 代码审查 |
| 4 | 多步数据库操作的服务方法声明 `@Transactional`。 | 代码审查 |
| 5 | Mapper 仅使用 XML；禁止 `@Select` 注解。 | 代码审查 |
| 6 | 使用构造器注入；避免字段上的 `@Autowired`。 | 代码审查 / Checkstyle |
| 7 | 业务层所有公共方法必须写 Javadoc。 | 代码审查 |
| 8 | XML 中 SQL 关键字大写（`SELECT`、`WHERE`）。 | 代码审查 |
| 9 | 每个查询都必须检查逻辑删除（`is_deleted = 0`）。 | 代码审查 / 单元测试 |
| 10 | 禁止 `System.out.println`；使用 SLF4J `log.info/debug/error`。 | 代码审查 |

### 9.3 命名约定

| 元素 | 约定 | 示例 |
|------|------|------|
| 数据库表 | snake_case，复数 | `course_members` |
| 实体类 | PascalCase，单数 | `CourseMember` |
| 实体字段 | camelCase | `courseId` |
| Mapper 接口 | `{实体}Mapper` | `CourseMemberMapper` |
| 业务接口 | `{实体}Service` | `CourseService` |
| 业务实现 | `{实体}ServiceImpl` | `CourseServiceImpl` |
| 控制器 | `{实体}Controller` | `CourseController` |
| DTO | `{动作}{实体}RequestDTO` | `CreateCourseRequestDTO` |
| VO | `{实体}{详情}ResponseVO` | `CourseDetailResponseVO` |

### 9.4 前端代码规范

#### 9.4.1 目录结构

```
gradescope-frontend/
├── public/                  # 静态资源（favicon、全局 CSS 变量）
├── src/
│   ├── api/                 # Axios 封装，按模块划分 API 请求
│   ├── assets/              # 图片、图标、全局样式
│   ├── components/          # 可复用业务组件
│   │   ├── common/          # 通用组件（AppHeader、AppSidebar、AppBreadcrumb）
│   │   └── course/          # 课程相关组件（CourseCard、MemberList）
│   ├── layouts/             # 布局组件（MainLayout、AuthLayout）
│   ├── router/              # Vue Router 配置
│   │   ├── index.ts         # 路由实例
│   │   └── routes.ts        # 路由定义表
│   ├── stores/              # Pinia 状态管理
│   │   ├── auth.ts
│   │   ├── course.ts
│   │   └── assignment.ts
│   ├── types/               # TypeScript 接口（与后端 VO/DTO 对齐）
│   │   ├── auth.ts
│   │   ├── user.ts
│   │   └── course.ts
│   ├── utils/               # 工具函数（日期格式化、文件大小格式化）
│   ├── views/               # 页面级组件
│   │   ├── auth/            # LoginView、RegisterView
│   │   ├── student/         # StudentDashboard、CourseDetailStudent
│   │   ├── instructor/      # InstructorDashboard、CourseManage、AssignmentManage
│   │   └── admin/           # AdminDashboard、UserManage
│   ├── App.vue
│   └── main.ts
├── .env.development         # 开发环境变量（API 基地址）
├── .env.production          # 生产环境变量
├── vite.config.ts
└── tsconfig.json
```

#### 9.4.2 前端强制规则

| 编号 | 规则 | 检查方式 |
|------|------|----------|
| 1 | 所有 API 响应必须通过 `src/api/request.ts` 中封装的 Axios 实例发起，禁止直接引入 `axios`。 | 代码审查 |
| 2 | 所有页面级组件使用 `defineComponent` 或 `<script setup>` 语法。 | ESLint |
| 3 | 组件名使用 PascalCase，多单词组合（如 `CourseDetailView`）。 | ESLint |
| 4 | 事件处理器和工具函数使用 camelCase（如 `handleSubmit`、`formatDate`）。 | ESLint |
| 5 | 所有 TypeScript 接口必须与后端 VO/DTO 字段名、类型一致。 | 代码审查 |
| 6 | 路由路径使用 kebab-case（如 `/course-management`）。 | 代码审查 |
| 7 | 禁止在组件中直接操作 `localStorage`；必须通过 `useAuthStore` 封装的方法读写 token。 | 代码审查 |
| 8 | 所有异步 API 调用必须有 `try/catch` 或统一错误拦截处理。 | 代码审查 |
| 9 | 列表页必须处理空状态、加载状态和错误状态。 | 代码审查 |
| 10 | 图片、CSS、工具函数使用绝对路径导入（通过 Vite `resolve.alias` 配置 `@/` 指向 `src/`）。 | 代码审查 |

---

## 10. 测试策略

### 10.1 测试金字塔

| 层级 | 类型 | 工具 | 覆盖率目标 |
|------|------|------|-----------|
| 单元 | 业务逻辑、工具类、转换器 | JUnit 5、Mockito | 业务逻辑 70%+ |
| 集成 | Mapper + DB、Controller + Service | `@SpringBootTest`、`@MybatisTest` | 所有 Mapper 方法、所有控制器端点 |
| 端到端 | 完整 HTTP 流程 | `auth-test.html` 手动测试，后续 Postman/Newman | 关键路径（认证、提交、评分） |

### 10.2 测试数据

- 使用 `@Sql` 或 `schema.sql/data.sql` 准备测试固件。
- 每个测试类使用独立数据集，避免跨测试污染。
- 集成测试使用 `@Transactional` 确保每次测试后回滚。

### 10.3 关键测试场景

- **认证**: 有效/无效凭据登录、过期 JWT、缺失令牌。
- **授权**: 学生访问管理员端点（预期 403）。
- **业务逻辑**: 超过最大提交次数后提交、截止后无迟交标志时提交。
- **数据完整性**: 重复用户名注册（预期 409）、`course_code` + `semester` 唯一性。

### 10.4 前端测试

| 层级 | 类型 | 工具 | 覆盖目标 |
|------|------|------|----------|
| 单元 | 组件渲染、工具函数、Store 逻辑 | Vitest + Vue Test Utils | 所有 `utils/` 函数、所有 Pinia Store |
| 集成 | 组件 + API Mock | Vitest + MSW (Mock Service Worker) | 表单提交流程、路由守卫行为 |
| E2E | 完整浏览器流程 | Playwright | 登录 → 创建课程 → 发布作业 → 提交 → 评分 全链路 |

**前端关键测试场景**:
- 登录页: 空输入校验、错误密码提示、JWT 正确存储。
- 路由守卫: 无令牌访问仪表盘 → 重定向登录；学生访问教师页面 → 重定向 403。
- 文件上传: 选择超大文件 → 前端拦截不上传；选择允许类型 → 正确显示文件名和大小。
- 响应式: 表格在移动端可横向滚动，侧边栏可折叠。

---

## 11. 演进路线图

### 第一阶段：基础（当前）
- [x] 项目脚手架搭建
- [x] 数据库 schema
- [x] 用户模块 + JWT 认证

### 第二阶段：加固
- 全局异常处理
- 输入校验
- 基于数据库真实角色的 RBAC
- 管理员端点

### 第三阶段：核心教学闭环
- 课程增删改查 + 选课
- 作业发布 + 文件附件
- 学生提交 + 迟交检测

### 第四阶段：评分与反馈
- 评分创建与更新
- 评分统计
- 学生成绩查看

### 第五阶段：资料与打磨
- 课程资料上传/下载
- 文件存储加固（类型检查、大小限制）
- 所有列表接口分页
- Redis 缓存热点数据

### 第六阶段：高级功能（未来）
- 重新评分申请
- 抄袭检测集成
- 批量导入/导出（CSV 成绩、花名册）
- 邮件通知

### 前端演进路线（与后端阶段对应）

| 阶段 | 前端任务 | 依赖后端 |
|------|---------|----------|
| 第二阶段 | Vue 3 项目脚手架、登录页、注册页、路由守卫、Pinia 状态管理、Axios 封装。 | 里程碑 0-1 |
| 第三阶段 | 学生仪表盘、课程列表/详情、作业列表/详情、提交页面（文本 + 文件）、个人中心。 | 里程碑 4-6 |
| 第四阶段 | 教师仪表盘、课程管理页、作业管理页、评分页面、成绩统计图表。 | 里程碑 4-8 |
| 第五阶段 | 管理员仪表盘、用户管理表格、课程资料页面、文件上传/下载组件。 | 里程碑 3-9 |
| 第六阶段 | 全局优化：响应式适配、暗黑模式、国际化（可选）、PWA（可选）。 | 里程碑 10 |

---

## 12. 文档变更日志

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|----------|------|
| 2026-06-25 | 1.0 | 初版创建。定义完整技术栈、模块需求、API 规范、安全策略和演进路线图。 | AI 助手 |
| 2026-06-25 | 1.1 | **新增前端规划**: 加入 Vue 3 + Vite + Element Plus + TypeScript 前端技术栈；定义完整前端页面清单（学生/教师/管理员视角）；新增前后端交互规范（CORS、Axios 封装、API 模块划分）；新增前端安全、前端代码规范、前端测试策略；更新系统架构图包含前端层；更新演进路线图加入前端阶段。 | AI 助手 |

---

> **如何更新本文档**: 添加新技术、变更架构决策或更新需求时，在第 12 节追加新条目并更新相关章节。务必更新页眉中的"最后更新"日期。
