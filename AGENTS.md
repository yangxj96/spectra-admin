# spectra-admin Agent 指令

## 环境与边界

- 使用 mise 管理的 Java 25 和本项目的 `.\mvnw.cmd`；不要依赖全局 Maven。
- 命令在本目录执行；`-pl spectra-launch -am` 使用 Maven reactor，无需预先 `install`。
- 应用启动时使用 UTC；PostgreSQL、Redis、S3 等外部依赖按任务需要验证，不自动启动。

## 开发与验证

- 开发中优先目标模块的编译或测试；模块完成时执行格式检查和 `verify`，交付前按需执行全项目门禁。完整命令见根目录常见命令文档。
- 本地启动使用 `dev` profile；`-Dmaven.test.skip=true` 会跳过测试资源处理、测试编译和测试执行，运行单元测试时必须省略。
- `verify` 会执行编译、打包及 Spotless、Checkstyle、PMD、SpotBugs、Enforcer 检查；加上测试跳过参数时不执行单元测试。Spotless 检查只验证格式。
- 直接调用 `spring-boot:run` 不触发生命周期绑定的 Enforcer 或质量检查；其工作目录由 `spectra-launch/pom.xml` 指向项目根目录，以解析 `files/` 资源。
- Java 25 下 Mockito inline 若出现 Byte Buddy self-attach 错误，检查 JDK 动态 agent 权限；必要时临时指定本机 Byte Buddy agent，勿将本机绝对路径写入仓库。

## 数据库迁移

- `spectra-launch/src/main/resources/db/migration/` 中 V1 为初始化基线，后续变更使用递增 migration；不要改写已发布的迁移。
- 不得用 `baseline-on-migrate`、`repair`、删除 `flyway_schema_history` 记录或忽略缺失 migration 掩盖版本漂移。需要保留数据时，先设计并审查一次性迁移。

## 实现约束

- 工程主标准见 `../docs/开发指南/04-工作区工程标准.md`；领域细则或 Skill 与主标准冲突时按已确认标准处理，实现状态另见治理台账。
- Launch 统一装配业务模块，模块依赖必要的 Framework 技术能力和 Common 公共契约；Common 不反向承载 ORM、Web 或数据库实现。
- 平台能力保留在 Core，仅新增业务功能使用独立模块。应用 Service 使用接口 + Impl，内部协作者按职责组织；编排服务不机械继承实体 CRUD。
- Core 跨域调用公开 Service 或必要 Facade 的业务操作，不操作对方 Mapper、Impl 或继承的通用 CRUD；跨模块不引用对方内部 Entity、Mapper 或实现类。
- 公共变更先分析全工作区直接与间接影响，受影响的暂缓 OA/Workflow 也需一致性改造及验证；不能只验证当前模块编译。

## 领域文档路由

- 领域文档按根目录 `AGENTS.md` 路由；后端规范见 `docs/后端/30-规范/`，Docker 运维见 `docs/部署运维/`。
