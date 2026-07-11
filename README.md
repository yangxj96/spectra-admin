# 光谱后台管理系统（Spectra Admin System）

> 基于 Spring Boot 4 的现代化通用后端框架

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen)
![PostgreSQL18](https://img.shields.io/badge/PostgreSQL-18.2-blue)
![License](https://img.shields.io/github/license/yangxj96/spectra-admin)

Spectra 系统由三个项目组成，本仓库为后端 API 服务：

| 项目 | 说明 |
|---|---|
| **spectra-admin**（本仓库） | 后端 API（Spring Boot 4 + Java 25） |
| [spectra-ui](https://github.com/yangxj96/spectra-ui) | Web 管理端（Vue 3 + Element Plus） |
| [spectra-app](https://github.com/yangxj96/spectra-app) | 移动端（uni-app，H5 / 微信小程序） |

两个前端均连接本服务作为 API 后端（开发环境默认端口 `4004`）。

---

## 🚀 为什么做这个项目？

在多年的后端开发实践中，微服务等高阶架构并非每个项目都能用上。  
而后端框架的标准化和复用，对于提升团队效率至关重要。

为了**减少重复造轮子、统一开发规范、提升团队效率**，我着手打造了一个开箱即用、结构清晰、技术栈现代化的通用后台管理系统框架 —— **Spectra**。

它不仅包含权限管理、通用 CRUD 等基础能力，还集成了当前主流的技术组件，力求做到**易用、可扩展、可持续维护**。

---

## 🛠 技术选型

### 后端技术栈

| 技术              | 版本     | 说明             |
|-----------------|--------|----------------|
| Java            | JDK 25  | 使用最新 LTS 版本，性能更强 |
| Maven           | 3.9.12 | 构建工具           |
| Spring Boot     | 4.1.0  | 核心框架           |
| Spring Security | 7.1.0  | 权限认证框架         |
| MyBatis-Plus    | 3.5.15 | 增强 ORM，简化 CRUD   |
| MapStruct       | 1.6.3  | 实体映射工具，提升性能    |
| PostgreSQL      | 18.2   | 知名关系型数据库       |

> ✅ 所有依赖均采用当前稳定最新版，并将持续跟进生态更新。

---

## ⚙️ 开发环境配置建议

### IDEA JVM 参数调优（降低内存占用）

```bash
-Xms256m -Xmx256m -Xmn100m
```

> 💡 适用于本地开发环境，有效减少服务启动内存消耗，提高多服务并行运行效率。

---

## 🔐 环境变量配置说明

```text
⚠️ 该文件不会提交至 Git，请自行创建。
⚠️ 项目使用 mise 管理环境。
⚠️ 可重命名 `.mise.local.toml.example` 文件为 `.mise.local.toml`，并把值设置为本地相关的值
```

### 后端 `.mise.local.toml` 文件（位于 [spectra-admin](spectra-admin) 目录下）

| 环境变量               | 说明               | 示例                                                            |
|--------------------|------------------|---------------------------------------------------------------|
| `DB_URL`           | 数据库URL           | `DB_URL=jdbc:postgresql://127.0.0.1:5432/devops00_spectra_db` |
| `DB_USERNAME`      | 数据库用户名           | `DB_USERNAME=XXX`                                             |
| `DB_PASSWORD`      | 数据库密码            | `DB_PASSWORD=XXX`                                             |
| `DEFAULT_PASSWORD` | 新增用户，重置用户密码的默认密码 | `DEFAULT_PASSWORD=admin123`                                   |
| `SERVER_PORT`      | 服务端口              | `SERVER_PORT=8080`                                            |
| `REDIS_HOST`       | Redis地址           | `REDIS_HOST=127.0.0.1`                                        |
| `REDIS_PORT`       | Redis端口           | `REDIS_PORT=6379`                                             |
| `REDIS_DB`         | Redis数据库          | `REDIS_DB=0`                                                  |
| `REDIS_PASSWORD`   | Redis密码           | `REDIS_PASSWORD=XXX`                                          |
| `SSL_PASSWORD`     | SSL的密码           | `SSL_PASSWORD=xxx`                                            |
| `SSL_TYPE`         | SSL的类型           | `SSL_TYPE=PKCS12`                                             |
| `SSL_ALIAS`        | SSL的别名           | `SSL_ALIAS=tomcat`                                            |
| `S3_ENDPOINT`      | S3 端点            | `S3_ENDPOINT=https://xxx`                                     |
| `S3_ACCESS_KEY`    | S3 访问密钥          | `S3_ACCESS_KEY=XXX`                                           |
| `S3_SECRET_KEY`    | S3 秘密密钥          | `S3_SECRET_KEY=XXX`                                           |
| `S3_BUCKET`        | S3 存储桶           | `S3_BUCKET=xxx`                                               |
| `S3_REGION`        | S3 区域            | `S3_REGION=xxx`                                               |
| `AI_KEY`           | AI API 密钥        | `AI_KEY=XXX`                                                  |
| `AI_BASE_URL`      | AI API 地址        | `AI_BASE_URL=https://xxx`                                     |
| `AI_MODEL`         | AI 模型名           | `AI_MODEL=xxx`                                                |
| `RAG_KEY`          | RAG API 密钥       | `RAG_KEY=XXX`                                                 |
| `RAG_BASE_URL`     | RAG API 地址       | `RAG_BASE_URL=https://xxx`                                    |
| `RAG_MODEL`        | RAG 模型名          | `RAG_MODEL=xxx`                                               |
| `SPECTRA_SYSTEM_SM_ENABLED` | 是否启用接口加解密（默认false） | `SPECTRA_SYSTEM_SM_ENABLED=true` |
| `SPECTRA_SYSTEM_SM_PUBLIC_KEY` | RSA公钥 (Base64) | `SPECTRA_SYSTEM_SM_PUBLIC_KEY=xxx` |
| `SPECTRA_SYSTEM_SM_PRIVATE_KEY` | RSA私钥 (Base64) | `SPECTRA_SYSTEM_SM_PRIVATE_KEY=xxx` |

> 用于数据库密码等敏感配置的加密保护。

> Redis、S3、AI、RAG 相关配置可根据实际需要进行配置。

---

## 🧩 项目模块结构

| 模块                 | 路径                                                                 | 说明                        |
|--------------------|--------------------------------------------------------------------|---------------------------|
| `spectra-config`   | [spectra-config](spectra-admin/spectra-config)                       | 统一配置文件，所有 application-*.yml 集中管理 |
| `spectra-common`   | [spectra-common](spectra-admin/spectra-common)                     | 通用工具类、注解、常量、DTO等共享内容      |
| `spectra-framework`| [spectra-framework](spectra-admin/spectra-framework)               | 平台配置、Redis、AOP、缓存      |
| `spectra-core`     | [spectra-core](spectra-admin/spectra-modules/spectra-core)         | 核心接口定义、领域模型、服务契约          |
| `spectra-upload`   | [spectra-upload](spectra-admin/spectra-modules/spectra-upload)     | 文件上传模块（S3）               |
| `spectra-workflow` | [spectra-workflow](spectra-admin/spectra-modules/spectra-workflow) | 工作流模块，选用的 Flowable 流程框架     |
| `spectra-oa`       | [spectra-oa](spectra-admin/spectra-modules/spectra-oa)             | OA 模块                     |
| `spectra-ai`       | [spectra-ai](spectra-admin/spectra-modules/spectra-ai)             | AI 集成模块（LangChain4j）       |
| `spectra-launch`   | [spectra-launch](spectra-admin/spectra-launch)                     | 启动模块 & 业务入口，用户可在此编写具体业务逻辑 |

> 📌 推荐使用方式：`spectra-launch` 作为你的“业务模块”，可自由扩展控制器、服务、Mapper 等。

---

## 🌟 特性亮点

- ✅ 基于 Spring Boot 4 + Java 25
- ✅ SpringSecurity 实现 RBAC 权限模型（用户、角色、菜单、按钮权限）
- ✅ MyBatis-Plus + MapStruct 提升开发效率
- ✅ 集成 Flowable 工作流引擎
- ✅ 集成 LangChain4j AI 能力
- ✅ 支持 S3 文件存储
- ✅ 前后端接口数据混合加密（AES-256-GCM + RSA-2048-OAEP）
- ✅ 标准 RESTful API 设计
- ✅ 可扩展的模块化架构，便于二次开发

---

## ⚠️ Docker 部署说明（验证码 & 字体相关）

本项目的 **验证码功能依赖 Java AWT（`java.desktop` 模块）进行字体注册与图像绘制**。
在使用 Spring Boot 官方 `build-image` 构建 Docker 镜像时，需要注意运行环境差异：

#### 问题说明

* Spring Boot `build-image` **默认使用精简（headless）JRE**
* 该运行环境 **不包含 `java.desktop` 模块及字体渲染依赖**
* 会导致以下异常（示例）：

```text
java.io.IOException: Problem reading font data
```

#### 解决方案（推荐）

请使用 **完整 JDK 的 buildpack** 构建镜像，例如：

```xml

<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <builder>paketobuildpacks/builder-jammy-full</builder>
        </image>
    </configuration>
</plugin>
```

该 builder 提供：

* 完整的 `java.desktop` 模块
* 字体渲染所需的系统依赖（fontconfig / freetype）
* 支持 AWT 字体注册与验证码正常生成

#### 说明

* 本地开发环境（完整 JDK）通常不会出现该问题
* 若后续切换为精简镜像或无 GUI 运行环境，请确保验证码功能已做兼容处理

----

## 🤝 贡献与反馈

欢迎提交 Issue 或 Pull Request！  
如果你在使用中遇到问题，或有功能建议，都可以在 GitHub 提出。

> 🙌 持续迭代中，欢迎 Star ⭐ 支持！

---

## 📄 许可证

本项目基于 [Apache-2.0](LICENSE) 开源，可免费用于个人或商业项目。

---

> **Spectra** —— 简洁有力，照亮你的开发之路 🌈

---
