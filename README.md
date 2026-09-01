# Spectra Admin

> Spectra 的后端 API 服务，同时为 `spectra-ui` 和 `spectra-app` 提供接口。基于 Java 25、Spring Boot 4.1、PostgreSQL、Redis 和 Maven 多模块构建。

## 模块

| 模块 | 职责 |
|---|---|
| `spectra-config` | 集中管理 `application-*.yml` 配置 |
| `spectra-common` | DTO、Entity 基类、公共工具和共享依赖 |
| `spectra-framework` | MVC、JSON、MyBatis-Plus、Redis、缓存和接口加解密 |
| `spectra-starter` | Security 和日志自动配置 Starter |
| `spectra-modules/spectra-core` | 用户、账号、角色权限、组织、菜单、字典、区域、配置、日志和消息中心 |
| `spectra-modules/spectra-upload` | 本地/S3 文件存储和分片上传 |
| `spectra-modules/spectra-workflow` | Flowable 流程定义、实例、任务、表单和审批能力 |
| `spectra-modules/spectra-oa` | 通用申请、请假、公告、日程、会议、文档、合同、报销、采购、资产、用品和报表 |
| `spectra-launch` | Spring Boot 启动入口和可执行 JAR 打包 |

模块的详细设计、接口和数据模型统一维护在 Spectra 根工作区的 `docs/`，不在各 Maven 子模块重复维护 README。

## 环境要求

| 工具 | 版本/要求 |
|---|---|
| Java | Temurin 25.0.2 |
| Maven | 3.9.12，使用项目自带 Wrapper |
| PostgreSQL | 18 |
| Redis | 本地开发必需 |

复制 `.mise.local.toml.example` 为 `.mise.local.toml`。模板默认使用 HTTPS 4004，首次启动前需要准备 `files/ssl/keystore.p12` 并填写对应密码；数据库和 Redis 必须改成真实可连接的本机值，S3 的占位地址只保证配置完整，使用文件存储功能前必须接入真实 Provider。该本机文件可能包含凭据，不得提交。

```bash
cp .mise.local.toml.example .mise.local.toml
```

数据库初始化、前端联调和 HTTPS 可选配置见根工作区 `docs/50-开发指南/10-环境搭建.md`。

## 构建与校验

以下命令从 `spectra-admin/` 执行；当前 Ubuntu/WSL 统一使用项目自带的 `./mvnw`。

```bash
# 渐进格式检查/格式化
./mvnw spotless:check
./mvnw spotless:apply

# 全量格式检查
./mvnw spotless:check '-Dspotless.ratchetFrom=NONE'

# 测试与完整打包
./mvnw test
./mvnw clean package -DskipTests
```

## 启动

先完成 Maven 打包，再运行 `spectra-launch` 生成的 Spring Boot 可执行 JAR：

```bash
jar=$(find spectra-launch/target -maxdepth 1 -type f \
    -name 'spectra-launch-*.jar' ! -name '*.jar.original' -print -quit)
test -n "$jar"
mise exec -- java --add-modules ALL-SYSTEM --enable-native-access=ALL-UNNAMED \
    -Dspring.profiles.active=dev \
    -jar "$jar"
```

默认开发端口为 `4004`，API 上下文为 `/api`。示例配置首次启动地址是 `https://127.0.0.1:4004/api`；模板默认启用 HTTPS，启动前需准备 `files/ssl/keystore.p12` 并填写 `SSL_PASSWORD`。启动前需确保 PostgreSQL、Redis 和 `.mise.local.toml` 中的环境变量可用。

## 文档入口

在 Spectra 根工作区中查看：

| 内容 | 路径 |
|---|---|
| 后端架构 | `docs/10-后端/10-架构分层.md` |
| 用户与权限 | `docs/10-后端/20-用户与权限.md` |
| 系统管理 | `docs/10-后端/30-系统管理.md` |
| OA 模块 | `docs/10-后端/40-OA模块.md` |
| 文件上传 | `docs/10-后端/50-文件上传.md` |
| 工作流 | `docs/10-后端/60-工作流.md` |
| 基础设施 | `docs/10-后端/80-基础设施.md` |
| API 总览 | `docs/10-后端/90-API总览.md` |
| 数据模型 | `docs/30-数据模型/` |
| 数据库迁移 | `spectra-config/src/main/resources/db/migration/` |
| 环境与命令 | `docs/50-开发指南/` |

在线文档：[https://www.devops00.com/spectra-admin/](https://www.devops00.com/spectra-admin/)

## 许可证

Apache-2.0
