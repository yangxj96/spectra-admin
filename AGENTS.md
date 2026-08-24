# AGENTS.md

## 项目概述

Spectra Admin 是 Spectra 系统的**后端 API 服务**，同时为 Web 管理后台（`spectra-ui`）和移动端（`spectra-app`）提供接口。

- 技术栈：Java 25, Spring Boot 4, Maven 多模块
- 启动入口：`com.devops00.spectra.launch.LaunchApplication`
- 开发端口：**4004**（通过 `.mise.local.toml` 中的 `SERVER_PORT` 设置）
- 新克隆模板默认使用 `https://127.0.0.1:4004/`；后端与两个前端统一使用本机 HTTPS 和对应证书配置

## mise、构建与运行

`spectra-admin/mise.toml` 固定使用 Temurin JDK 25.0.2；Maven 使用项目自带 wrapper（3.9.12）。正常开发终端已经通过 PowerShell profile 激活 mise：

```powershell
(&mise activate pwsh) | Out-String | Invoke-Expression
```

新终端若未加载 profile，先执行上面的激活命令；之后在本目录直接执行后端流程。默认先打包，再运行 `spectra-launch` 的可执行 JAR：

如果 mise 提示 `.mise.local.toml` 未被信任，在本目录执行一次 `mise trust`；仅信任本机配置，不要把其中的密钥复制到代码或文档中。

```powershell
# 在 spectra-admin/ 下执行
.\mvnw.cmd clean package -DskipTests

# 选择 repackage 生成的 JAR，不要选择 *.jar.original
$jar = Get-ChildItem .\spectra-launch\target\spectra-launch-*.jar -File |
    Where-Object { $_.Name -notlike '*.jar.original' } |
    Select-Object -First 1
java --add-modules ALL-SYSTEM --enable-native-access=ALL-UNNAMED `
    -Dspring.profiles.active=dev `
    -jar $jar.FullName
```

项目 wrapper 已修复 PowerShell 在普通 `.m2` 目录上访问空 `Target[0]` 导致的 `Cannot index into a null array` 问题。正常用户终端不需要额外参数；如果 Codex/CI 沙箱把 Java 的 `user.home` 指向不可写目录，则将 Maven 本地仓库显式指向可写的临时目录：

```powershell
$mavenRepo = Join-Path $env:TEMP 'spectra-maven-repository'
.\mvnw.cmd "-Dmaven.repo.local=$mavenRepo" clean package -DskipTests
```

`spectra-launch/pom.xml` 中的 `spring-boot-maven-plugin` 配置了 `repackage` 和 `LaunchApplication` 主类，因此 Maven `package` 是“编译后启动”的标准流程。产物版本会变化，始终按上面的通配符从 `target/` 选择非 `*.jar.original` 文件。运行前需要 PostgreSQL、Redis 和 `.mise.local.toml` 中的环境变量。

IDEA 显示的 `java ... @<系统临时目录>\idea_arg_file... com.devops00.spectra.launch.LaunchApplication` 是 IDEA 对已编译 classpath 的直接启动命令；参数文件位置属于本机临时路径，不得复制进公共启动说明。需要可重复的终端流程时使用上面的 `package` + JAR 方式。

当前 `LaunchApplication` 不需要额外的 `@Import`。安全 starter 的 `SecurityAutoConfiguration` 会扫描 `LoginExceptionAdvice` 和 `UserOnlineConverter` 所在的技术适配包，业务 Controller 和 Service 由 `spectra-core` 自身扫描。

IDEA 启动命令中的本机 HTTP/HTTPS 代理参数只在访问外部服务时按需添加，不是本地 API 启动的固定参数。

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
- `SERVER_SSL_ENABLED`, `SSL_*` - SSL 配置（模板默认启用 HTTPS，证书使用 `files/ssl/keystore.p12`）
- `S3_*` - 启动时保留完整配置，使用上传功能前连接真实 S3/MinIO
- `AI_KEY`, `AI_BASE_URL`, `AI_MODEL` - 启动时保留完整配置，使用 AI 前连接真实服务
- `RAG_KEY`, `RAG_BASE_URL`, `RAG_MODEL` - 启动时保留完整配置，使用 RAG 前连接真实服务并准备 pgvector

接口加解密不再读取环境变量；开关和密钥由 `spectra_core.sys_config` 中的 `crypto.*` 配置通过管理 API 动态维护，默认关闭。

通知模块开关、运行时 AES 密钥和敏感载荷清理开关在 DEV_OPS 首次登录后的系统设置引导中写入 `spectra_core.sys_config`；数据库、Redis、SSL、MFA 主密钥和外部 Provider 凭据仍需在启动前配置。

需要运行的服务：PostgreSQL, Redis。

## 关键约定

- **UTC 时区** 启动时强制设置（显示层处理本地时区格式化）
- **Spring profiles**：`dev`（本地）、`prod`（Docker/部署）
- **配置文件管理**：所有 `application-*.yml` 集中在 `spectra-config` 模块，通过 `spectra-common` 传递依赖对全项目可见

## 代码风格与命名约定

**编写后端代码时必须遵循 `spectra/spectra-admin-spec` skill**（修改 spectra-admin 代码时自动加载）。

以下为 skill 未覆盖的项目特有约定：

- **Java 25** Temurin 发行版
- **MapStruct 依赖顺序**：mapstruct → lombok → mapstruct-processor（不可重排，否则编译失败）
- **版本属性**：pom.xml 中使用 `${revision}`，由 `flatten-maven-plugin` 展平
- **注释**：使用传统 Javadoc 块注释（`/** ... */`）及 Javadoc/HTML 语法，不使用三斜杠 Markdown 文档注释；每个 Java 文件必须包含 Apache License 2.0 头部
- **方法注释**：接口方法声明以及没有可复用接口契约的公开方法必须有 Javadoc；带 `@Override` 的实现方法不重复复制接口注释；私有辅助方法必须有简短 Javadoc 说明职责，私有构造器不要求重复注释。

### 代码格式化与校验

- 统一格式配置：`config/eclipse-java-formatter.xml`
- Java 缩进：4 个空格，不使用 Tab
- Java 右边距：150 列
- Maven 格式化：`.\mvnw.cmd spotless:apply`
- Maven 校验：`.\mvnw.cmd verify`
- Spotless 使用 Eclipse JDT Formatter，不依赖本机 IDEA 或其他 IDE 的可执行文件；IDEA 侧可导入同一份 Eclipse formatter XML 作为编辑器格式来源
- 当前使用 `origin/master` 作为渐进校验基线；需要全量校验时执行 `.\mvnw.cmd spotless:check "-Dspotless.ratchetFrom=NONE"`。PowerShell 中必须给该系统属性加引号，避免 Maven 将其误解析为生命周期阶段

### 本地代码质量门禁

- `Spotless` 只负责 Java 格式化和 import 整理；`Checkstyle` 负责命名、import 合法性和基础语义约定；`PMD` 负责源码级坏味道、复杂度和异常处理；`SpotBugs` 负责字节码 Bug 模式，FindSecBugs 作为其安全插件；`ArchUnit` 通过 JUnit 测试约束模块分层。
- 修改 Java 后执行 `.\mvnw.cmd spotless:apply`，再执行 `.\mvnw.cmd verify`。`verify` 是统一门禁，必须同时通过 Spotless、Checkstyle、PMD、SpotBugs/FindSecBugs、ArchUnit 和 JUnit。
- 任一检查失败时，阅读完整错误信息，定位源码并修复根因后重跑；不得使用 `skip`、`exclude`、`failOnViolation=false` 或无理由的 suppression 绕过合理问题。仅生成代码或已确认的工具误报可以精确过滤，并在 `config/spotbugs/exclude.xml` 说明原因。
- PMD 的复杂度阈值是现有历史编排代码的迁移基线（方法 30、类 200、NPath 10000），新增代码不得以此扩大复杂度，后续应逐步降低阈值。

## 测试

```powershell
# 运行全部测试
.\mvnw.cmd test

# 运行指定模块的测试
.\mvnw.cmd test -pl spectra-common
```

`spectra-launch` 模块包含全量后端的 ArchUnit 架构测试。

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

- `.mise.local.toml` 在 gitignore 中——新克隆必须从 example 创建；根工作区同名文件只是维护者可选覆盖，不是前置条件
- 原生 JVM 参数：`--add-modules ALL-SYSTEM --enable-native-access=ALL-UNNAMED`
- PostgreSQL 默认端口 5432，Redis 默认端口 6379
- 此处 API 变更会直接影响 `spectra-ui` 和 `spectra-app`——修改端点时需与前端协调
