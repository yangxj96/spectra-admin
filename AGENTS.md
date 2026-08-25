# spectra-admin Agent 指令

## 运行时与安全

- 使用 Java 25 和本项目的 `mvnw.cmd`；不要依赖全局 Maven。
- `.mise.local.toml` 只保存本机配置和凭据，不读取后输出，不复制到代码、文档或日志。
- PostgreSQL、Redis、S3、AI 等外部依赖按当前任务需要验证，不因普通检查自动启动服务。
- 安全 Redis 是安全事实源；连接、命令或脚本状态无法确认时必须 fail-closed，不得降级为“Token/Challenge 不存在”。
- 启动时使用 UTC；API 契约版本为 `1.0.0`。

## 实现约束

- 修改或审查 Java 代码时使用 `$spectra-admin-spec`。
- 保持 `launch → modules/starter → framework → common → config` 的分层关系。
- 业务模块位于 `spectra-modules/`；跨模块调用优先通过明确的 Facade、Port 或事件，不引用对方内部 Entity、Mapper 或实现类。
- 详细后端规范和示例由 Skill 及其按需 reference 提供，不在本文件复制。

## 领域文档路由

- 用户、角色、权限：`docs/10-后端/20-用户与权限.md`
- 部门、菜单、字典、日志：`docs/10-后端/30-系统管理.md`
- OA：`docs/10-后端/40-OA模块.md`
- 文件上传：`docs/10-后端/50-文件上传.md`
- 工作流：`docs/10-后端/60-工作流.md`
- AI：`docs/10-后端/70-AI模块.md`
- 基础设施：`docs/10-后端/80-基础设施.md`
- API：`docs/10-后端/90-API总览.md`
- 数据模型：`docs/30-数据模型/` 中与目标实体对应的笔记

只读取当前任务涉及的领域；架构、新模块或跨模块任务再读取项目总览和架构分层。

## 验证

- 开发中优先执行目标模块的 `compile` 或 `test`。
- 模块完成时执行目标模块的格式化和 `verify`。
- 交付或提交前按需执行全项目 `spotless` 和 `verify`。
- 详细命令和环境排障见 `docs/50-开发指南/20-常见命令.md`，Docker 说明见 `docs/60-部署运维/`。
- 新增或修改 Entity、Controller、配置、SQL 后，按根仓库文档同步规则更新知识库。
