# Spectra Admin

> Spectra 全栈系统的后端 API 服务，基于 Spring Boot 4 + Java 25。

## 技术栈

| 技术 | 版本 | 说明 |
|---|---|---|
| Java | 25 | Temurin LTS |
| Spring Boot | 4.1.0 | 核心框架 |
| Spring Security | 7.1.0 | 权限认证 |
| MyBatis-Plus | 3.5.15 | ORM |
| MapStruct | 1.6.3 | 实体映射 |
| PostgreSQL | 18 | 数据库 |
| Redis + JetCache | 2.7.8 | 缓存 |
| LangChain4j | 1.16.3 | AI 集成 |
| Flowable | — | 工作流引擎 |

## 模块结构

```
spectra-admin/
├── spectra-config/      ← 统一配置文件（application-*.yml）
├── spectra-common/      ← DTO/Entity 基类/工具类
├── spectra-framework/   ← MVC/JSON/缓存/Redis/MyBatis 配置
├── spectra-modules/     ← 业务模块
│   ├── spectra-core/    ← 用户/角色/权限/菜单/部门/字典/区域/日志
│   ├── spectra-oa/      ← OA 办公（资产/考勤/日历/通讯录/合同/文档/会议/公告/报表）
│   ├── spectra-upload/  ← 文件上传（本地 + S3，分片上传）
│   ├── spectra-workflow/← 工作流（Flowable 流程引擎）
│   └── spectra-ai/      ← AI 集成（LangChain4j + RAG）
├── spectra-starter/     ← 自动配置 Starter（Security/Log/AI）
└── spectra-launch/      ← 启动入口（Spring Boot Application）
```

## 常用命令

```bash
# 构建
./mvnw clean package -DskipTests

# 启动（默认端口 4004）
./mvnw spring-boot:run -pl spectra-launch
```

## 配置

通过 `.mise.local.toml` 管理环境变量，复制 `.mise.local.toml.example` 为 `.mise.local.toml` 后填入：

| 变量 | 说明 |
|---|---|
| DB_URL / DB_USERNAME / DB_PASSWORD | PostgreSQL 连接 |
| REDIS_HOST / REDIS_PORT | Redis 连接 |
| S3_* | 对象存储配置 |
| AI_* / RAG_* | AI 模型配置 |
| SSL_* | 证书配置 |

## 文档

项目文档统一维护在根仓库 [spectra-docs](https://github.com/yangxj96/spectra-docs)：

| 文档 | 路径 |
|---|---|
| 架构分层 | `docs/10-后端/10-架构分层.md` |
| 用户与权限 | `docs/10-后端/20-用户与权限.md` |
| 系统管理 | `docs/10-后端/30-系统管理.md` |
| OA 模块 | `docs/10-后端/40-OA模块.md` |
| 工作流 | `docs/10-后端/60-工作流.md` |
| AI 模块 | `docs/10-后端/70-AI模块.md` |
| 基础设施 | `docs/10-后端/80-基础设施.md` |
| API 总览 | `docs/10-后端/90-API总览.md` |
| 建表 SQL | `docs/20-知识库/30-数据模型/` |
| 初始化数据 | `docs/20-知识库/30-数据模型/33-初始化数据.sql` |
| 部署运维 | `docs/20-知识库/60-部署运维/` |

## 许可证

Apache-2.0
