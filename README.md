# 光谱后台管理系统（Spectra Admin System）

> 一个基于 Spring Boot 3 + Vue 3 的现代化前后端分离通用框架

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5.17-green)
![MySQL](https://img.shields.io/badge/MySQL-8.4.4-blue)
![License](https://img.shields.io/github/license/yangxj96/spectra-api)

---

## 🚀 为什么做这个项目？

在多年的后端开发实践中，微服务等高阶架构并非每个项目都能用上。  
而“**Spring Boot + Vue**”的单体前后端分离架构，依然是中小型项目和快速开发场景下的主流选择。

为了**减少重复造轮子、统一开发规范、提升团队效率**，我着手打造了一个开箱即用、结构清晰、技术栈现代化的通用后台管理系统框架 —— **Spectra**。

它不仅包含权限管理、通用 CRUD、代码生成等基础能力，还集成了当前主流的技术组件，力求做到**易用、可扩展、可持续维护**。

---

## 🛠 技术选型

### 后端技术栈

| 技术           | 版本     | 说明             |
|--------------|--------|----------------|
| Java         | JDK 21 | 使用最新LTS版本，性能更强 |
| Maven        | 3.9.9  | 构建工具           |
| Spring Boot  | 3.5.4  | 核心框架           |
| MyBatis-Plus | 3.5.12 | 增强ORM，简化CRUD   |
| Sa-Token     | 1.44.0 | 轻量级Java权限认证框架  |
| MapStruct    | 1.6.3  | 实体映射工具，提升性能    |
| MySQL        | 8.4.4  | 主流开源关系型数据库     |
| Jasypt       | 3.0.5  | 配置加密，保障敏感信息安全  |

> ✅ 所有依赖均采用当前稳定最新版，并将持续跟进生态更新。

---

### 前端技术栈

| 包名           | 版本      | 用途                     |
|--------------|---------|------------------------|
| Vue          | 3.5.17  | 渐进式前端框架                |
| Vue Router   | 4.5.1   | 路由管理                   |
| Pinia        | 3.0.3   | 状态管理（Vuex替代）           |
| @vueuse/core | 13.5.0  | 实用 Composition API 工具库 |
| Vite         | 7.0.2   | 构建工具，极速启动              |
| Element Plus | 2.10.3  | UI 组件库                 |
| Axios        | 1.10.0  | HTTP 请求客户端             |
| ECharts      | 5.6.0   | 数据可视化图表                |
| Lodash       | 4.17.21 | 工具函数库，简化数据操作           |
| ESLint       | 9.30.1  | 代码质量检查                 |
| Prettier     | 3.6.2   | 统一代码格式风格               |

---

## ⚙️ 开发环境配置建议

### IDEA JVM 参数调优（降低内存占用）

```bash
-Xms256m -Xmx256m -Xmn100m
```

> 💡 适用于本地开发环境，有效减少服务启动内存消耗，提高多服务并行运行效率。

---

## 🔐 环境变量配置说明

### 后端 `.env` 文件（位于 `spectra-api` 目录）

> ⚠️ 该文件不会提交至 Git，请自行创建。

| 环境变量                        | 说明            | 示例                                 |
|-----------------------------|---------------|------------------------------------|
| `JASYPT_ENCRYPTOR_PASSWORD` | Jasypt 加密解密密钥 | `JASYPT_ENCRYPTOR_PASSWORD=123456` |

> 用于数据库密码等敏感配置的加密保护。

---

### 前端 `.env` 文件（位于 `spectra-ui` 目录）

| 环境变量           | 说明                  | 示例                                    |
|----------------|---------------------|---------------------------------------|
| `VITE_API_URL` | 后端API基础地址（末尾需带 `/`） | `VITE_API_URL=http://localhost:8888/` |

> 支持 `.env.development`、`.env.production` 多环境配置。

---

## 🧩 项目模块结构

| 模块                  | 路径                                     | 说明                               |
|---------------------|----------------------------------------|----------------------------------|
| `spectra-common`    | [spectra-common](spectra-common)       | 通用工具类、注解、常量、DTO等共享内容             |
| `spectra-core`      | [spectra-core](spectra-core)           | 核心接口定义、领域模型、服务契约                 |
| `spectra-framework` | [spectra-framework](spectra-framework) | 框架级配置（如权限、日志、异常处理、拦截器等）          |
| `spectra-ui`        | [spectra-ui](spectra-ui)               | 前端 Vue 项目，基于 Vite + Element Plus |
| `spectra-launch`    | [spectra-launch](spectra-launch)       | 启动模块 & 业务入口，用户可在此编写具体业务逻辑        |

> 📌 推荐使用方式：`spectra-launch` 作为你的“业务模块”，可自由扩展控制器、服务、Mapper 等。

---

## 📦 快速开始

1. 克隆项目：
   ```bash
   git clone https://github.com/yangxj96/spectra-api.git
   ```

2. 创建 `.env` 文件并配置环境变量（前后端分别配置）

3. 启动后端服务（确保 PostgreSQL 已运行）：
   ```bash
   cd spectra-launch
   mvn spring-boot:run
   ```

4. 启动前端（进入前端项目目录）：
   ```bash
   cd spectra-ui
   npm install
   npm run start
   ```

5. 浏览器访问：`http://localhost:5173`

---

## 🌟 特性亮点

- ✅ 基于 Spring Boot 3 + Java 21，响应式编程支持
- ✅ 前后端完全分离，Vite 提供极速 HMR
- ✅ Sa-Token 实现 RBAC 权限模型（用户、角色、菜单、按钮权限）
- ✅ MyBatis-Plus + MapStruct 提升开发效率
- ✅ 敏感配置加密（Jasypt）
- ✅ 标准 RESTful API 设计
- ✅ 可扩展的模块化架构，便于二次开发

---

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

### ✅ 后续规划（Roadmap）

- [ ] 集成代码生成器
- [ ] 添加多租户支持
- [ ] 引入 Redis 缓存示例
- [ ] 提供 Docker 部署脚本
- [ ] 完善文档与示例页面
