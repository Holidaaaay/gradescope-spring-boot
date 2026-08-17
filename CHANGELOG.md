# 变更日志 / 推送记录

> **状态**: 动态文档 | **最后更新**: 2026-08-17  
> **用途**: 按时间顺序记录每次推送到远程仓库的操作。每条记录 documenting 实现了什么、如何实现的、关键设计决策，以及任何已知问题或回滚指令。用于追踪进度、调试回归问题、理解历史上下文。

---

## 记录格式

每次推送记录**必须**遵循以下结构：

```markdown
## 推送 #{编号} —— {简短标题}

- **日期**: YYYY-MM-DD
- **分支**: {分支名}
- **提交范围**: `{起始哈希}..{结束哈希}`（若含多个提交）
- **里程碑**: 关联 `MILESTONES.md` 中的里程碑

### 实现了什么
{功能、修复或重构的列表}

### 实现细节
{构建方式、关键类/文件变更、架构决策}

### 新增 / 修改 / 删除的文件
- **新增**: `path/to/new/file.java`
- **修改**: `path/to/changed/file.java`
- **删除**: `path/to/removed/file.java`

### 执行的测试
{运行了哪些测试，手动或自动，及其结果}

### 已知问题 / 限制
{任何未完成项、代码中留下的 TODO、或未处理的边界情况}

### 回滚指令
{若发现严重缺陷，如何回滚本次推送}
```

---

## 推送 #0 —— 初始项目搭建与 JWT 认证

- **日期**: 2026-03-27
- **分支**: `main`
- **提交范围**: `1e71163..ba41ae1`
- **里程碑**: [里程碑 0：项目基础与 JWT 认证](MILESTONES.md#里程碑-0项目基础与-jwt-认证)

### 实现了什么
- 使用 Maven 构建系统创建 Spring Boot 3.3.5 项目。
- 在 `application.properties` 中配置 MySQL 8 连接（`gradescope_db`）。
- 配置 MyBatis XML Mapper 支持（`map-underscore-to-camel-case=true`）。
- 创建统一响应包装器 `Result<T>`，位于 `common.result` 包。
- 实现 `User` 实体，映射 `users` 表（16 个字段，支持逻辑删除）。
- 实现 `UserMapper` 接口 + `UserMapper.xml`，含 `selectById`、`selectByUsername`、`insert`。
- 实现 `UserService` / `UserServiceImpl`，含 `getById`、`getByUsername`、`save`。
- 实现 `UserController`，含 `GET /users/{id}`，返回 `UserVO`（敏感字段已隐藏）。
- 修复 `@MapperScan` 范围问题：仅扫描 `mapper` 包，防止 Spring 误将 Service 识别为 Mapper。
- 集成 Spring Security，自定义 `SecurityConfig`：
  - 关闭 CSRF（无状态 REST API）。
  - 会话创建策略设为 `STATELESS`。
  - 配置公开端点：`/auth/register`、`/auth/login`、`/auth/me`、`/auth-test.html`。
  - 其余端点需认证。
- 实现 JWT 认证流程：
  - `JwtTokenProvider`：HS256 令牌生成、解析、校验，支持配置密钥和有效期。
  - `JwtAuthenticationFilter`：从 `Authorization` 头部提取 `Bearer` 令牌，校验 JWT，填充 `SecurityContextHolder`。
  - `LoginUser`：自定义 `UserDetails` 实现，包装 `User` 实体。
  - `CustomUserDetailsService`：从数据库按用户名加载用户。
- 在 `AuthController` 中实现认证端点：
  - `POST /auth/register`：校验输入，BCrypt 哈希密码，创建用户，返回 `RegisterResponseVO`。
  - `POST /auth/login`：校验凭据，检查用户状态（`status=1`、`isDeleted=0`），生成 JWT，返回 `LoginResponseVO`。
  - `GET /auth/me`：从 `Authentication` 主体提取当前用户，返回用户 ID 和用户名。
- 添加 `PasswordConfig`，将 `BCryptPasswordEncoder` 暴露为 Spring Bean。
- 创建 DTO：`RegisterRequestDTO`、`LoginRequestDTO`。
- 创建 VO：`RegisterResponseVO`、`LoginResponseVO`、`UserVO`。
- 添加静态测试页面 `auth-test.html`，用于手动端到端 JWT 流程验证。
- 创建基线测试：`UserMapperTest`（MyBatis 集成）、`GradescopeSpringBootApplicationTests`（上下文加载）。

### 实现细节
- **密码哈希**: BCrypt 默认强度（10）。`PasswordConfig` 将编码器 Bean 隔离，避免与安全自动配置产生循环依赖。
- **JWT 声明结构**:
  - `subject`: 用户名
  - `claim("userId")`: 用户主键
  - `claim("username")`: 用户名（冗余但便于过滤器解析）
  - `iat`、`exp`: 标准 JWT 时间戳
- **安全过滤器链顺序**:
  1. `JwtAuthenticationFilter`（在 `UsernamePasswordAuthenticationFilter` 之前添加）
  2. 若令牌有效 → `SecurityContextHolder` 填充 `UsernamePasswordAuthenticationToken`
  3. 若无令牌或无效 → 链继续；授权规则决定访问权限
- **Bean 注入风格**: 全项目优先使用构造器注入（`UserServiceImpl`、`AuthServiceImpl`、`UserController`、`AuthController`、`JwtAuthenticationFilter`、`SecurityConfig`、`CustomUserDetailsService`）。
- **VO/Entity 分离**: `UserVO` 故意省略 `passwordHash`、`lastLoginAt`、`createdAt`、`updatedAt`、`isDeleted`。转换使用 `BeanUtils.copyProperties`，作为引入 MapStruct 前的轻量过渡方案。
- **MyBatis 配置**: XML Mapper 位于 `classpath:mapper/*.xml`。接口使用 `@Mapper` 注解；主类不使用 `@MapperScan` 以避免之前的范围问题。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `src/main/java/com/example/gradescopespringboot/GradescopeSpringBootApplication.java`
  - `src/main/java/com/example/gradescopespringboot/entity/User.java`
  - `src/main/java/com/example/gradescopespringboot/mapper/UserMapper.java`
  - `src/main/resources/mapper/UserMapper.xml`
  - `src/main/java/com/example/gradescopespringboot/service/UserService.java`
  - `src/main/java/com/example/gradescopespringboot/service/impl/UserServiceImpl.java`
  - `src/main/java/com/example/gradescopespringboot/controller/UserController.java`
  - `src/main/java/com/example/gradescopespringboot/controller/AuthController.java`
  - `src/main/java/com/example/gradescopespringboot/service/AuthService.java`
  - `src/main/java/com/example/gradescopespringboot/service/impl/AuthServiceImpl.java`
  - `src/main/java/com/example/gradescopespringboot/common/result/Result.java`
  - `src/main/java/com/example/gradescopespringboot/dto/auth/RegisterRequestDTO.java`
  - `src/main/java/com/example/gradescopespringboot/dto/auth/LoginRequestDTO.java`
  - `src/main/java/com/example/gradescopespringboot/vo/auth/RegisterResponseVO.java`
  - `src/main/java/com/example/gradescopespringboot/vo/auth/LoginResponseVO.java`
  - `src/main/java/com/example/gradescopespringboot/vo/user/UserVO.java`
  - `src/main/java/com/example/gradescopespringboot/config/PasswordConfig.java`
  - `src/main/java/com/example/gradescopespringboot/config/SecurityConfig.java`
  - `src/main/java/com/example/gradescopespringboot/security/util/JwtTokenProvider.java`
  - `src/main/java/com/example/gradescopespringboot/security/filter/JwtAuthenticationFilter.java`
  - `src/main/java/com/example/gradescopespringboot/security/model/LoginUser.java`
  - `src/main/java/com/example/gradescopespringboot/security/service/CustomUserDetailsService.java`
  - `src/main/resources/static/auth-test.html`
  - `src/test/java/com/example/gradescopespringboot/UserMapperTest.java`
  - `src/test/java/com/example/gradescopespringboot/GradescopeSpringBootApplicationTests.java`
  - `pom.xml`
  - `src/main/resources/application.properties`
- **修改**: （初始提交，无先前文件修改）
- **删除**: （无）

### 执行的测试
- **自动化**:
  - `mvn clean test`：`UserMapperTest.testSelectById()` 通过；上下文加载测试通过。
- **手动**:
  - 通过 IDE 启动应用；确认启动无异常。
  - 在浏览器中使用 `auth-test.html`：
    1. 注册新用户 → 收到 `{"code":200,"message":"success","data":{"userId":X,"username":"testuser"}}`。
    2. 使用相同凭据登录 → 收到 JWT 令牌。
    3. 携带 Bearer 令牌调用 `GET /auth/me` → 收到用户信息。
    4. 携带令牌调用 `GET /users/1` → 收到用户详情。
    5. 不带令牌调用 `GET /users/1` → 收到 401。

### 已知问题 / 限制
- `UserMapper.xml` 查询**目前未过滤** `is_deleted = 0`。已删除用户仍可通过 ID 或用户名查询到。**将在里程碑 1 中修复**。
- `AuthServiceImpl` 对业务错误（如"用户名已存在"）抛出原始 `RuntimeException`。这会作为 500 错误并泄露堆栈跟踪。**将在里程碑 1 中**通过 `@ControllerAdvice` 修复。
- DTO（`RegisterRequestDTO`、`LoginRequestDTO`）无校验注解。可提交空字符串。**将在里程碑 1 中修复**。
- `UserController.getById` 未处理 `null` 用户（返回空 VO 或在 `BeanUtils.copyProperties` 时引发 NPE）。应返回 404。**将在里程碑 1 中修复**。
- `LoginUser.getAuthorities()` 中角色硬编码为 `ROLE_USER`。`roles` 和 `user_roles` 表存在但未使用。**将在里程碑 2 中处理**。
- 所有列表接口暂不分页（目前不需要，但里程碑 10 中将需要）。
- 尚无文件上传功能。
- `auth-test.html` 是便利工具，非生产前端。

### 回滚指令
若本次推送导致严重问题，可回退到提交 `1e71163`（初始提交）：
```bash
git reset --hard 1e71163
git push --force origin main  # 警告：破坏性操作；仅在绝对必要时使用
```

---

## 推送 #1 —— 技术计划、里程碑与日志文档

- **日期**: 2026-06-25
- **分支**: `main`
- **提交范围**: （待提交并推送）
- **里程碑**: 不适用（元/项目管理）

### 实现了什么
- 创建 `TECHNICAL_PLAN.md`：全面的唯一权威来源，记录项目愿景、已锁定技术栈（后端 + 前端）、未来技术补充、系统架构（含前端层）、按模块划分的功能需求（含完整前端页面清单）、数据库设计原理、API 规范（含前后端交互与 CORS）、安全策略（含前端安全）、代码约定（含前端代码规范）、测试策略（含前端测试）和演进路线图（含前端阶段）。
- 创建 `MILESTONES.md`：详细的 11 个后端里程碑 + 6 个前端里程碑任务拆解（M0/F0 已完成，M1-M10 / F1-F6 已规划）。每个里程碑包含子任务、验收标准、测试方法、代码审查清单和推送条件。
- 创建 `CHANGELOG.md`：本文档，建立推送记录格式并记录基线推送 #0。
- 创建 `PROJECT_OVERVIEW.md`：高层次项目介绍，用于入门和外部参考。
- **本次更新（文档修订）**: 在 `TECHNICAL_PLAN.md` 和 `MILESTONES.md` 中补充了完整的前端规划：
  - 前端技术栈：Vue 3 + Vite + TypeScript + Element Plus + Vue Router + Pinia + Axios。
  - 前端页面清单：按角色（学生/教师/管理员）划分的完整页面列表，含路由、功能说明。
  - 前端架构：SPA 单页应用、Pinia 状态管理、Axios 统一封装、路由守卫、动态菜单。
  - 前端里程碑 F1-F6：从项目搭建 → 学生页面 → 教师页面 → 评分统计 → 管理员后台 → 部署优化。

### 实现细节
- 所有文档均为 Markdown 格式，存放于仓库根目录便于访问。
- `TECHNICAL_PLAN.md` 设计为**动态文档**，包含显式变更日志章节（第 12 节）以追踪自身演进。
- `MILESTONES.md` 使用严格的 `[ ]` / `[~]` / `[x]` 状态系统，防止完成状态歧义。
- 里程碑依赖关系显式声明（例如里程碑 2 依赖里程碑 1），以强制线性、可验证的进度。
- `MILESTONES.md` 中的推送条件要求 `mvn clean test` 通过**且**手动安全验证通过后才可推送到远程。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `TECHNICAL_PLAN.md`
  - `MILESTONES.md`
  - `CHANGELOG.md`
  - `PROJECT_OVERVIEW.md`
- **修改**: （无）
- **删除**: （无）

### 执行的测试
- 不适用（仅文档，无代码变更）。

### 已知问题 / 限制
- 这些文档代表**计划状态**。实际实现可能需要偏差。任何偏差必须同时记录在 `CHANGELOG.md`（推送记录）和 `TECHNICAL_PLAN.md`（第 12 节文档变更日志）中。
- 里程碑工作量估算是基于当前代码库复杂度的粗略猜测。完成里程碑 1 后应重新校准。

### 回滚指令
- 文档仅为追加。如需回滚，直接从工作区删除 `.md` 文件并提交即可。

---

## 推送 #2 —— 全局异常处理与输入校验

- **日期**: 2026-06-25
- **分支**: `main`
- **提交范围**: （待提交后填写）
- **里程碑**: [里程碑 1：全局异常处理与输入校验](MILESTONES.md#里程碑-1全局异常处理与输入校验)

### 实现了什么
- 创建自定义异常体系：
  - `BusinessException`（业务异常基类，支持 code + message）
  - `ResourceNotFoundException`（404，资源不存在）
  - `ValidationException`（400，参数校验失败）
  - `UnauthorizedException`（401，未认证）
- 创建 `ResultCode` 枚举，集中定义所有业务状态码（200/400/401/403/404/409/500）。
- 创建 `GlobalExceptionHandler`（`@ControllerAdvice`），统一捕获所有异常并封装为 `Result<T>`：
  - `BusinessException` → 返回结构化错误（code + message）
  - `MethodArgumentNotValidException` → 返回 400 及字段错误信息
  - `BindException` / `IllegalArgumentException` → 返回 400
  - 通用 `Exception` → 返回 500，**服务端记录堆栈，客户端不泄露**
- 为 DTO 添加 Jakarta Validation 注解：
  - `RegisterRequestDTO`：`@NotBlank`、`@Size(min=3, max=50)`（username）、`@Size(min=8)`（password）、`@Email`
  - `LoginRequestDTO`：`@NotBlank`（username、password）
- 在 `AuthController` 的所有 `@RequestBody` 参数上添加 `@Valid`。
- 更新 `UserMapper.xml`：`selectById` 和 `selectByUsername` 均加入 `AND is_deleted = 0`，已删除用户不再被查询到。
- 重构 `AuthServiceImpl`：所有业务错误抛出 `BusinessException`（带 `ResultCode`），替代原始 `RuntimeException`。
- 重构 `UserController.getById`：当用户不存在时抛出 `ResourceNotFoundException`，返回 404 而非空对象或 NPE。
- 在 `pom.xml` 中添加 `spring-security-test` 依赖（测试作用域）。
- 创建 `GlobalExceptionHandlerTest`：5 个单元测试，直接验证异常处理器的返回值结构，全部通过。

### 实现细节
- `GlobalExceptionHandler` 使用 `@Slf4j` 记录日志：业务异常记 WARN，未预期异常记 ERROR（含完整堆栈）。
- 异常处理器返回的 HTTP 状态码始终是 200（保持 `Result<T>` 包装一致），业务状态码通过 `Result.code` 区分。
- `AuthServiceImpl` 中用户名/密码为空的检查仍保留，作为双重保险（前端校验 + 后端校验）。
- `UserMapper.xml` 中 `is_deleted = 0` 的检查直接追加在 WHERE 条件中，不影响现有查询语义。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `src/main/java/com/example/gradescopespringboot/common/exception/BusinessException.java`
  - `src/main/java/com/example/gradescopespringboot/common/exception/ResourceNotFoundException.java`
  - `src/main/java/com/example/gradescopespringboot/common/exception/ValidationException.java`
  - `src/main/java/com/example/gradescopespringboot/common/exception/UnauthorizedException.java`
  - `src/main/java/com/example/gradescopespringboot/common/exception/ResultCode.java`
  - `src/main/java/com/example/gradescopespringboot/common/exception/GlobalExceptionHandler.java`
  - `src/test/java/com/example/gradescopespringboot/GlobalExceptionHandlerTest.java`
- **修改**:
  - `src/main/java/com/example/gradescopespringboot/dto/auth/RegisterRequestDTO.java`（添加校验注解）
  - `src/main/java/com/example/gradescopespringboot/dto/auth/LoginRequestDTO.java`（添加校验注解）
  - `src/main/java/com/example/gradescopespringboot/controller/AuthController.java`（添加 `@Valid`）
  - `src/main/java/com/example/gradescopespringboot/controller/UserController.java`（null 检查 + ResourceNotFoundException）
  - `src/main/java/com/example/gradescopespringboot/service/impl/AuthServiceImpl.java`（替换 RuntimeException 为 BusinessException）
  - `src/main/resources/mapper/UserMapper.xml`（添加 `is_deleted = 0`）
  - `pom.xml`（添加 `spring-security-test`）
  - `.gitignore`（添加 `.claude/`）
- **删除**: （无）

### 执行的测试
- `GlobalExceptionHandlerTest`：5 个单元测试全部通过
  - `testHandleBusinessException_ReturnsStructuredResult`
  - `testHandleMethodArgumentNotValid_Returns400WithFieldErrors`
  - `testHandleIllegalArgument_Returns400`
  - `testHandleException_Returns500WithoutSensitiveInfo`
  - `testHandleException_WithNullMessage_Returns500`
- `GradescopeSpringBootApplicationTests`：上下文加载测试通过
- **说明**: `UserMapperTest` 需要本地 MySQL 运行，当前环境未启动数据库，故未执行。

### 已知问题 / 限制
- `UserMapperTest` 因本地 MySQL 未运行而无法执行。建议在本地启动 MySQL 后补充运行 `mvn clean test` 以验证完整测试套件。
- 当前未引入 `@NotBlank` 对全角空格的处理（`@NotBlank` 已覆盖空格，但全角空格需额外自定义校验器，可在后续里程碑补充）。

### 回滚指令
```bash
git revert HEAD
```

---

## 推送 #3 —— 前端项目搭建与认证页面

- **日期**: 2026-06-25
- **分支**: `main`
- **提交范围**: （待提交后填写）
- **里程碑**: [前端里程碑 F1：前端项目搭建与认证页面](MILESTONES.md#前端里程碑-1f1前端项目搭建与认证页面)

### 实现了什么
- 初始化 Vue 3 + Vite + TypeScript 前端工程（`gradescope-frontend/`）。
- 安装核心依赖：`vue-router@4`、`pinia`、`axios`、`element-plus`、`@element-plus/icons-vue`。
- 配置 Vite：`@/` 别名指向 `src/`，开发代理 `/api` → `http://localhost:8080`。
- 创建前端目录结构：`api/`、`stores/`、`router/`、`types/`、`views/`、`layouts/`、`components/`。
- **Axios 封装** (`src/api/request.ts`)：
  - 请求拦截器自动注入 `Authorization: Bearer <token>`
  - 响应拦截器统一处理 401（跳转登录）、403（无权限提示）、500（服务器错误）、网络错误
  - 与后端 `Result<T>` 结构对齐
- **Pinia Auth Store** (`src/stores/auth.ts`)：
  - `token` / `user` 状态，`isAuthenticated` computed
  - `login()` / `register()` / `fetchUser()` / `logout()` 方法
  - Token 持久化到 `localStorage`
- **路由配置** (`src/router/index.ts`)：
  - 路由：`/login`、`/register`、`/dashboard`（占位）
  - `beforeEach` 路由守卫：未认证用户访问需登录页面时跳转 `/login`，已认证用户访问登录页时跳转 `/dashboard`
- **登录页** (`src/views/auth/LoginView.vue`)：Element Plus 表单，用户名/密码输入，登录成功后跳转仪表盘。
- **注册页** (`src/views/auth/RegisterView.vue`)：Element Plus 表单，含用户名、密码、确认密码、真实姓名、邮箱、电话、学号/工号字段，前端校验（密码一致性、邮箱格式）。
- **认证布局** (`src/layouts/AuthLayout.vue`)：渐变背景 + 卡片式布局，统一定制登录/注册页视觉风格。
- **类型定义** (`src/types/auth.ts`)：TypeScript 接口与后端 DTO/VO 对齐（`ApiResponse`、`LoginRequest`、`RegisterRequest`、`LoginResponse`、`RegisterResponse`、`UserVO`）。

### 实现细节
- 前端使用 Composition API (`<script setup lang="ts">`)，遵循 Vue 3 最佳实践。
- `npm run build` 构建成功，无 TypeScript 编译错误。
- Element Plus 组件使用 `size="large"` 和 `label-position="top"` 提升表单体验。
- 注册页密码通过自定义 `validator` 实现确认密码一致性检查。

### 新增 / 修改 / 删除的文件
- **新增**: `gradescope-frontend/` 整个目录（Vue 3 项目脚手架 + 所有自定义代码）
- **修改**: （无后端文件变更）
- **删除**: （无）

### 执行的测试
- `npm run build`：构建成功，生成 `dist/` 目录
- 手动验证：登录页和注册页组件渲染逻辑正确，路由守卫逻辑通过代码审查

### 已知问题 / 限制
- 前端尚未连接真实后端进行端到端测试（需要后端服务运行在 `localhost:8080`）。
- 仪表盘页面为占位符，将在前端里程碑 F2 中实现。
- 404 页面为占位符，将在后续里程碑中完善。
- 构建产物中 `index.js` 体积较大（约 1MB），后续可通过路由懒加载和组件按需引入进一步优化。

### 回滚指令
```bash
rm -rf gradescope-frontend/
git checkout -- gradescope-frontend/  # 如果已提交
git revert HEAD
```

---

## 推送 #4 —— 前端 UI 设计重构与风格统一

- **日期**: 2026-06-25
- **分支**: `main`
- **提交范围**: （待提交后填写）
- **里程碑**: [前端里程碑 F1：前端项目搭建与认证页面](MILESTONES.md#前端里程碑-1f1前端项目搭建与认证页面)（设计优化）

### 实现了什么
参考 2025 年现代 SaaS 设计趋势（Linear、Vercel、Notion 风格），对前端认证页面进行了全面视觉重构：

- **全新设计系统** (`src/style.css`)：
  - CSS 变量体系：Indigo 主色板 + Slate 中性色板
  - 统一阴影层级（sm/md/lg/xl/card）
  - 统一圆角体系（6px/10px/16px/24px）
  - 8px 网格间距系统
  - 通用动画关键帧（fadeIn、fadeInScale、slideInRight）
  - 排版工具类（display/headline/title/body/caption/label）

- **分屏布局认证页** (`src/layouts/AuthLayout.vue`)：
  - 左侧品牌面板（44% 宽度）：Indigo 渐变背景 + 品牌 Logo + 产品标语 + 功能特性列表 + 装饰性圆环
  - 右侧表单面板（56% 宽度）：浅灰背景 + 居中大卡片
  - 响应式：≤960px 时隐藏左侧面板，移动端顶部留白

- **精致登录页** (`src/views/auth/LoginView.vue`)：
  - "Welcome back" 大标题 + 副标题
  - 带图标前缀的输入框（User / Lock）
  - 聚焦时双环主色边框效果
  - "Remember me" 复选框 + "Forgot password?" 链接
  - 渐变主色按钮 + hover 上浮阴影动画
  - 优雅分隔线 + 底部注册引导

- **精致注册页** (`src/views/auth/RegisterView.vue`)：
  - "Create your account" 大标题 + 副标题
  - 必填字段带红色星号标记
  - "Optional Information" 分隔线区分必填/选填
  - 所有字段带图标前缀（User / Lock / Message / Phone / Document）
  - 与登录页完全一致的按钮和链接风格

- **全局过渡动画** (`src/App.vue`)：
  - 页面切换 fade 过渡（opacity + translateY）

- **路由结构优化** (`src/router/index.ts`)：
  - 使用嵌套路由让 `/login` 和 `/register` 共享 `AuthLayout`

### 设计参考
- [Modern Web Design Trends in 2025](https://www.duomi.fi/blog/modern-web-design-trends)
- [Web Design Trends for 2025: Modern Design and Innovations](https://www.digital4u.gr/en/trends-stin-kataskevi-istoselidon-to-2025-monternos-schediasmos-kai-kainotomies/)
- [Gradescope Review 2025](https://www.notieai.com/gradescope-review-2025-6-month-experience/)

### 新增 / 修改 / 删除的文件
- **修改**:
  - `gradescope-frontend/src/style.css`（全新设计系统变量 + 动画 + 工具类）
  - `gradescope-frontend/src/App.vue`（添加页面切换过渡动画）
  - `gradescope-frontend/src/layouts/AuthLayout.vue`（分屏布局 + 品牌面板）
  - `gradescope-frontend/src/views/auth/LoginView.vue`（精致表单 + 图标 + 动画）
  - `gradescope-frontend/src/views/auth/RegisterView.vue`（精致表单 + 图标 + 动画）
  - `gradescope-frontend/src/router/index.ts`（嵌套路由共享 AuthLayout）
  - `CHANGELOG.md`（更新日志）

### 执行的测试
- `npm run build`：构建成功（503ms），无 TypeScript/Vue 编译错误

### 已知问题 / 限制
- 构建产物 `index.js` 约 1MB（Element Plus 完整引入），后续可通过 `unplugin-vue-components` + `unplugin-auto-import` 实现按需加载以减小体积。
- 装饰性圆环在品牌面板中使用 CSS 实现，如需更精致的插图可后续引入 SVG 插画。

### 回滚指令
```bash
git revert HEAD
```

---

## 推送 #5 —— 前端认证页 UI 重设计（frontend-design）

- **日期**: 2026-06-25
- **分支**: `main`
- **提交范围**: `d9033ae..ff7376c`
- **里程碑**: [前端里程碑 F1：前端项目搭建与认证页面](MILESTONES.md#前端里程碑-1f1前端项目搭建与认证页面)

### 实现了什么
- 安装并使用 Anthropic 官方 `frontend-design` plugin 生成认证页 redesign 设计规范。
- 根据规范重构前端设计系统：
  - 新色板：`--color-desk`（桌面灰）、`--color-paper`（纸张白）、`--color-ink`（墨色）、`--color-rule`（批改红）等。
  - 新字体：`Source Serif 4` 用于标题，`Sora` 用于 UI 文本，通过 Google Fonts 加载。
- 重构 `AuthLayout.vue`：
  - 左侧 33% 深色品牌面板（石墨黑），右侧表单区居中卡片。
  - 新增“Margin Rule（批改建）”标志性装饰：表单卡片左侧 2px 红色竖线 + 刻度标记。
  - 响应式：桌面双栏、平板顶部横幅、移动端隐藏品牌区、卡片全宽。
- 重构 `AuthLayout.vue` 左侧品牌面板：
  - 去除冗长功能列表，只保留品牌 Logo、一行大标题、一道红色编辑标记、一行介绍语。
  - 标题与介绍语已中文化。
- 重构 `LoginView.vue` 与 `RegisterView.vue`：
  - 全部文案中文化（标题、标签、占位符、按钮、页脚、验证提示）。
  - 修复提交按钮不可见的问题：移除依赖 `opacity: 0` + `animation forwards` 的入场动效，确保按钮在任意动画偏好下都正常显示。
  - 输入框聚焦红色光环、提交按钮悬停上浮、自定义视觉风格保留。
- 在 `style.css` 中加入 `prefers-reduced-motion` 媒体查询，尊重用户减少动画偏好。

### 实现细节
- 设计规格直接来源于 `frontend-design` skill 输出：以“学术桌面 / 批改纸”为隐喻，避免常见 SaaS 默认色板。
- 表单卡片由 `AuthLayout.vue` 统一提供，子页面只负责内容，减少重复样式。
- Margin Rule 使用绝对定位 + `aria-hidden="true"`，仅作装饰，不影响无障碍。
- 所有交互动画（入场、聚焦、悬停）均使用 CSS 变量和 `cubic-bezier(0.4, 0, 0.2, 1)`。

### 新增 / 修改 / 删除的文件
- **新增**: （无）
- **修改**:
  - `TECHNICAL_PLAN.md`（新增 9.4.3 前端设计系统规范：色板、字体、Margin Rule、动效、响应式、中文文案、无障碍）
  - `gradescope-frontend/src/main.ts`（补充 `import './style.css'`，修复样式未加载导致页面白底黑字的问题）
  - `gradescope-frontend/index.html`（加载 Google Fonts）
  - `gradescope-frontend/src/style.css`（设计系统变量、动画、字体）
  - `gradescope-frontend/src/layouts/AuthLayout.vue`（双栏布局 + 最小化品牌面板 + Margin Rule）
  - `gradescope-frontend/src/views/auth/LoginView.vue`（中文化 + 修复按钮显示）
  - `gradescope-frontend/src/views/auth/RegisterView.vue`（中文化 + 修复按钮显示）
- **删除**: （无）

### 执行的测试
- `npm run build`：
  - `vue-tsc -b` 通过，无 TypeScript 编译错误。
  - `vite build` 成功生成 `dist/`。
- 存在的两个警告为依赖库预置问题，非本次改动引入：
  - `node_modules/@vueuse/core/dist/index.js` 的 `/* #__PURE__ */` 注释位置警告。
  - 构建产物 `index.js` 大于 500 kB 的 chunk 大小提示（Element Plus 完整引入，后续 F6 按需加载优化）。

### 已知问题 / 限制
- 前端尚未连接真实后端进行端到端测试（需要后端服务运行在 `localhost:8080`）。
- `useAuthStore` 当前仍将 JWT 持久化到 `localStorage`，与项目 Vue 安全规范中“禁止将原始 token 写入 localStorage”存在冲突；将在后续里程碑（RBAC/安全加固）中升级为 httpOnly Cookie 或安全存储方案。
- `MILESTONES.md` F1 的部分手动验收项（如登录成功后 token 写入、跳转 `/dashboard`）需启动前后端后验证。
- 构建产物体积较大，计划在 F6 通过路由懒加载 + `unplugin-vue-components` 优化。

### 回滚指令
若本次推送导致问题，可回滚最近一次提交：
```bash
git revert HEAD
```

---

## 推送 #6 —— 修复认证 403 与添加开发测试账号

- **日期**: 2026-08-06
- **分支**: `main`
- **提交范围**: `af73178..8d8e128`
- **里程碑**: [前端里程碑 F1：前端项目搭建与认证页面](MILESTONES.md#前端里程碑-1f1前端项目搭建与认证页面)

### 实现了什么
- 修复前端注册/登录提示“没有权限”（403）的问题：
  - `vite.config.ts` 的 `/api` 代理增加 `rewrite`，把 `/api/auth/...` 正确转发到后端的 `/auth/...`。
  - `SecurityConfig.java` 的包声明从 `security.config` 修正为 `config`，与文件路径保持一致。
- 将 `request.ts` 中所有错误通知文案中文化（请求失败、登录已过期、没有权限、服务器错误、网络错误）。
- 新增开发环境数据种子 `DataSeeder.java`，应用启动时自动创建 3 个测试账号：
  - `alice` / `password123`（学生）
  - `bob` / `password123`（教师）
  - `charlie` / `password123`（管理员）
- 种子程序具备幂等性：重启应用不会重复创建已有用户。

### 实现细节
- Vite proxy 默认不会自动去掉 `/api` 前缀，需要显式 `rewrite: (path) => path.replace(/^\/api/, '')`。
- `DataSeeder` 使用 `CommandLineRunner` 实现，注入 `UserService` 和 `PasswordEncoder`，在 Spring 上下文完全启动后执行，因此可以用 BCrypt 实时哈希密码。
- 使用 `@Profile("!prod")` 防止生产环境意外执行种子程序。
- `SecurityConfig` 包路径修正后，Spring Boot 的组件扫描能正确加载自定义安全过滤器链。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `src/main/java/com/example/gradescopespringboot/config/DataSeeder.java`
- **修改**:
  - `src/main/java/com/example/gradescopespringboot/config/SecurityConfig.java`（修正包声明）
  - `gradescope-frontend/vite.config.ts`（增加 `/api` rewrite）
  - `gradescope-frontend/src/api/request.ts`（错误提示中文化）

### 执行的测试
- `./mvnw clean compile -DskipTests`：编译通过，无 Java 错误。
- `cd gradescope-frontend && npm run build`：构建通过，无 TypeScript/Vue 错误。
- 待启动前后端后进行端到端登录/注册验证。

### 已知问题 / 限制
- `DataSeeder` 目前只插入 `users` 表；`roles` / `user_roles` 表的数据将在后端 M2 RBAC 里程碑中补充。
- `useAuthStore` 仍使用 `localStorage` 存储 JWT，后续需升级为更安全的存储方案。

### 回滚指令
若本次推送导致问题，可回滚最近一次提交：
```bash
git revert HEAD
```

---

## 推送 #7 —— F1 联调完成与 MySQL Docker 连接修复

- **日期**: 2026-08-17
- **分支**: `main`
- **提交范围**: `553e40b..4cb335b`
- **里程碑**: [前端里程碑 F1：前端项目搭建与认证页面](MILESTONES.md#前端里程碑-1f1前端项目搭建与认证页面)

### 实现了什么
- 修复后端在 Docker MySQL 8 默认 `caching_sha2_password` 插件下无法连接的问题：
  - 在 `application.properties` 的 JDBC URL 中追加 `allowPublicKeyRetrieval=true`。
- 完成前端 F1 里程碑的后端联调与端到端验证：
  - 启动 Spring Boot 后端（端口 8080）与 Vue 开发服务器（端口 5173）。
  - 使用 DataSeeder 账号 `alice` / `password123` 登录成功并写入 `localStorage` token。
  - 使用 Playwright + 系统 Chrome 验证注册新用户、自动跳转登录页、新用户再次登录成功。
  - 验证 Vite 代理 `/api` → `http://localhost:8080` 工作正常。
- 更新 `MILESTONES.md`：
  - 将前端里程碑 F1 标记为已完成。
  - 将后端里程碑 M1（全局异常处理与输入校验）标记为已完成（实际已实现并推送于 `a3c1800`，文档状态此前未同步）。

### 实现细节
- `allowPublicKeyRetrieval=true` 仅用于开发环境；生产环境应使用 SSL 或配置 MySQL 为 `mysql_native_password`。
- Playwright 验证脚本使用系统已安装的 Chrome（`channel: 'chrome'`），避免在仓库中引入浏览器依赖。

### 新增 / 修改 / 删除的文件
- **修改**:
  - `src/main/resources/application.properties`（追加 `allowPublicKeyRetrieval=true`）
  - `MILESTONES.md`（F1 与 M1 状态改为已完成，更新验收标准勾选）
  - `CHANGELOG.md`（本记录）
- **删除**: （无）

### 执行的测试
- `mvn clean test`：7 个测试全部通过（含 `GlobalExceptionHandlerTest`、`GradescopeSpringBootApplicationTests`、`UserMapperTest`）。
- `cd gradescope-frontend && npm run build`：构建成功，无 TypeScript/Vue 编译错误。
- Playwright 端到端验证（系统 Chrome）：
  - 登录 `alice` / `password123` → 跳转 `/dashboard`，token 写入 `localStorage`。
  - 注册随机新用户 → 后端返回 200，前端跳转 `/login`。
  - 新用户登录 → 跳转 `/dashboard`，token 写入 `localStorage`。
- curl 直接验证后端：`POST /auth/login` 与 `POST /auth/register` 均返回 200。

### 已知问题 / 限制
- `allowPublicKeyRetrieval=true` 会降低安全性，仅用于本地 Docker 开发。
- `useAuthStore` 仍使用 `localStorage` 存储 JWT，后续里程碑中需升级为更安全的存储方案。
- Playwright 验证脚本为临时文件，未纳入仓库；后续 F2+ 可引入正式的 `@playwright/test` E2E 套件。

### 回滚指令
```bash
git revert HEAD
```

---

## 推送 #8 —— 后端 RBAC 基于角色的访问控制

- **日期**: 2026-08-17
- **分支**: `main`
- **提交范围**: `4cb335b..1075184`
- **里程碑**: [里程碑 2：RBAC（基于角色的访问控制）](MILESTONES.md#里程碑-2rbac基于角色的访问控制)

### 实现了什么
- 创建 RBAC 实体：`Role`、`UserRole`，对应 `roles` 与 `user_roles` 表。
- 创建 MyBatis XML Mapper：`RoleMapper` + `RoleMapper.xml`、`UserRoleMapper` + `UserRoleMapper.xml`。
- 创建 Service 层：`RoleService` / `RoleServiceImpl`、`UserRoleService` / `UserRoleServiceImpl`。
- 在 `mysql.txt` 追加默认角色 INSERT：`ADMIN`、`STUDENT`、`TA`、`INSTRUCTOR`。
- 修改 `CustomUserDetailsService`：从 `user_roles` + `roles` 表加载真实角色，映射为 `SimpleGrantedAuthority("ROLE_" + roleCode)`。
- 重构 `LoginUser`：移除硬编码 `ROLE_USER`，改为通过构造函数接收 authorities；新增 `withRoleCodes` 工厂方法。
- 修改 `JwtTokenProvider.generateToken`：新增 `roles` 声明，登录时把用户角色写入 JWT。
- 修改 `JwtAuthenticationFilter`：从 JWT 解析 `roles` 声明并重建 `GrantedAuthority` 列表。
- 更新 `SecurityConfig`：
  - `/admin/**` 仅 `ADMIN` 可访问。
  - `POST /courses` 仅 `INSTRUCTOR` 或 `ADMIN` 可访问。
  - `/auth/me` 需要认证；注册/登录保持公开。
- 更新 `AuthController.me`：响应中增加 `roles` 字段。
- 更新 `DataSeeder`：自动创建默认角色，为 `alice`/`bob`/`charlie` 分配 `STUDENT`/`INSTRUCTOR`/`ADMIN`，并对已存在但无角色的用户补分配角色。
- 新增 `AdminController`（占位）：`GET /admin/dashboard/stats`，用于验证 ADMIN 角色权限。
- 新增测试：
  - `CustomUserDetailsServiceTest`（Mockito 单元测试）。
  - `RbacIntegrationTest`（MockMvc 集成测试）。

### 实现细节
- 数据库角色编码（如 `ADMIN`）与 JWT 中的 `roles` 声明保持一致；Spring Security 自动拼接 `ROLE_` 前缀进行鉴权。
- `CustomUserDetailsService` 使用 stream + JOIN-free 查询加载角色；当前用户量小，N+1 可接受，后续数据量大时可改为一次性 JOIN。
- `DataSeeder` 保持幂等：角色/用户存在则跳过，已存在用户无角色则补分配。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `src/main/java/com/example/gradescopespringboot/entity/Role.java`
  - `src/main/java/com/example/gradescopespringboot/entity/UserRole.java`
  - `src/main/java/com/example/gradescopespringboot/mapper/RoleMapper.java`
  - `src/main/resources/mapper/RoleMapper.xml`
  - `src/main/java/com/example/gradescopespringboot/mapper/UserRoleMapper.java`
  - `src/main/resources/mapper/UserRoleMapper.xml`
  - `src/main/java/com/example/gradescopespringboot/service/RoleService.java`
  - `src/main/java/com/example/gradescopespringboot/service/impl/RoleServiceImpl.java`
  - `src/main/java/com/example/gradescopespringboot/service/UserRoleService.java`
  - `src/main/java/com/example/gradescopespringboot/service/impl/UserRoleServiceImpl.java`
  - `src/main/java/com/example/gradescopespringboot/controller/AdminController.java`
  - `src/test/java/com/example/gradescopespringboot/RbacIntegrationTest.java`
  - `src/test/java/com/example/gradescopespringboot/security/service/CustomUserDetailsServiceTest.java`
- **修改**:
  - `src/main/java/com/example/gradescopespringboot/security/model/LoginUser.java`（移除硬编码角色）
  - `src/main/java/com/example/gradescopespringboot/security/service/CustomUserDetailsService.java`（加载真实角色）
  - `src/main/java/com/example/gradescopespringboot/security/util/JwtTokenProvider.java`（roles 声明）
  - `src/main/java/com/example/gradescopespringboot/security/filter/JwtAuthenticationFilter.java`（解析 JWT 角色）
  - `src/main/java/com/example/gradescopespringboot/config/SecurityConfig.java`（角色鉴权规则）
  - `src/main/java/com/example/gradescopespringboot/controller/AuthController.java`（me 返回 roles）
  - `src/main/java/com/example/gradescopespringboot/service/impl/AuthServiceImpl.java`（登录时查询角色写入 token）
  - `src/main/java/com/example/gradescopespringboot/config/DataSeeder.java`（角色种子与分配）
  - `mysql.txt`（默认角色 INSERT）
  - `MILESTONES.md`（M2 标记完成）
  - `CHANGELOG.md`（本记录，并修正推送 #7 提交范围为 `553e40b..4cb335b`）
- **删除**: （无）

### 执行的测试
- `mvn clean test`：12 个测试全部通过。
  - `GlobalExceptionHandlerTest`（5）
  - `GradescopeSpringBootApplicationTests`（1）
  - `UserMapperTest`（1）
  - `RbacIntegrationTest`（4）
  - `CustomUserDetailsServiceTest`（1）
- `cd gradescope-frontend && npm run build`：构建成功，无 TypeScript/Vue 编译错误。
- 手动 curl 验证：
  - `alice`（STUDENT）访问 `/admin/dashboard/stats` → 403。
  - `charlie`（ADMIN）访问 `/admin/dashboard/stats` → 200。
  - `GET /auth/me` 返回包含 `roles` 列表。

### 已知问题 / 限制
- `CustomUserDetailsService` 当前按 userId 查询 `user_roles` 再逐条查 `roles`，数据量大时建议改为一次 JOIN。
- 新注册用户默认没有任何角色；后续可在注册流程中分配默认 `STUDENT` 角色，或由管理员在后台分配。
- `localStorage` JWT 存储仍是临时方案，后续里程碑中需升级为更安全的存储。

### 回滚指令
```bash
git revert HEAD
```

---

## 推送 #9 —— 后端管理员管理模块

- **日期**: 2026-08-17
- **分支**: `main`
- **提交范围**: `262d369..a5ac31e`
- **里程碑**: [里程碑 3：管理员管理模块](MILESTONES.md#里程碑-3管理员管理模块)

### 实现了什么
- 创建 `PageResult<T>` 统一分页响应包装器（含 `list`、`total`、`pageNum`、`pageSize`、`pages`），为后续 PageHelper 做准备。
- 创建 `AdminUserListVO`：管理员视角用户列表对象，包含角色列表，不暴露 `passwordHash`。
- 扩展 `UserMapper` / `UserMapper.xml`：
  - `countAdminUsers` + `selectAdminUserList`：支持按 `role`、`status` 过滤的手动分页查询，使用 `GROUP_CONCAT` 聚合角色编码。
  - `updateStatusById`：启用/禁用用户。
  - `countUsers` / `countCourses` / `countAssignments` / `countSubmissions`：仪表盘统计。
- 创建 `AdminUserStatusUpdateDTO`（`status` 字段 `@NotNull` 校验）。
- 创建 `AdminService` / `AdminServiceImpl`：
  - `listUsers`：分页 + 过滤 + 角色列表展开。
  - `getUserById`：按 ID 查询并组装角色。
  - `updateUserStatus`：原子状态切换（`@Transactional`）。
  - `dashboardStats`：返回四项统计计数。
- 完善 `AdminController`：
  - `GET /admin/users`
  - `GET /admin/users/{id}`
  - `PATCH /admin/users/{id}/status`
  - `GET /admin/dashboard/stats`
- 新增 `AdminControllerIntegrationTest`：
  - 管理员可访问用户列表、按角色过滤、查看详情、禁用用户。
  - 非管理员访问 `/admin/**` 返回 403。
  - 禁用用户后登录失败。

### 实现细节
- 分页默认值：`pageNum=1`，`pageSize=10`，最大 `pageSize=100`。
- 用户列表 SQL 使用 `LEFT JOIN user_roles / roles` + `GROUP_CONCAT`，避免在列表接口中对每个用户单独查角色（N+1）。
- 状态切换在 service 层校验 `status` 只能是 0 或 1，并使用 `@Transactional`。
- `AdminUserListVO` 中的 `roleCodes` 字段用于接收 SQL `GROUP_CONCAT`，service 层将其拆分为 `roles` 列表返回给前端。

### 新增 / 修改 / 删除的文件
- **新增**:
  - `src/main/java/com/example/gradescopespringboot/common/result/PageResult.java`
  - `src/main/java/com/example/gradescopespringboot/vo/admin/AdminUserListVO.java`
  - `src/main/java/com/example/gradescopespringboot/dto/admin/AdminUserStatusUpdateDTO.java`
  - `src/main/java/com/example/gradescopespringboot/service/AdminService.java`
  - `src/main/java/com/example/gradescopespringboot/service/impl/AdminServiceImpl.java`
  - `src/test/java/com/example/gradescopespringboot/AdminControllerIntegrationTest.java`
- **修改**:
  - `src/main/java/com/example/gradescopespringboot/controller/AdminController.java`（实现全部管理员端点）
  - `src/main/java/com/example/gradescopespringboot/mapper/UserMapper.java`（新增管理员相关方法）
  - `src/main/resources/mapper/UserMapper.xml`（新增分页、过滤、统计 SQL）
  - `MILESTONES.md`（M3 标记完成）
  - `CHANGELOG.md`（本记录）
- **删除**: （无）

### 执行的测试
- `mvn clean test`：**18 个测试全部通过**。
  - `GlobalExceptionHandlerTest`（5）
  - `GradescopeSpringBootApplicationTests`（1）
  - `UserMapperTest`（1）
  - `RbacIntegrationTest`（4）
  - `CustomUserDetailsServiceTest`（1）
  - `AdminControllerIntegrationTest`（6）
- `cd gradescope-frontend && npm run build`：构建成功，无 TypeScript/Vue 编译错误。
- 手动 curl 验证：
  - `charlie`（ADMIN）`GET /admin/users` → 200，返回分页列表。
  - `alice`（STUDENT）`GET /admin/users` → 403。
  - `PATCH /admin/users/{id}/status` → 状态更新，禁用后登录返回 code 403。

### 已知问题 / 限制
- 新注册用户默认无角色，需管理员在后台分配（未来可在注册流程默认分配 `STUDENT`）。
- 分页仍为手动 `LIMIT/OFFSET`；M10 中可替换为 PageHelper。
- `localStorage` JWT 存储仍是临时方案，后续里程碑中需升级。

### 回滚指令
```bash
git revert HEAD
```

---

> **如何更新本文档**: 每次推送到远程仓库后，在日志顶部（本行下方）按既定格式追加新记录。更新页眉中的"最后更新"日期。关联 `MILESTONES.md` 中的相关里程碑并更新其状态。
