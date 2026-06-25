# Gradescope Spring Boot —— 教学管理与作业评分系统

> 一个面向高校教学场景的作业管理系统，目标参考 [Gradescope](https://www.gradescope.com/)，覆盖课程管理、作业发布、学生提交、教师评分等核心教学闭环。

---

## 一、项目概述

本项目是一个基于 **Spring Boot 3.x** 构建的后端服务，采用经典的 Java 企业级分层架构，力求在“初学者友好”与“工业规范”之间取得平衡。系统当前已完成**用户管理**与 **JWT 认证**两大基础模块，为后续课程、作业、提交、评分等核心业务提供了坚实的身份与权限底座。

---

## 二、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 核心框架 |
| Java | 17 | JDK 版本 |
| MyBatis | 3.0.3 | ORM 框架，采用 **XML Mapper** 写法 |
| MySQL | 8.x | 关系型数据库 |
| Spring Security | 6.x | 安全与鉴权框架 |
| JJWT | 0.12.7 | JWT 令牌生成与解析 |
| Lombok | 1.18.30 | 减少样板代码 |
| Maven | — | 构建工具 |

> **注意**：当前版本尚未引入 Redis，所有状态均维护在 JWT 令牌中。

---

## 三、数据库设计

共规划 **11 张表**，覆盖用户、课程、作业、提交、评分、资料等完整教学业务：

| 表名 | 说明 |
|------|------|
| `roles` | 系统角色表（ADMIN / STUDENT / TA / TEACHER） |
| `users` | 用户表（登录账号、密码、个人信息、状态） |
| `user_roles` | 用户角色关联表（多对多） |
| `courses` | 课程表（课程编号、学期、描述、状态） |
| `course_members` | 课程成员表（学生在课程内的角色：STUDENT / TA / INSTRUCTOR） |
| `assignments` | 作业表（标题、描述、总分、截止时间、迟交策略、提交次数） |
| `assignment_files` | 作业附件表（教师发布的参考文件或附件） |
| `submissions` | 学生提交表（提交内容、提交时间、是否迟交、状态） |
| `submission_files` | 提交附件表（学生上传的作业文件） |
| `grades` | 评分表（得分、评语、评分人、评分时间） |
| `course_materials` | 课程资料表（课件、资料上传下载） |

### 设计亮点
- 所有表均包含 `created_at` / `updated_at` 审计字段。
- 统一使用 **逻辑删除**（`is_deleted`），避免物理删除导致数据丢失。
- 外键约束完整，保障数据一致性。
- 关键查询字段均建立索引（如 `uk_username`、`idx_course_id` 等）。

---

## 四、项目结构

```
com.example.gradescopespringboot
├── GradescopeSpringBootApplication.java   // 启动类
│
├── controller/                            // 控制层：接收 HTTP 请求
│   ├── UserController.java
│   └── AuthController.java
│
├── service/                               // 业务层：处理业务逻辑
│   ├── UserService.java
│   ├── AuthService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       └── AuthServiceImpl.java
│
├── mapper/                                // 数据访问层：MyBatis 接口
│   └── UserMapper.java
│
├── entity/                                // 数据库实体
│   └── User.java
│
├── dto/                                   // 数据传输对象：接收前端参数
│   └── auth/
│       ├── LoginRequestDTO.java
│       └── RegisterRequestDTO.java
│
├── vo/                                    // 视图对象：返回给前端
│   ├── auth/
│   │   ├── LoginResponseVO.java
│   │   └── RegisterResponseVO.java
│   └── user/
│       └── UserVO.java
│
├── converter/                             // 对象转换器（预留，目前使用 BeanUtils）
│
├── common/result/                         // 统一响应结构
│   └── Result.java
│
├── config/                                // 配置类
│   ├── PasswordConfig.java                // BCrypt 密码加密器
│   └── SecurityConfig.java                // Spring Security 过滤器链配置
│
└── security/                              // 安全模块
    ├── filter/
    │   └── JwtAuthenticationFilter.java   // JWT 请求拦截与解析
    ├── model/
    │   └── LoginUser.java                 // Spring Security 用户封装
    ├── service/
    │   └── CustomUserDetailsService.java  // 用户详情加载
    └── util/
        └── JwtTokenProvider.java          // JWT 生成、解析、验证工具

resources/
├── mapper/
│   └── UserMapper.xml                     // MyBatis SQL 映射文件
├── static/
│   └── auth-test.html                     // 前端 JWT 测试页面
└── application.properties                 // 应用配置
```

---

## 五、核心功能与实现细节

### 5.1 统一响应结构 `Result<T>`

所有接口均返回统一格式，便于前端统一处理：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

支持便捷构造：`Result.success(data)`、`Result.fail(code, message)`。

### 5.2 用户模块（User）

- **Entity** `User`：完整映射 `users` 表，包含 16 个字段，使用 Lombok `@Data` 简化 getter/setter。
- **Mapper**：`UserMapper.java` + `UserMapper.xml`，采用 XML 方式编写原生 SQL，已规避 `@MapperScan` 范围过大导致 Service 被误识别为 Mapper 的坑。
- **Service**：提供 `getById(Long)`、`getByUsername(String)`、`save(User)` 三个基础方法。
- **Controller**：`GET /users/{id}`，查询后通过 `BeanUtils.copyProperties` 转换为 `UserVO` 返回，**不将数据库实体直接暴露给前端**（已隐藏 `passwordHash`、`lastLoginAt`、`createdAt`、`updatedAt`、`isDeleted` 等敏感/内部字段）。

### 5.3 JWT 认证模块（Auth）

这是当前最完整的业务模块，实现了无状态登录鉴权：

#### 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 用户注册，密码经 BCrypt 加密存储 |
| POST | `/auth/login` | 用户登录，验证成功后返回 JWT |
| GET | `/auth/me` | 携带 JWT 获取当前登录用户信息 |

#### 安全设计

1. **密码安全**：使用 `BCryptPasswordEncoder` 对明文密码进行单向哈希，salt 自动嵌入，无需额外存储。
2. **JWT 令牌**：
   - 载荷包含 `userId` 与 `username`；
   - 使用 HS256 算法签名；
   - 有效期默认 24 小时（`86400000 ms`），密钥通过 `application.properties` 外部化配置。
3. **请求拦截**：`JwtAuthenticationFilter` 继承 `OncePerRequestFilter`，从 `Authorization: Bearer <token>` 头中提取并校验 JWT，校验通过后自动将用户信息注入 `SecurityContextHolder`。
4. **Spring Security 配置**：
   - 关闭 CSRF（前后端分离场景）；
   - Session 策略设为 `STATELESS`（完全无状态）；
   - `/auth/register`、`/auth/login`、`/auth/me`、`/auth-test.html` 允许匿名访问；
   - 其余接口均需认证。
5. **用户状态检查**：登录时校验 `status = 1`（正常）且 `isDeleted = 0`（未删除），被禁用或已删除的用户无法登录。

#### 测试支持

项目内置 `auth-test.html`（位于 `src/main/resources/static`），启动后访问 `http://localhost:8080/auth-test.html` 即可在浏览器中完成注册、登录、Token 存储与受保护接口测试，无需额外前端工程。

---

## 六、代码风格与工程约定

本项目在开发过程中刻意遵循以下规范，作为后续模块扩展的模板：

| 约定 | 说明 |
|------|------|
| **分层清晰** | Controller → Service → Mapper → Entity，禁止 Controller 直接操作数据库。 |
| **MyBatis XML** | 所有 SQL 写在 `resources/mapper/*.xml` 中，不使用注解 SQL，便于复杂查询维护。 |
| **DTO / VO 分离** | DTO 负责接收请求参数，VO 负责封装响应数据，Entity 只与数据库表映射。 |
| **统一返回** | Controller 层统一返回 `Result<T>`，禁止裸返回 Entity 或原始类型。 |
| **构造器注入** | Service、Controller 中优先使用构造器注入，降低对 `@Autowired` 字段注入的依赖。 |
| **敏感字段隔离** | 返回前端的 VO 中绝不包含 `passwordHash`、`isDeleted` 等内部字段。 |
| **异常处理** | Service 层对业务异常直接抛出 `RuntimeException`（后续将统一引入全局异常处理器）。 |

---

## 七、当前进度里程碑

- [x] Spring Boot 基础环境搭建（Maven、MySQL、MyBatis、Lombok）
- [x] 数据库 11 张表建表语句完成
- [x] MyBatis XML 映射与 `map-underscore-to-camel-case` 配置
- [x] 统一响应结构 `Result<T>`
- [x] User 模块：Entity、Mapper、Service、Controller 跑通完整链路
- [x] `UserMapperTest`、`UserServiceTest` 测试通过
- [x] Spring Security 引入并关闭默认登录页
- [x] **JWT 认证完成**：注册 / 登录 / Token 解析 / 受保护接口鉴权
- [ ] 全局异常处理（待补充 `@ControllerAdvice`）
- [ ] 角色权限控制（当前仅硬编码 `ROLE_USER`）
- [ ] 课程管理模块
- [ ] 作业发布与提交模块
- [ ] 文件上传与存储模块
- [ ] 评分与反馈模块

---

## 八、快速启动

### 8.1 环境要求

- JDK 17+
- MySQL 8.x
- Maven 3.8+

### 8.2 数据库准备

1. 创建数据库 `gradescope_db`。
2. 执行 `mysql.txt` 中的建表语句，创建全部 11 张表。

### 8.3 配置修改

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gradescope_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your-very-very-very-long-secret-key-change-me
jwt.expiration=86400000
```

### 8.4 运行

```bash
# 方式一：命令行
./mvnw spring-boot:run

# 方式二：IDE
直接运行 GradescopeSpringBootApplication.java
```

### 8.5 验证

- 浏览器打开 `http://localhost:8080/auth-test.html` 进行注册/登录测试。
- 或使用 curl：

```bash
# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'

# 获取当前用户（将 <token> 替换为登录返回的 token）
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer <token>"
```

---

## 九、后续规划

1. **全局异常处理**：引入 `@ControllerAdvice` + 自定义业务异常，统一封装错误响应。
2. **角色权限细化**：从 `user_roles` 表读取真实角色，替换当前硬编码的 `ROLE_USER`，实现基于角色的接口鉴权。
3. **课程管理**：CRUD 课程、添加课程成员、区分 INSTRUCTOR / TA / STUDENT 权限。
4. **作业与提交**：发布作业、学生多次提交、截止时间与迟交检测。
5. **文件存储**：接入本地或 OSS 存储，支持作业附件与提交文件上传下载。
6. **评分反馈**：教师/TA 在线评分、写评语、学生查看成绩。

---

> **作者备注**：本项目以“规范优先”为原则，每个新增模块都会严格遵循已建立的 Controller / Service / Mapper / Entity / DTO / VO 分层和统一返回结构，确保代码长期可维护。
