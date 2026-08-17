# 项目里程碑与任务拆解

> **状态**: 动态文档 | **最后更新**: 2026-06-25  
> **用途**: 本文档将整个项目拆分为可验证的里程碑。每个里程碑包含子任务、明确的验收标准、测试说明和代码审查清单。**在完成当前里程碑的所有验收标准、测试、审查并推送到远程仓库之前，禁止进入下一个里程碑。**

---

## 使用说明

1. 选择**下一个未完成的里程碑**。
2. 按顺序完成**所有子任务**。
3. 运行里程碑中定义的**全部测试**。
4. 使用清单进行**自我代码审查**。
5. 若所有标准通过，**提交并推送**到 GitHub。
6. 在 `CHANGELOG.md` 中更新推送日志。
7. 标记该里程碑为已完成，然后继续下一个。

---

## 图例

- `[ ]` — 未开始
- `[~]` — 进行中
- `[x]` — 已完成

---

## 里程碑 0：项目基础与 JWT 认证

**状态**: `[x]` **已完成**  
**分支**: `main`  
**完成日期**: 2026-03-27（本计划之前）

### 子任务
- [x] Spring Boot 3.3.5 项目脚手架与 Maven 构建。
- [x] MySQL 8 连接与 `application.properties` 配置。
- [x] MyBatis XML Mapper 配置（`map-underscore-to-camel-case=true`）。
- [x] 数据库 schema 创建（11 张表）。
- [x] 统一响应结构 `Result<T>`。
- [x] 用户实体、Mapper、Service、Controller，包含 `GET /users/{id}`。
- [x] BCrypt 密码编码器 Bean。
- [x] JWT 令牌提供者（生成、解析、校验）。
- [x] JWT 认证过滤器。
- [x] Spring Security 过滤器链配置（无状态、关闭 CSRF、公开认证端点）。
- [x] `CustomUserDetailsService` + `LoginUser` 实现 `UserDetails`。
- [x] 认证端点：`POST /auth/register`、`POST /auth/login`、`GET /auth/me`。
- [x] 静态测试页面 `auth-test.html` 用于手动 JWT 流程验证。
- [x] 基线测试：`UserMapperTest` 和 `UserServiceTest`。

### 验收标准
- [x] `mvn clean test` 无错误通过。
- [x] `GET /users/1` 返回包装在 `Result<UserVO>` 中的有效 JSON。
- [x] `POST /auth/register` 创建用户并使用 BCrypt 加密密码。
- [x] `POST /auth/login` 返回 JWT 令牌。
- [x] `GET /auth/me` 携带有效 Bearer 令牌返回用户信息。
- [x] 不带令牌访问 `/users/1` 返回 401。
- [x] `auth-test.html` 在浏览器中端到端正常工作。

### 代码审查清单
- [x] 生产代码中无 `System.out.println`。
- [x] 全项目使用构造器注入。
- [x] `UserVO` 不暴露 `passwordHash`。
- [x] MyBatis XML 对所有参数使用 `#{}`。
- [x] `UserMapper.xml` 查询中检查 `is_deleted = 0`。（*注：当前缺失，将在里程碑 1 中修复*）

### 推送条件
- [x] 所有测试通过。
- [x] 应用启动无异常。
- [x] 认证流程手动浏览器测试成功。

---

## 里程碑 1：全局异常处理与输入校验

**状态**: `[x]` **已完成**  
**依赖**: 里程碑 0  
**预估工作量**: 小（1 个会话）  
**完成日期**: 2026-06-25

### 目标
消除原始异常堆栈直接返回给客户端的情况。提供有意义的结构化错误响应。校验所有传入 DTO。

### 子任务
1. [x] **创建自定义异常体系**:
   - `BusinessException`（运行时异常，带 `code` 和 `message`）。
   - `ResourceNotFoundException extends BusinessException`。
   - `ValidationException extends BusinessException`。
   - `UnauthorizedException extends BusinessException`。
2. [x] **创建 `@ControllerAdvice` 类**（`GlobalExceptionHandler`）:
   - 处理 `BusinessException` → 返回 `Result.fail(code, message)`。
   - 处理 `MethodArgumentNotValidException` → 返回 `Result.fail(400, 字段错误)`。
   - 处理通用 `Exception` → 返回 `Result.fail(500, "服务器内部错误")`（服务端记录完整堆栈）。
3. [x] **重构 `AuthServiceImpl`**，将原始 `RuntimeException` 改为抛出 `BusinessException`。
4. [x] **为 DTO 添加 Jakarta Validation 注解**:
   - `RegisterRequestDTO`：`username` 和 `password` 加 `@NotBlank`，`username` 加 `@Size(min=3, max=50)`，`password` 加 `@Size(min=8)`。
   - `LoginRequestDTO`：两个字段都加 `@NotBlank`。
5. [x] **在所有控制器的 `@RequestBody` 参数上添加 `@Valid` 注解**。
6. [x] **更新 `UserMapper.xml`**，在 `selectById` 和 `selectByUsername` 中加入 `AND is_deleted = 0`。
7. [x] **创建 `ResultCode` 枚举**（推荐但可选）以标准化 `code` 值（例如 `SUCCESS(200)`、`PARAM_ERROR(400)`、`UNAUTHORIZED(401)`、`FORBIDDEN(403)`、`NOT_FOUND(404)`、`INTERNAL_ERROR(500)`）。

### 验收标准
- [x] 以空用户名调用 `POST /auth/register` 返回 `{"code":400,"message":"username: must not be blank"}`。
- [x] 以错误密码调用 `POST /auth/login` 返回结构化的 `Result`，code 为 400，**不是**堆栈跟踪。
- [x] 查询不存在（或已删除）的用户 `GET /users/999` 返回 code 404。
- [x] 所有现有测试仍然通过。
- [x] `UserMapper.selectById` 对已逻辑删除的用户返回 `null`。

### 测试方法
1. 使用 `MockMvc` 单元测试 `GlobalExceptionHandler`:
   - 发送无效 DTO → 预期 400 及字段错误。
   - 触发业务层 `BusinessException` → 预期正确 code。
2. 集成测试:
   - 以空 body 注册 → 断言响应 code 和 message。
   - 查询已删除用户 → 断言 404。

### 代码审查清单
- [x] 业务层不再抛出原始 `RuntimeException`。
- [x] 所有适用 DTO 都带有校验注解。
- [x] 所有接受 DTO 的控制器端点都带有 `@Valid`。
- [x] `GlobalExceptionHandler` 捕获了**所有**预期异常类型。
- [x] HTTP 响应中不泄露敏感信息（如堆栈跟踪）。
- [x] `UserMapper.xml` 所有查询中都存在 `is_deleted = 0`。

### 推送条件
- [x] 所有子任务完成。
- [x] `mvn clean test` 通过。
- [x] 错误场景的手动 Postman/curl 测试通过。
- [x] `auth-test.html` 在注册/登录的成功路径下仍然正常工作。

---

## 里程碑 2：RBAC（基于角色的访问控制）

**状态**: `[x]` **已完成**  
**依赖**: 里程碑 1  
**预估工作量**: 中（2-3 个会话）  
**完成日期**: 2026-08-17

### 目标
替换 `LoginUser` 中硬编码的 `ROLE_USER`，使用数据库中的真实角色。实现全局和课程域的授权。

### 子任务
1. [x] **创建实体类**: `Role`、`UserRole`。
2. [x] **创建 Mapper**: `RoleMapper.java` + `RoleMapper.xml`、`UserRoleMapper.java` + `UserRoleMapper.xml`。
3. [x] **创建 Service**: `RoleService`、`UserRoleService` 及基础 CRUD。
4. [x] **播种默认角色**: 在 `mysql.txt` 或通过数据 SQL 文件插入：`ADMIN`、`STUDENT`、`TA`、`INSTRUCTOR`。
5. [x] **修改 `CustomUserDetailsService`**: 从 `user_roles` + `roles` 表加载角色并映射为 `GrantedAuthority`。
6. [x] **修改 `LoginUser.getAuthorities()`**: 返回真实角色而非硬编码 `ROLE_USER`。
7. [x] **修改 `JwtTokenProvider.generateToken`**: 在 JWT 声明中包含角色代码。
8. [x] **修改 `JwtAuthenticationFilter`**: 从 JWT 解析角色并重建 `GrantedAuthority` 列表。
9. [x] **更新 `SecurityConfig`**:
   - [x] `.requestMatchers("/admin/**").hasRole("ADMIN")`
   - [x] `.requestMatchers(HttpMethod.POST, "/courses").hasAnyRole("INSTRUCTOR", "ADMIN")`
   - [x] 其余端点保持 `.authenticated()` 基线。
10. [x] **通过 SQL 或启动脚本创建带角色的测试用户**。

### 验收标准
- [x] 具有 `STUDENT` 角色的用户无法访问 `/admin/**`（收到 403）。
- [x] 具有 `ADMIN` 角色的用户可以访问 `/admin/**`。
- [x] `GET /auth/me` 除 userId 和 username 外，还返回用户角色列表。
- [x] JWT 令牌载荷包含 `roles` 声明。
- [x] 角色数据从 `roles` 和 `user_roles` 表加载，非硬编码。

### 测试方法
1. 创建测试用户：`alice`（STUDENT）、`bob`（INSTRUCTOR）、`charlie`（ADMIN）。
2. 分别以各用户登录，提取 JWT。
3. 使用 JWT 调用受保护端点，断言 200 与 403。
4. 单元测试 `CustomUserDetailsService`（Mock Mapper）。

### 代码审查清单
- [x] 安全代码中不存在硬编码 `ROLE_USER`。
- [x] 角色查询使用恰当 JOIN（避免 N+1）。
- [x] JWT 声明中的角色字符串与 `roles.role_code` 完全匹配。
- [x] `SecurityConfig` 可读且按角色分组。

### 推送条件
- [x] 所有基于角色的授权测试通过。
- [x] 现有认证流程（注册/登录/me）仍然正常。
- [x] `mvn clean test` 通过。

---

## 里程碑 3：管理员管理模块

**状态**: `[x]` **已完成**  
**依赖**: 里程碑 2  
**预估工作量**: 小（1-2 个会话）  
**完成日期**: 2026-08-17

### 目标
提供仅限管理员使用的用户与系统管理端点。

### 子任务
1. [x] **创建 `AdminController`**，路径为 `/admin/**`。
2. [x] **端点**: `GET /admin/users` — 分页用户列表（查询参数：`pageNum`、`pageSize`、`role`、`status`）。
3. [x] **端点**: `GET /admin/users/{id}` — 管理员查看任意用户。
4. [x] **端点**: `PATCH /admin/users/{id}/status` — 启用/禁用用户（`status: 0 或 1`）。
5. [x] **端点**: `GET /admin/dashboard/stats` — 统计：总用户数、总课程数、总作业数、总提交数。
6. [x] **创建 `AdminUserListVO`**（可包含比普通 `UserVO` 更多的字段）。
7. [x] **创建 `PageResult<T>`** 分页响应包装器（为后续 PageHelper 做准备）。

### 验收标准
- [x] `GET /admin/users` 返回分页列表；仅 ADMIN 角色可访问。
- [x] `PATCH /admin/users/{id}/status` 正确切换数据库中的用户状态。
- [x] 已禁用用户（`status = 0`）无法登录。
- [x] 仪表盘统计数据反映真实数据库计数。

### 测试方法
1. [x] 对 `/admin/**` 进行不同角色的 MockMvc 测试 → 断言非管理员返回 403。
2. [x] 集成测试：禁用用户 → 尝试登录 → 断言失败。
3. [x] 验证分页参数生效（limit、offset 等价物）。

### 代码审查清单
- [x] 所有 `/admin/**` 端点已正确保护。
- [x] 即使在管理员列表中也不直接暴露 `passwordHash`。
- [x] 状态切换是原子的（使用 `@Transactional`）。
- [x] 分页默认值合理（pageNum=1, pageSize=10）。

### 推送条件
- [x] 管理员端点已用管理员与非管理员 JWT 测试。
- [x] `mvn clean test` 通过。

---

## 里程碑 4：课程管理模块

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 3  
**预估工作量**: 中（2-3 个会话）

### 目标
课程的完整增删改查、选课管理及按角色范围划分的列表查询。

### 子任务
1. [ ] **创建 `Course` 实体**，对应 `courses` 表。
2. [ ] **创建 `CourseMember` 实体**，对应 `course_members` 表。
3. [ ] **创建 Mapper**: `CourseMapper.xml`、`CourseMemberMapper.xml`。
4. [ ] **创建 DTO**: `CreateCourseRequestDTO`、`UpdateCourseRequestDTO`、`EnrollMemberRequestDTO`。
5. [ ] **创建 VO**: `CourseVO`、`CourseDetailVO`、`CourseMemberVO`。
6. [ ] **创建 `CourseService` / `CourseServiceImpl`**:
   - `createCourse`: 仅教师可用。设置 `created_by` 为当前用户。
   - `updateCourse`: 仅限创建者或管理员。
   - `deleteCourse`: 逻辑删除。
   - `listCourses`: 按角色区分的查询（见需求）。
   - `getCourseDetail`: 带成员列表。
7. [ ] **创建 `CourseController`**:
   - `POST /courses`
   - `GET /courses`
   - `GET /courses/{id}`
   - `PUT /courses/{id}`
   - `DELETE /courses/{id}`
   - `POST /courses/{id}/members`（添加成员）
   - `DELETE /courses/{id}/members/{userId}`（移除成员）
8. [ ] **Service 层权限检查**: 允许修改前校验当前用户是否为课程教师/管理员。

### 验收标准
- [ ] 教师可以创建课程并隐式成为成员。
- [ ] `(course_code, semester)` 唯一性受约束（数据库 + 业务检查）。
- [ ] 学生调用 `POST /courses` 收到 403。
- [ ] 已选课学生在 `GET /courses` 中可见该课程；未选课用户不可见。
- [ ] `GET /courses/{id}` 对教师包含成员列表；对学生仅显示自己。

### 测试方法
1. 集成测试:
   - 创建课程 → 断言存在。
   - 重复 course_code + semester → 断言 409。
   - 添加学生成员 → 列出成员 → 断言存在。
2. 安全测试:
   - 学生尝试创建课程 → 403。
   - 非成员尝试查看课程 → 403。

### 代码审查清单
- [ ] 所有查询检查逻辑删除（`is_deleted = 0`）。
- [ ] `created_by` 和 `updated_by` 从 JWT 获取，而非请求体。
- [ ] 添加成员前检查用户是否存在。
- [ ] 获取课程及成员时无 N+1 查询问题（使用 JOIN 或嵌套 resultMap）。

### 推送条件
- [ ] 所有增删改查端点通过集成测试。
- [ ] 每个端点已验证基于角色的访问控制。
- [ ] `mvn clean test` 通过。

---

## 里程碑 5：作业管理模块

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 4  
**预估工作量**: 中（2-3 个会话）

### 目标
教师可在课程内发布作业，学生可查看已发布作业。

### 子任务
1. [ ] **创建 `Assignment` 实体**。
2. [ ] **创建 `AssignmentFile` 实体**。
3. [ ] **创建 Mapper**: `AssignmentMapper.xml`、`AssignmentFileMapper.xml`。
4. [ ] **创建 DTO**: `CreateAssignmentRequestDTO`、`UpdateAssignmentRequestDTO`。
5. [ ] **创建 VO**: `AssignmentVO`、`AssignmentDetailVO`。
6. [ ] **创建 `AssignmentService` / `AssignmentServiceImpl`**:
   - `createAssignment`: 仅限其课程的教师/助教。
   - `updateAssignment`: 仅在截止前或草稿状态时允许。
   - `publishAssignment`: 状态从草稿(0)变为已发布(1)。
   - `closeAssignment`: 状态变为已关闭(2)。
   - `listAssignmentsByCourse`: 学生仅见已发布；教师见全部。
   - `getAssignmentDetail`: 带文件附件。
7. [ ] **创建 `AssignmentController`**，路径为 `/courses/{courseId}/assignments`（嵌套资源）。
8. [ ] **作业文件附件端点**（占位；完整上传在里程碑 7）:
   - `POST /courses/{courseId}/assignments/{assignmentId}/files`
   - `GET /courses/{courseId}/assignments/{assignmentId}/files`

### 验收标准
- [ ] 教师可在拥有的课程中创建作业。
- [ ] 学生仅在状态 = 1（已发布）后可见作业。
- [ ] 草稿作业对学生不可见。
- [ ] 作业截止时间受强制执行（统一存储为 UTC 或本地时间）。
- [ ] 作业列表限定于课程（校验 course ID）。

### 测试方法
1. 增删改查集成测试。
2. 状态转换测试：草稿 → 已发布 → 已关闭。
3. 安全测试：学生尝试创建作业 → 403。
4. 验证 `course_id` 外键约束行为。

### 代码审查清单
- [ ] `created_by` 从 JWT 获取，非请求体。
- [ ] `due_time` 格式已记录且一致（推荐 ISO 8601）。
- [ ] 嵌套 URL 路径正确映射课程-作业关系。
- [ ] 应用逻辑删除。

### 推送条件
- [ ] 作业生命周期（创建、发布、关闭）已测试。
- [ ] 基于角色的可见性已测试。
- [ ] `mvn clean test` 通过。

---

## 里程碑 6：学生提交模块

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 5  
**预估工作量**: 中（2-3 个会话）

### 目标
学生可提交作业响应，系统强制执行提交次数限制和迟交规则。

### 子任务
1. [ ] **创建 `Submission` 实体**。
2. [ ] **创建 `SubmissionFile` 实体**。
3. [ ] **创建 Mapper**: `SubmissionMapper.xml`、`SubmissionFileMapper.xml`。
4. [ ] **创建 DTO**: `CreateSubmissionRequestDTO`。
5. [ ] **创建 VO**: `SubmissionVO`、`SubmissionDetailVO`。
6. [ ] **创建 `SubmissionService` / `SubmissionServiceImpl`**:
   - `createSubmission`: 仅限学生。
     - 校验学生是否已加入课程。
     - 检查 `max_submission_times`: 超限则拒绝。
     - 检查 `due_time` 与 `allow_late_submission`: 不允许迟交时拒绝迟交。
     - 按 (assignment_id, student_id) 自动递增 `submission_no`。
     - 自动设置 `is_late`。
   - `saveDraft`: 同上，但 `status = 0`。
   - `listMySubmissions`: 针对特定作业。
   - `getSubmissionDetail`: 学生看自己的；教师看课程内任何提交。
7. [ ] **创建 `SubmissionController`**，路径为 `/courses/{courseId}/assignments/{assignmentId}/submissions`。

### 验收标准
- [ ] 学生最多可提交 `max_submission_times` 次。
- [ ] 第 4 次提交（若上限为 3）被拒绝并返回明确错误。
- [ ] 截止后提交且 `allow_late_submission = 0` 时被拒绝。
- [ ] 截止后提交且允许迟交时被接受，`is_late = 1`。
- [ ] `submission_no` 正确递增：1、2、3。
- [ ] 教师可查看某作业的所有提交。
- [ ] 学生无法查看其他学生的提交。

### 测试方法
1. 集成测试:
   - 提交 3 次 → 每次断言成功。
   - 第 4 次提交 → 断言 409 或 400 及提示信息。
   - 迟交标志为 false 时截止后提交 → 断言拒绝。
   - 迟交标志为 true 时截止后提交 → 断言成功且 `is_late=1`。
2. 安全测试:
   - 未加入课程的用户提交 → 403。
   - 学生查看同伴提交 → 403。

### 代码审查清单
- [ ] 提交次数检查是原子的（考虑竞态条件；使用数据库唯一约束作为安全网）。
- [ ] `due_time` 与 `submitted_at` 的时区处理一致。
- [ ] `is_late` 由业务层计算，不接受客户端传入。
- [ ] `student_id` 来自 JWT，非请求体。

### 推送条件
- [ ] 提交次数限制和迟交逻辑已充分测试。
- [ ] 安全边界已验证。
- [ ] `mvn clean test` 通过。

---

## 里程碑 7：文件上传与存储

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 6  
**预估工作量**: 中（2 个会话）

### 目标
支持作业附件、提交文件和课程资料的多部分文件上传。

### 子任务
1. [ ] **在 `application.properties` 中配置多部分上传**:
   - `spring.servlet.multipart.max-file-size=10MB`
   - `spring.servlet.multipart.max-request-size=50MB`
2. [ ] **创建 `FileStorageService`**:
   - `storeFile(MultipartFile, String subDirectory)`: 保存到本地文件系统 `/uploads/{subDirectory}/` 下。
   - 生成唯一文件名（UUID + 原始扩展名）。
   - 返回可访问的 URL 路径。
3. [ ] **创建 `FileDownloadController`**:
   - `GET /files/{filename}`: 以正确 `Content-Type` 提供文件。
   - 提供文件前进行角色检查（校验用户是否有权访问与该文件关联的课程）。
4. [ ] **实现作业文件上传**（`POST /courses/{courseId}/assignments/{assignmentId}/files`）。
5. [ ] **实现提交文件上传**（`POST /courses/{courseId}/assignments/{assignmentId}/submissions/{submissionId}/files`）。
6. [ ] **实现课程资料上传**（`POST /courses/{courseId}/materials`）。
7. [ ] **文件类型白名单**: 拒绝非允许扩展名。
8. [ ] **创建 `FileUtil`**: 提取扩展名、校验 MIME 类型。

### 验收标准
- [ ] 教师可上传 PDF 到作业；文件出现在作业详情中。
- [ ] 学生可上传 ZIP 作为提交。
- [ ] 超出大小限制时返回 400 及明确提示。
- [ ] 不允许的文件类型（例如 `.exe`）被拒绝。
- [ ] 下载端点仅向课程成员提供文件。
- [ ] 文件存储在 Web 根目录之外；无法通过静态路径直接访问。

### 测试方法
1. 使用 `MockMvc` + `MockMultipartFile` 进行集成测试:
   - 上传有效文件 → 断言 200 且数据库记录已创建。
   - 上传 `.exe` → 断言 400。
   - 上传超大文件 → 断言 400。
2. 手动测试：通过浏览器/curl 携带 JWT 下载文件。

### 代码审查清单
- [ ] 文件名已净化（防止目录遍历如 `../../../etc/passwd`）。
- [ ] 唯一文件名生成防止覆盖。
- [ ] 文件类型检查使用白名单，**不是**黑名单。
- [ ] 下载在流式传输字节前执行授权检查。
- [ ] 数据库中准确记录 `file_size`。

### 推送条件
- [ ] 三种上下文（作业、提交、资料）的上传/下载流程均已测试。
- [ ] 未授权下载的安全测试通过。
- [ ] `mvn clean test` 通过。

---

## 里程碑 8：评分与反馈模块

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 7  
**预估工作量**: 中（2 个会话）

### 目标
教师和助教可为提交打分，学生可查看成绩。

### 子任务
1. [ ] **创建 `Grade` 实体**。
2. [ ] **创建 Mapper**: `GradeMapper.xml`。
3. [ ] **创建 DTO**: `CreateGradeRequestDTO`、`UpdateGradeRequestDTO`。
4. [ ] **创建 VO**: `GradeVO`、`GradeWithSubmissionVO`。
5. [ ] **创建 `GradeService` / `GradeServiceImpl`**:
   - `createGrade`: 仅限助教/教师。检查提交是否属于其课程。
   - `updateGrade`: 仅当状态为草稿，或带有重新评分标志时。
   - `getGradeBySubmission`: 学生看自己的；教师看任何。
   - `getGradeStatistics`: 每作业的平均分、中位数、最高分、最低分（仅教师）。
6. [ ] **创建 `GradeController`**，路径为 `/courses/{courseId}/assignments/{assignmentId}/submissions/{submissionId}/grade`。
7. [ ] **提交状态更新**: 评分时更新 `submissions.status` 为 3（已评分）。

### 验收标准
- [ ] 助教可为提交创建评分。
- [ ] 同一提交的重复评分被阻止（数据库唯一约束 + 业务检查）。
- [ ] 分数不可超过作业的 `total_score`。
- [ ] 学生可查看自己的评分。
- [ ] 学生无法查看其他学生的评分。
- [ ] 评分统计准确。

### 测试方法
1. 集成测试:
   - 为提交评分 → 断言 `grades` 和 `submissions.status = 3` 的数据库记录。
   - 再次为同一提交评分 → 断言 409。
   - 分数 > total_score → 断言 400。
2. 安全测试:
   - 学生尝试评分 → 403。
   - 学生查看同伴评分 → 403。

### 代码审查清单
- [ ] 评分创建与提交状态更新使用 `@Transactional`。
- [ ] `scorer_id` 来自 JWT，非请求体。
- [ ] 分数校验在业务层执行。
- [ ] 统计查询高效（使用 SQL 聚合函数）。

### 推送条件
- [ ] 评分增删改查和统计已测试。
- [ ] 角色边界已验证。
- [ ] `mvn clean test` 通过。

---

## 里程碑 9：课程资料模块

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 8  
**预估工作量**: 小（1-2 个会话）

### 目标
教师可上传课程资料，学生可下载。

### 子任务
1. [ ] **创建 `CourseMaterial` 实体**。
2. [ ] **创建 Mapper**: `CourseMaterialMapper.xml`。
3. [ ] **创建 DTO**: `UploadMaterialRequestDTO`。
4. [ ] **创建 VO**: `CourseMaterialVO`。
5. [ ] **创建 `CourseMaterialService` / `CourseMaterialServiceImpl`**:
   - `uploadMaterial`: 仅限教师。
   - `listMaterialsByCourse`: 所有课程成员。
   - `deleteMaterial`: 上传者或管理员执行逻辑删除。
6. [ ] **创建 `CourseMaterialController`**，路径为 `/courses/{courseId}/materials`。
7. [ ] 复用里程碑 7 的 `FileStorageService`。

### 验收标准
- [ ] 教师上传资料 → 出现在课程资料列表中。
- [ ] 学生可列出并下载已加入课程的资料。
- [ ] 非成员无法访问资料。
- [ ] 已删除资料被隐藏但数据库记录保留。

### 测试方法
1. 集成测试：上传 → 列出 → 下载 → 删除 → 再次列出（断言隐藏）。
2. 安全测试：非成员访问 → 403。

### 代码审查清单
- [ ] 复用现有文件存储逻辑；无重复代码。
- [ ] `uploaded_by` 从 JWT 获取。
- [ ] 应用逻辑删除。

### 推送条件
- [ ] 资料生命周期已测试。
- [ ] `mvn clean test` 通过。

---

## 里程碑 10：分页、缓存与打磨

**状态**: `[ ]` **未开始**  
**依赖**: 里程碑 9  
**预估工作量**: 中（2 个会话）

### 目标
使所有列表接口具备生产级分页，为热点数据引入缓存。

### 子任务
1. [ ] **在 `pom.xml` 中添加 PageHelper 依赖**。
2. [ ] **为所有列表接口添加分页**:
   - `GET /admin/users`
   - `GET /courses`
   - `GET /courses/{id}/assignments`
   - `GET /courses/{id}/assignments/{aid}/submissions`
   - `GET /courses/{id}/materials`
3. [ ] **创建 `PageResult<T>`** 包装器，包含 `list`、`total`、`pageNum`、`pageSize`。
4. [ ] **添加 Redis**（当前阶段可选；可先使用本地缓存）:
   - 按 ID 缓存用户详情。
   - 按 ID 缓存课程详情。
   - 更新/删除时使缓存失效。
5. [ ] **添加请求日志**: 通过 Spring AOP 或 HandlerInterceptor 记录方法、URL、用户 ID、耗时。
6. [ ] **添加 API 文档**: 为所有端点添加 SpringDoc OpenAPI。
7. [ ] **代码清理**: 删除未使用导入，确保所有类都有包级 Javadoc。

### 验收标准
- [ ] 所有列表接口接受 `pageNum` 和 `pageSize` 并返回 `PageResult<T>`。
- [ ] 分页默认值防止无限制查询。
- [ ] Redis 缓存减少重复的用户/课程查询（如启用）。
- [ ] API 文档可在 `/swagger-ui.html` 访问。

### 测试方法
1. 以不同页大小请求列表 → 断言正确切片。
2. 请求最后一页 → 若超出总数则断言空列表。
3. 性能测试（非正式）：重复调用缓存端点显示数据库查询减少。

### 代码审查清单
- [ ] PageHelper 配置正确（方言 = MySQL）。
- [ ] 缓存失效逻辑覆盖更新/删除路径。
- [ ] 日志中无敏感数据。
- [ ] 所有控制器都有 OpenAPI 注解。

### 推送条件
- [ ] 所有列表接口已分页。
- [ ] `mvn clean test` 通过。
- [ ] 应用干净启动，无警告。

---

# 前端里程碑

> 前端里程碑与后端里程碑**并行推进**，但按依赖关系排序。每个前端里程碑验收通过后可独立推送前端仓库（若前端独立仓库）或与对应后端里程碑合并推送（若前后端同仓库）。

---

## 前端里程碑 1（F1）：前端项目搭建与认证页面

**状态**: `[x]` **已完成**  
**依赖**: 后端里程碑 0（JWT 认证已完成）  
**预估工作量**: 中（2 个会话）  
**完成日期**: 2026-08-17

> **备注**: 项目脚手架、Axios/Pinia/路由、登录/注册页功能已完成；UI 已根据 `frontend-design` plugin  redesign 规范重构；前后端联调通过。

### 目标
搭建 Vue 3 前端工程，完成登录页和注册页，实现与后端 JWT 认证的完整对接。

### 子任务
1. [x] **初始化 Vue 3 项目**: `npm create vite@latest gradescope-frontend -- --template vue-ts`。
2. [x] **安装依赖**: `vue-router`、`pinia`、`axios`、`element-plus`、`@element-plus/icons-vue`。
3. [x] **配置 Vite**: 设置 `@/` 别名指向 `src/`，配置代理解决开发阶段 CORS（`proxy: { '/api': 'http://localhost:8080' }`）。
4. [x] **创建目录结构**: `api/`、`components/`、`views/`、`stores/`、`types/`、`utils/`、`router/`、`layouts/`。
5. [x] **Axios 封装**: `src/api/request.ts`，配置 baseURL、请求拦截器（注入 JWT）、响应拦截器（处理 401/403/500）。
6. [x] **Pinia Auth Store**: `useAuthStore`，含 `token`、`userInfo`、`login()`、`logout()`、`isAuthenticated`。
7. [x] **路由配置**: 定义 `/login`、`/register` 路由；配置 `beforeEach` 路由守卫（无令牌跳转登录）。
8. [x] **登录页 (`LoginView`)**: 表单（用户名、密码），调用 `POST /auth/login`，成功存储 JWT 并跳转仪表盘。
9. [x] **注册页 (`RegisterView`)**: 表单（用户名、密码、真实姓名、邮箱、电话、学号），调用 `POST /auth/register`，成功后跳转登录页。
10. [x] **布局组件**: `AuthLayout`（登录/注册页使用的简洁布局，无侧边栏）。
11. [x] **类型定义**: `src/types/auth.ts` 定义 `LoginRequest`、`RegisterRequest`、`LoginResponse`、`UserVO` 等接口。
12. [x] **UI 重设计**: 基于 `frontend-design` plugin 的设计规范，重构 `style.css`、`AuthLayout.vue`、`LoginView.vue`、`RegisterView.vue`。
13. [x] **中文化**: 登录/注册页所有文案、验证提示、品牌面板文案全部改为中文。
14. [x] **修复提交按钮不可见**: 移除导致按钮在部分场景下无法显示的 `opacity: 0` + 入场动画写法。
15. [x] **修复认证 403**: Vite 代理增加 `/api` rewrite；修正 `SecurityConfig` 包声明。
16. [x] **开发测试账号**: 新增 `DataSeeder`，启动后端时自动创建 alice/bob/charlie 三个测试账号。

### 验收标准
- [x] `npm run dev` 启动前端，无编译错误。
- [x] 访问 `http://localhost:5173/login` 显示登录页，Element Plus 样式正常。
- [x] 输入正确凭据登录 → 成功存储 token 到 `localStorage`，页面跳转 `/dashboard`。（已通过 Playwright 联调验证）
- [x] 输入错误密码登录 → 顶部弹出错误提示（后端 `message` 内容）。（已通过 Playwright 联调验证）
- [x] 注册新用户 → 后端创建用户，成功后跳转登录页。（已通过 Playwright 联调验证）
- [x] 直接访问需要登录的页面（如 `/dashboard`）→ 无令牌时自动跳转 `/login`。
- [x] 刷新页面后，若 `localStorage` 有有效 token，保持登录状态（通过 `authStore` 初始化读取）。（已通过 Playwright 联调验证）
- [x] `npm run build` 构建成功，无 TypeScript/Vue 编译错误。
- [x] 登录页与注册页视觉风格符合 `frontend-design` redesign 规范。

### 测试方法
1. **手动测试**: 使用浏览器开发者工具检查 Network 面板，确认请求头部携带 `Authorization: Bearer ...`。
2. **手动测试**: 在 `Application > Local Storage` 中确认 token 已存储。
3. **手动测试**: 删除 `localStorage` 中的 token，刷新页面，确认跳转登录页。

### 代码审查清单
- [x] 前端 `.env.development` 中 API 基地址正确。
- [x] Axios 拦截器不泄露敏感信息到控制台。
- [x] 表单提交按钮在请求期间显示 loading 状态。
- [x] 所有 TypeScript 接口字段名与后端 DTO/VO 一致。
- [x] 无 `any` 类型滥用（`tsconfig` 开启 `strict`）。
- [x] 设计系统使用 CSS 变量，颜色/字体/圆角/阴影统一。
- [x] 装饰性 Margin Rule 已设置 `aria-hidden`，不影响无障碍。

### 推送条件
- [x] 登录/注册页面在浏览器中可渲染（样式、布局、动画）。
- [x] `npm run build` 无 TypeScript 编译错误。
- [x] 登录/注册流程在浏览器中完全跑通。（已通过 Playwright + 系统 Chrome 端到端验证）
- [x] 无 ESLint 警告（`npm run lint`）或已记录可接受的警告。

---

## 前端里程碑 2（F2）：学生仪表盘与课程页面

**状态**: `[ ]` **未开始**  
**依赖**: 前端里程碑 F1、后端里程碑 4（课程管理）  
**预估工作量**: 中（2-3 个会话）

### 目标
学生可查看仪表盘、浏览课程、查看作业详情并提交作业。

### 子任务
1. [ ] **主布局 (`MainLayout`)**: 左侧侧边栏导航（按角色动态菜单）、顶部栏（显示用户名、头像、退出按钮）。
2. [ ] **动态菜单**: 学生菜单项：仪表盘、我的课程、我的成绩、个人中心。
3. [ ] **学生仪表盘 (`StudentDashboard`)**: 展示已加入课程卡片、即将截止作业列表（带倒计时）、最新成绩。
4. [ ] **课程列表页 (`StudentCourseList`)**: 卡片式布局展示学生已加入的所有课程。
5. [ ] **课程详情页-学生视角 (`StudentCourseDetail`)**: 课程信息、资料列表、作业列表（标签页切换）。
6. [ ] **作业详情页-学生视角 (`StudentAssignmentDetail`)**: 作业描述、附件下载、历史提交记录表格、最新评分显示。
7. [ ] **提交作业页 (`SubmitAssignment`)**: 文本输入框（`ElInput` type="textarea"）、文件上传组件（`ElUpload`）、剩余提交次数和截止时间显示。
8. [ ] **个人中心 (`ProfileView`)**: 修改资料表单、修改密码表单、头像上传占位。
9. [ ] **API 模块**: `course.ts`、`assignment.ts`、`submission.ts`、`user.ts`。
10. [ ] **类型定义**: `CourseVO`、`AssignmentVO`、`SubmissionVO` 等。

### 验收标准
- [ ] 学生登录后看到学生仪表盘，侧边栏仅显示学生菜单。
- [ ] 点击课程卡片进入课程详情，可见作业列表和资料列表。
- [ ] 点击作业进入作业详情，可见"提交作业"按钮。
- [ ] 提交作业时，若超过 `max_submission_times`，前端按钮置灰并提示"已达最大提交次数"。
- [ ] 提交作业后，历史提交记录表格自动刷新。
- [ ] 截止后不允许迟交的作业，"提交"按钮自动禁用并显示"已截止"。

### 测试方法
1. 手动测试完整学生流程：登录 → 查看仪表盘 → 进入课程 → 查看作业 → 提交作业 → 查看历史提交。
2. 检查 Network 面板确认所有 API 请求都携带 JWT。
3. 测试边界：无课程的学生仪表盘显示空状态插图。

### 代码审查清单
- [ ] 所有列表页处理空状态。
- [ ] 文件上传组件限制文件类型和大小（前端预校验）。
- [ ] 提交按钮有 loading 状态防止重复提交。
- [ ] 课程详情页的标签页切换使用 `ElTabs`。

### 推送条件
- [ ] 学生核心流程（仪表盘 → 课程 → 作业 → 提交）手动测试通过。
- [ ] `npm run build` 无错误。

---

## 前端里程碑 3（F3）：教师仪表盘与课程/作业管理

**状态**: `[ ]` **未开始**  
**依赖**: 前端里程碑 F2、后端里程碑 4-5（课程与作业管理）  
**预估工作量**: 中（2-3 个会话）

### 目标
教师/助教可管理课程、发布作业、查看学生提交。

### 子任务
1. [ ] **教师菜单**: 仪表盘、课程管理、作业管理（动态菜单根据角色渲染）。
2. [ ] **教师仪表盘 (`InstructorDashboard`)**: 管理的课程列表、待评分提交数量提示、最近学生活动。
3. [ ] **课程管理页 (`CourseManageView`)**: 课程表格（分页）、创建课程对话框（`ElDialog` + 表单）、编辑课程、删除课程（二次确认）。
4. [ ] **课程成员管理 (`CourseMemberManage`)**: 成员表格、添加成员对话框（输入用户名/邮箱，选择角色）、移除成员（二次确认）。
5. [ ] **作业管理页 (`AssignmentManageView`)**: 作业列表、创建作业对话框（含截止时间选择器 `ElDatePicker`、分数输入、迟交开关）、发布/关闭作业按钮。
6. [ ] **作业详情-教师视角 (`InstructorAssignmentDetail`)**: 作业统计卡片（提交人数、未提交人数）、所有学生提交列表（表格，含提交时间、是否迟交、状态）。
7. [ ] **上传参考文件**: 在作业详情页使用 `ElUpload` 上传 PDF/ZIP 附件。
8. [ ] **面包屑导航**: 所有教师页面显示面包屑（课程管理 > CS101 > 作业管理）。

### 验收标准
- [ ] 教师登录后看到教师仪表盘，侧边栏显示教师菜单。
- [ ] 教师可创建课程，创建成功后出现在课程列表第一行。
- [ ] 教师可在课程中添加学生/助教，成员列表实时更新。
- [ ] 教师可发布作业，发布后学生在作业列表中可见。
- [ ] 教师可查看某作业的所有学生提交，未提交的学生显示"未提交"标记。
- [ ] 删除操作必须弹出二次确认对话框，确认后才发送请求。

### 测试方法
1. 手动测试教师流程：登录 → 创建课程 → 添加成员 → 发布作业 → 查看提交列表。
2. 测试边界：空课程列表显示创建引导；无成员时显示添加引导。

### 代码审查清单
- [ ] 所有创建/编辑操作使用对话框而非跳转新页面，保持上下文。
- [ ] 表格操作列（编辑/删除）使用 `ElButton` 图标按钮。
- [ ] 表单提交前进行前端校验（必填项、格式）。
- [ ] 分页组件正确使用（`ElPagination`）。

### 推送条件
- [ ] 教师核心管理流程手动测试通过。
- [ ] `npm run build` 无错误。

---

## 前端里程碑 4（F4）：评分页面与成绩统计

**状态**: `[ ]` **未开始**  
**依赖**: 前端里程碑 F3、后端里程碑 8（评分模块）  
**预估工作量**: 中（2 个会话）

### 目标
教师/助教可为学生提交评分，学生可查看成绩。

### 子任务
1. [ ] **评分页 (`GradeSubmissionView`)**: 左侧显示学生提交内容（文本 + 文件下载链接），右侧评分表单（分数输入 `ElInputNumber`、评语输入 `ElInput` type="textarea"）、保存草稿按钮、发布成绩按钮。
2. [ ] **成绩统计页 (`GradeStatisticsView`)**: `ECharts` 或 `vue-echarts` 绘制分数分布直方图；统计卡片显示平均分、中位数、最高分、最低分。
3. [ ] **学生成绩页 (`StudentGradesView`)**: 表格展示所有课程的所有作业成绩，支持按学期筛选。
4. [ ] **提交状态标签**: 提交列表中用 `ElTag` 显示状态（已提交/已评分/迟交）。
5. [ ] **成绩发布状态**: 评分后学生在作业详情页看到分数和评语；未发布前学生看不到。

### 验收标准
- [ ] 教师点击"评分"进入评分页，可输入分数和评语。
- [ ] 分数输入框限制最大值（作业 `total_score`），超出时前端提示。
- [ ] 发布成绩后，学生在作业详情页立即看到分数。
- [ ] 成绩统计图表正确反映数据分布。
- [ ] 学生成绩页按学期筛选后表格正确刷新。

### 测试方法
1. 手动测试评分流程：教师评分 → 学生刷新页面查看成绩。
2. 测试边界：所有学生均未提交时，统计页显示空状态。

### 代码审查清单
- [ ] 评分页布局响应式（小屏幕下上下堆叠而非左右分栏）。
- [ ] 图表组件懒加载，仅在进入统计页时加载。
- [ ] 分数输入框只允许数字，禁止负数。

### 推送条件
- [ ] 评分和成绩查看流程手动测试通过。
- [ ] `npm run build` 无错误。

---

## 前端里程碑 5（F5）：管理员后台与课程资料

**状态**: `[ ]` **未开始**  
**依赖**: 前端里程碑 F1、后端里程碑 3-9（Admin + 资料）  
**预估工作量**: 中（2 个会话）

### 目标
管理员可查看系统统计、管理用户；所有角色可使用课程资料功能。

### 子任务
1. [ ] **管理员菜单**: 仪表盘、用户管理（仅 ADMIN 可见）。
2. [ ] **管理员仪表盘 (`AdminDashboard`)**: 顶部统计卡片（用户数、课程数、作业数、提交数）；最近注册用户表格；简单折线图显示注册趋势。
3. [ ] **用户管理页 (`UserManageView`)**: `ElTable` 分页展示所有用户，支持按角色/状态筛选；每行有启用/禁用按钮。
4. [ ] **课程资料页 (`CourseMaterialsView`)**: 资料列表（卡片或表格）、上传资料按钮（仅限教师）、下载按钮。
5. [ ] **文件下载组件**: 点击下载时带 JWT 头部请求后端文件下载接口，浏览器自动保存。
6. [ ] **403 页面**: 无权限访问时显示友好提示和返回首页按钮。
7. [ ] **404 页面**: 路由不匹配时显示。

### 验收标准
- [ ] 管理员登录后看到管理员仪表盘，侧边栏有"用户管理"。
- [ ] 用户管理表格分页正确，筛选后总数更新。
- [ ] 禁用用户后，该用户无法再次登录（前端登录接口返回错误）。
- [ ] 教师可在课程资料页上传资料，学生可下载。
- [ ] 非课程成员访问课程资料页 → 403 页面。

### 测试方法
1. 手动测试管理员流程：登录 → 查看统计 → 禁用用户 → 验证该用户登录失败。
2. 手动测试资料流程：教师上传 → 学生下载。

### 代码审查清单
- [ ] 管理员页面表格列宽自适应，重要字段不省略。
- [ ] 统计数字使用动画计数效果（可选，提升体验）。
- [ ] 403/404 页面有返回首页的明确按钮。

### 推送条件
- [ ] 管理员核心功能和资料功能手动测试通过。
- [ ] `npm run build` 无错误。

---

## 前端里程碑 6（F6）：全局优化与部署准备

**状态**: `[ ]` **未开始**  
**依赖**: 前端里程碑 F5、后端里程碑 10  
**预估工作量**: 中（2 个会话）

### 目标
前端工程生产级打磨，完成部署配置。

### 子任务
1. [ ] **响应式适配**: 使用 Element Plus 的 `el-col` 和 `el-row`，确保表格在移动端可横向滚动，侧边栏在小屏幕可折叠为汉堡菜单。
2. [ ] **性能优化**: 路由懒加载（`component: () => import('@/views/...')`）；`vite-plugin-compression` 启用 Gzip。
3. [ ] **环境变量**: `.env.production` 配置生产 API 地址；构建输出目录配置为 `dist/`。
4. [ ] **Nginx 配置**: 提供 `nginx.conf` 模板，配置静态文件托管、反向代理到后端、单页应用路由回退（`try_files $uri $uri/ /index.html`）。
5. [ ] **Docker 支持**: 提供 `Dockerfile`（基于 `nginx:alpine`）和 `docker-compose.yml`（前后端 + MySQL 一键启动）。
6. [ ] **代码清理**: 删除未使用导入、注释掉的代码、调试用的 `console.log`。
7. [ ] **ESLint + Prettier**: 统一代码风格，提交前自动格式化（`husky` + `lint-staged` 可选）。

### 验收标准
- [ ] 在 375px 宽度的模拟设备上，所有页面可正常浏览（表格可横向滚动，表单元素不重叠）。
- [ ] `npm run build` 生成的 `dist/` 目录包含正确哈希化的 JS/CSS 文件。
- [ ] Nginx 配置下，直接访问 `/courses/1` 不返回 404（SPA 回退生效）。
- [ ] Docker Compose 一键启动后，`http://localhost` 可访问完整应用。
- [ ] 生产构建无 ESLint 错误和 TypeScript 编译错误。

### 测试方法
1. 使用浏览器 DevTools 的 Device Mode 测试移动端布局。
2. 使用 Lighthouse 进行性能评分，目标 Performance >= 70。
3. 手动测试 Docker Compose 完整部署流程。

### 代码审查清单
- [ ] 所有路由组件使用懒加载。
- [ ] 生产构建产物中无 `.map` 文件（或按需保留）。
- [ ] Nginx 配置中无敏感信息泄露。
- [ ] Docker 镜像体积最小化（使用多阶段构建）。

### 推送条件
- [ ] 响应式测试通过（桌面端 + 移动端）。
- [ ] 生产构建成功，Nginx 部署验证通过。
- [ ] 无 ESLint/TypeScript 错误。

---

## 已完成里程碑汇总

| 编号 | 里程碑 | 状态 | 完成日期 |
|------|--------|------|----------|
| 0 | 项目基础与 JWT 认证 | ✅ 已完成 | 2026-03-27 |
| 1 | 全局异常处理与输入校验 | ✅ 已完成 | 2026-06-25 |
| 2 | RBAC（基于角色的访问控制） | ✅ 已完成 | 2026-08-17 |
| 3 | 管理员管理模块 | ✅ 已完成 | 2026-08-17 |
| 4 | 课程管理模块 | ⬜ 未开始 | — |
| 5 | 作业管理模块 | ⬜ 未开始 | — |
| 6 | 学生提交模块 | ⬜ 未开始 | — |
| 7 | 文件上传与存储 | ⬜ 未开始 | — |
| 8 | 评分与反馈模块 | ⬜ 未开始 | — |
| 9 | 课程资料模块 | ⬜ 未开始 | — |
| 10 | 分页、缓存与打磨 | ⬜ 未开始 | — |
| F1 | 前端项目搭建与认证页面 | ✅ 已完成 | 2026-08-17 |
| F2 | 学生仪表盘与课程页面 | ⬜ 未开始 | — |
| F3 | 教师仪表盘与课程/作业管理 | ⬜ 未开始 | — |
| F4 | 评分页面与成绩统计 | ⬜ 未开始 | — |
| F5 | 管理员后台与课程资料 | ⬜ 未开始 | — |
| F6 | 全局优化与部署准备 | ⬜ 未开始 | — |
| 3 | 管理员管理模块 | ✅ 已完成 | 2026-08-17 |
| 4 | 课程管理模块 | ⬜ 未开始 | — |
| 5 | 作业管理模块 | ⬜ 未开始 | — |
| 6 | 学生提交模块 | ⬜ 未开始 | — |
| 7 | 文件上传与存储 | ⬜ 未开始 | — |
| 8 | 评分与反馈模块 | ⬜ 未开始 | — |
| 9 | 课程资料模块 | ⬜ 未开始 | — |
| 10 | 分页、缓存与打磨 | ⬜ 未开始 | — |

---

> **更新规则**: 里程碑完成时，将其状态更新为 `[x]`，填写完成日期，并附注与原始计划的任何偏差。若出现不适合现有里程碑的新需求，在末尾添加新里程碑，而非挤入已有里程碑中。
