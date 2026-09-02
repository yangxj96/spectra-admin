# spectra-admin Agent 指令

## 运行时与安全

- 使用 mise 管理的 Java 25 和本项目的 `./mvnw`；不要依赖全局 Maven。
- `.mise.local.toml` 只保存本机配置和凭据，不读取后输出，不复制到代码、文档或日志。
- PostgreSQL、Redis、S3 等外部依赖按当前任务需要验证，不因普通检查自动启动服务。
- 安全 Redis 是安全事实源；连接、命令或脚本状态无法确认时必须 fail-closed，不得降级为“Token/Challenge 不存在”。
- 启动时使用 UTC；API 契约版本为 `1.0.0`。

## 本地开发启动流程

- 命令均在本目录（`spectra-admin/`）根目录执行；`-pl spectra-launch -am` 使用 Maven reactor 内的模块输出，不需要为日常启动预先 `install`。
- 启动后端统一使用：

  ```bash
  mise exec -- ./mvnw -pl spectra-launch -am spring-boot:run \
      -Dspring-boot.run.profiles=dev \
      -Dmaven.test.skip=true
  ```

  `-Dmaven.test.skip=true` 会跳过测试资源处理、测试编译和测试执行；通过 mise 加载 `.mise.local.toml` 环境，但不得读取或输出该文件中的凭据。
- 执行单元测试使用：

  ```bash
  mise exec -- ./mvnw -pl spectra-launch -am test
  ```

  单元测试命令不得携带 `-Dmaven.test.skip=true`。如果 Java 25 下 Mockito inline 报 Byte Buddy self-attach 错误，先确认本机 JDK 的动态 agent 权限，再按本机 Maven 仓库中实际的 Byte Buddy agent 路径临时传入 `-DargLine=-javaagent:<path>`；不要把本机绝对路径写入仓库。
- 阶段性完成后执行质量门禁：

  ```bash
  mise exec -- ./mvnw -pl spectra-launch -am verify \
      -Dmaven.test.skip=true
  ```

  该命令执行编译、打包、Spotless、Checkstyle、PMD、SpotBugs 和 Enforcer，但不执行单元测试；Spotless 的 `check` 只验证格式，不自动修改文件。
- `spring-boot:run` 不进入 `verify` 阶段，因此日常启动不会执行 Spotless、Checkstyle、PMD 和 SpotBugs；Enforcer 位于 `validate` 阶段，仍然执行。
- `spring-boot:run` 的工作目录由 `spectra-launch/pom.xml` 指向 `spectra-admin/` 根目录，以便正确解析 `files/` 下的本地开发资源。

## 本地 Flyway 状态

- 当前数据库结构来源是 `spectra-config/src/main/resources/db/migration/` 的 V1 基线及后续递增 migration；不得通过打开 `baseline-on-migrate`、`repair`、删除 `flyway_schema_history` 记录或忽略缺失 migration 来掩盖版本漂移。
- 如果开发库提示数据库版本高于仓库最新 migration，先确认 `DB_URL` 指向可丢弃的开发库；当前仓库基线重整后，执行过旧 V1～V34 或其他历史链的开发库应重建，再由当前 migration 链初始化。需要保留数据的环境必须先设计并审查一次性结构/数据迁移。

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
