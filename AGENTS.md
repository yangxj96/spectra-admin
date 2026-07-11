# AGENTS.md

## 项目概述

Spectra Admin 是 Spectra 系统的**后端 API 服务**，同时为 Web 管理后台（`spectra-ui`）和移动端（`spectra-app`）提供接口。

- 技术栈：Java 25, Spring Boot 4, Maven 多模块
- 启动入口：`com.devops00.spectra.launch.LaunchApplication`
- 开发端口：**4004**（通过 `.mise.local.toml` 中的 `SERVER_PORT` 设置）
- 两个前端在开发环境均连接 `https://127.0.0.1:4004/`

## 构建与运行

```bash
# 构建（跳过测试以加快速度）
./mvnw clean package -DskipTests

# 本地运行（启动前端之前先启动此服务）
./mvnw spring-boot:run -pl spectra-launch

# 或运行构建好的 jar
java -jar spectra-launch/target/spectra-launch-*.jar
```

## 模块结构

```
spectra-config       → 统一配置文件（所有 application-*.yml 集中管理）
spectra-common       → 共享工具、DTO、MyBatis-Plus 配置
spectra-framework    → 平台配置、Redis、AOP、缓存、接口加解密（Advice）
spectra-starter/     → 自动配置 Starter
  spectra-security-base
  spectra-security-spring-boot-starter
  spectra-log-base
  spectra-log-spring-boot-starter
  spectra-ai-base
spectra-modules/     → 业务模块
  spectra-core       → 核心业务逻辑
  spectra-upload     → 文件上传（S3）
  spectra-workflow   → Flowable 工作流
  spectra-oa         → OA 模块
  spectra-ai         → AI 集成（LangChain4j）
spectra-launch       → 应用入口，运行此模块
```

## 环境配置

使用 mise 管理工具链。复制 `.mise.local.toml.example` 为 `.mise.local.toml` 并配置：

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL 连接
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_DB`, `REDIS_PASSWORD` - Redis 连接
- `DEFAULT_PASSWORD` - 默认用户密码
- `SSL_*` - SSL 配置（可选）
- `S3_*` - S3 存储配置
- `AI_KEY`, `AI_BASE_URL`, `AI_MODEL` - AI 服务配置
- `RAG_KEY`, `RAG_BASE_URL`, `RAG_MODEL` - RAG 检索增强生成配置
- `SPECTRA_SYSTEM_SM_ENABLED`, `SPECTRA_SYSTEM_SM_PUBLIC_KEY`, `SPECTRA_SYSTEM_SM_PRIVATE_KEY` - 接口加解密（默认关闭）

需要运行的服务：PostgreSQL, Redis。

## 关键约定

- **Java 25** Temurin 发行版
- **UTC 时区** 启动时强制设置（显示层处理本地时区格式化）
- **MapStruct 依赖顺序**：mapstruct → lombok → mapstruct-processor（不可重排，否则编译失败）
- **版本属性**：pom.xml 中使用 `${revision}`，由 `flatten-maven-plugin` 展平
- **Spring profiles**：`dev`（本地）、`prod`（Docker/部署）
- **配置文件管理**：所有 `application-*.yml` 集中在 `spectra-config` 模块，通过 `spectra-common` 传递依赖对全项目可见

## 代码风格与命名约定

### 注释
- 使用三斜杠（`///`）注释，而非 Javadoc 块注释
- 每个 Java 文件必须包含 Apache License 2.0 头部

### Git Commit Messages

基于 Conventional Commits 规范，针对中文团队适配。

#### 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 类型（type）定义

| 类型 | 说明 | 示例场景 |
|---|---|---|
| `feat` | 新功能 | 添加用户注册模块 |
| `fix` | 修复缺陷 | 修复登录页白屏问题 |
| `docs` | 文档变更 | 更新 API 接口文档 |
| `style` | 代码格式（不影响逻辑） | 调整缩进、补充分号 |
| `refactor` | 重构（非新功能、非修复） | 拆分过长的服务类 |
| `perf` | 性能优化 | 优化首页列表查询速度 |
| `test` | 测试相关 | 补充用户模块单元测试 |
| `chore` | 构建/工具/依赖变更 | 升级 Maven 版本 |
| `ci` | 持续集成配置 | 修改 GitHub Actions 流程 |
| `revert` | 回滚提交 | 回滚 v2.1.0 的登录重构 |

#### 规则

- **type**：必填，保留英文关键字（工具链兼容性好）
- **scope**：选填，使用中文模块名（如 `安全`、`用户`、`AI`、`工作流`）
- **description**：必填，中文简述，不超过 50 字符，使用动宾短语（「添加 xxx」「修复 xxx」），不加句号
- **body**：选填，说明变更原因和方案，每行不超过 72 字符
- **footer**：选填，标注 BREAKING CHANGE 或关联 Issue

#### 好的示例

```
feat(安全): 添加基于 RBAC 的细粒度权限控制
fix(支付): 修复微信支付回调签名验证失败的问题
perf(列表页): 优化大数据量表格的虚拟滚动渲染
refactor(网关): 将单体网关拆分为独立微服务
```

#### 反面示例

```
fix: 修了一个 bug
feat: 更新代码
chore: 改了点东西
```

### 命名约定
- 实体类：PascalCase，如 `User`、`BaseEntity`
- Controller：PascalCase + `Controller` 后缀
- Service：接口 PascalCase + `Service` 后缀，实现类 `ServiceImpl` 后缀
- 表单对象：PascalCase + `From` 后缀（注意：`From` 而非 `Form`）
- VO 对象：PascalCase + `VO` 后缀
- 包结构：`com.devops00.spectra.{module}.{layer}`

### 分层结构
```
controller/    → REST 端点
service/       → 业务逻辑接口
service/impl/  → Service 实现
mapper/        → MyBatis-Plus Mapper
javabean/
  entity/      → 数据库实体
  from/        → 请求表单对象
  vo/          → 响应视图对象
```

### 实体约定
- 使用 UUID 作为主键（`@TableId(type = IdType.INPUT)`）
- 包含审计字段：`createdBy`、`createdAt`、`updatedBy`、`updatedAt`
- 使用 `Instant deleted` 实现软删除（null = 未删除）
- 使用 `@Version` 实现乐观锁
- 使用 `@OrderBy` 设置默认排序

### Controller 约定
- 使用构造器注入（非字段注入）
- 使用 `@PreAuthorize` 进行权限控制
- 使用 `@ULog` 记录操作日志
- 在 Mapping 注解中使用 `version = "1.0.0+"` 进行 API 版本控制
- 使用 `@Validated(Verify.Insert.class)` 或 `@Validated(Verify.Update.class)` 进行分组校验

## 测试

```bash
# 运行全部测试
./mvnw test

# 运行指定模块的测试
./mvnw test -pl spectra-common
```

spectra-launch 模块当前无测试文件。

## Docker

构建需要完整 JDK 镜像以支持验证码/AWT：

```bash
# 在 spectra-launch 目录下执行
docker build --build-arg JAR_FILE=spectra-launch-*.jar -t spectra-admin .
```

若使用 Spring Boot build-image 目标，请使用 `paketobuildpacks/builder-jammy-full`。

## CI/CD

GitHub Actions 工作流：`.github/workflows/spectra-minimal-image.yml`
- 手动触发（`workflow_dispatch`）
- 构建 Maven 项目，创建 Docker 镜像，推送到 GHCR

## 常见陷阱

- `.mise.local.toml` 在 gitignore 中——运行时必须从 example 创建
- 原生 JVM 参数：`--add-modules ALL-SYSTEM --enable-native-access=ALL-UNNAMED`
- PostgreSQL 默认端口 5432，Redis 默认端口 6379
- 此处 API 变更会直接影响 `spectra-ui` 和 `spectra-app`——修改端点时需与前端协调

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
