# 光谱后台管理系统（Spectra Admin System）

> 一个基于 Spring Boot 4 + Vue 3 的现代化前后端分离通用框架

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.4-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5.28-green)
![PostgreSQL18](https://img.shields.io/badge/PostgreSQL-18.2-blue)
![License](https://img.shields.io/github/license/yangxj96/spectra-admin)

---

## 🚀 为什么做这个项目？

在多年的后端开发实践中，微服务等高阶架构并非每个项目都能用上。  
而“**Spring Boot + Vue**”的单体前后端分离架构，依然是中小型项目和快速开发场景下的主流选择。

为了**减少重复造轮子、统一开发规范、提升团队效率**，我着手打造了一个开箱即用、结构清晰、技术栈现代化的通用后台管理系统框架 —— **Spectra**。

它不仅包含权限管理、通用 CRUD等基础能力，还集成了当前主流的技术组件，力求做到**易用、可扩展、可持续维护**。

---

## 🛠 技术选型

### 后端技术栈

| 技术              | 版本     | 说明             |
|-----------------|--------|----------------|
| Java            | JDK25  | 使用最新LTS版本，性能更强 |
| Maven           | 3.9.12 | 构建工具           |
| Spring Boot     | 4.0.4  | 核心框架           |
| Spring Security | 7.0.4  | 权限认证框架         |
| MyBatis-Plus    | 3.5.15 | 增强ORM，简化CRUD   |
| MapStruct       | 1.6.3  | 实体映射工具，提升性能    |
| PostgreSQL      | 18.2   | 知名关系型数据库       |

> ✅ 所有依赖均采用当前稳定最新版，并将持续跟进生态更新。

---

### 前端技术栈

| 包名           | 版本      | 用途                     |
|--------------|---------|------------------------|
| Vue          | 3.5.28  | 渐进式前端框架                |
| Vue Router   | 5.0.2   | 路由管理                   |
| Pinia        | 3.0.4   | 状态管理（Vuex替代）           |
| @vueuse/core | 14.2.0  | 实用 Composition API 工具库 |
| Vite         | 7.3.1   | 构建工具，极速启动              |
| Element Plus | 2.13.1  | UI 组件库                 |
| Axios        | 1.13.5  | HTTP 请求客户端             |
| ECharts      | 6.0.0   | 数据可视化图表                |
| vue-echarts  | 8.0.1   | 转为vue封装的echarts操作组件    |
| Lodash       | 4.17.23 | 工具函数库，简化数据操作           |
| Eslint       | 9.39.2  | 代码质量检查                 |
| Prettier     | 3.8.1   | 统一代码格式风格               |

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
⚠️ 项目使用mise管理环境.
⚠️ 可重命名`.mise.local.toml.example`文件为`.mise.local.toml`,并把值设置为本地相关的值
```

### 后端 `.mise.local.toml` 文件（位于 [spectra-admin](spectra-admin) 目录下）

| 环境变量               | 说明                | 示例                                                   |
|--------------------|-------------------|------------------------------------------------------|
| `DB_URL`           | 数据库URL            | `DB_URL=jdbc:postgresql://127.0.0.1:5237/spectra_db` |
| `DB_USERNAME`      | 数据库用户名            | `DB_USERNAME=XXX`                                    |
| `DB_PASSWORD`      | 数据库密码             | `DB_PASSWORD=XXX`                                    |
| `DEFAULT_PASSWORD` | 新增用户,重置用户密码的默认密码  | `DEFAULT_PASSWORD=admin123`                          |
| `LICENSE_PASSWORD` | 许可模式,使用的密钥密码(临时用) | `LICENSE_PASSWORD=XXX`                               |
| `SSL_PASSWORD`     | SSL的密码            | `SSL_PASSWORD=xxx`                                   |
| `SSL_TYPE`         | SSL的类型            | `SSL_TYPE=PKCS12`                                    |
| `SSL_ALIAS`        | SSL的别名            | `SSL_ALIAS=xxx`                                      |

> 用于数据库密码等敏感配置的加密保护。

1. SSL 开头的几个配置根据实际需要进行配置即可
2. LICENSE 开头的如果没用到 LICENSE 模块则无效

---

### 前端 `.env` 文件（位于 [spectra-ui](spectra-ui) 目录）

| 环境变量             | 说明                  | 示例                                     |
|------------------|---------------------|----------------------------------------|
| `VITE_API_URL`   | 后端API基础地址（末尾需带 `/`） | `VITE_API_URL=https://localhost:8888/` |
| `VITE_WEB_TITLE` | 网站名称                | `光谱平台`                                 |

> 支持 `.env.development`、`.env.production` 多环境配置。

---

## 🧩 项目模块结构

| 模块                 | 路径                                                 | 说明                               |
|--------------------|----------------------------------------------------|----------------------------------|
| `spectra-common`   | [spectra-common](spectra-admin/spectra-common)     | 通用工具类、注解、常量、DTO等共享内容             |
| `spectra-core`     | [spectra-core](spectra-admin/spectra-modules/spectra-core)        | 核心接口定义、领域模型、服务契约                 |
| `spectra-workflow` | [spectra-workflow](spectra-admin/spectra-modules/spectra-workflow) | 工作流模块,选用的flowable流程框架            |
| `spectra-launch`   | [spectra-launch](spectra-admin/spectra-launch)     | 启动模块 & 业务入口，用户可在此编写具体业务逻辑        |
| `spectra-ui`       | [spectra-ui](spectra-ui)                           | 前端 Vue 项目，基于 Vite + Element Plus |

> 📌 推荐使用方式：`spectra-launch` 作为你的“业务模块”，可自由扩展控制器、服务、Mapper 等。

---

## 🌟 特性亮点

- ✅ 基于 Spring Boot 4 + Java 25，响应式编程支持
- ✅ 前后端完全分离，Vite 提供极速 HMR
- ✅ SpringSecurity 实现 RBAC 权限模型（用户、角色、菜单、按钮权限）
- ✅ MyBatis-Plus + MapStruct 提升开发效率
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
