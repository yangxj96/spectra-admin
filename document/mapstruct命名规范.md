MapStruct 命名规范（官方推荐 + 团队约定最终版）

适用于 MyBatis + mapstruct-spring-annotations

---

一、适用范围与目标

本规范适用于以下技术栈：

* 对象映射框架：MapStruct
* Spring 环境（mapstruct-spring-annotations）
* 持久层：MyBatis / MyBatis-Plus
* 转换对象类型：Entity / DO / PO / DTO / VO / BO / Cmd / Query

制定目标：

1. 与 MapStruct 官方设计思想保持一致
2. 通过“命名约定”最大化自动映射能力
3. 避免歧义，减少 @Mapping、@Named、qualifiedBy 使用
4. 提升 Converter 的可读性、可维护性和可扩展性

本规范重点在 **方法命名约定**，其次约束类命名与使用方式。

---
二、总体设计原则（官方思想 + 工程约定）
---

1. 单一职责原则（官方明确立场）

Converter 的唯一职责是：
“对象结构映射（Structural Mapping）”

明确禁止在 Converter 中出现：

* 业务逻辑
* 权限判断
* 状态流转
* 数据校验
* 复杂计算

Converter ≠ Service ≠ Assembler

2. 约定优于配置（MapStruct 官方核心思想）

优先使用 MapStruct 的默认能力：

* 同名字段自动映射
* 集合类型自动映射
* 嵌套对象递归映射

只有在以下场景才允许使用 @Mapping：

* 字段名不一致
* 忽略字段
* 扁平化 / 嵌套结构转换

禁止为了“看起来清晰”而滥用 @Mapping。

3. 命名服务于自动推断，而不是个人习惯

MapStruct 并不关心方法名语义，
但它**强烈依赖“方法唯一性”**来完成自动选择。

团队通过统一命名约定来：

* 消除歧义
* 避免 qualifier
* 让 MapStruct“猜得到、选得对”

---
三、Converter 接口命名规范
---

1. 基本规则

统一命名格式：

<Domain>Converter

示例：

* UserConverter
* OrderConverter
* RoleConverter
* ProcessDefinitionConverter

2. 命名选择说明

使用 Converter 而不是 Mapper：

* 避免与 MyBatis Mapper 概念冲突
* 职责语义更清晰
* 符合工程实际，而不违背 MapStruct 官方思想

明确禁止以下命名：

* XxxMapper（概念混淆）
* XxxAssembler（DDD 语义，不统一）
* XxxConvertUtil（工具类语义，破坏 Spring 管理）

---
四、方法命名核心规范（最重要部分）
---

1. 官方推荐的命名思想（关键）

MapStruct 官方并未强制方法名格式，但推荐遵循：

* 方法名体现“目标类型”
* 同一来源 → 同一目标，只存在一个“默认映射方法”

团队在此基础上形成如下约定。

2. 通用命名格式（默认规则）

无歧义场景：

to<目标类型>

示例：

* toEntity
* toVO
* toDTO
* toBO

3. 存在歧义时的命名格式（官方推荐做法）

当“同一目标类型”存在多个来源时，必须显式标明来源：

to<目标类型>From<来源类型>

这是 **替代 @Named / qualifiedBy 的首选方案**。

示例：

* toEntityFromCreateCmd
* toEntityFromUpdateCmd

---
五、基础对象转换命名规范
---

1. 常见对象之间的约定

DTO / Cmd / Query → Entity
方法名：toEntity

Entity → DTO
方法名：toDTO

Entity → VO
方法名：toVO

Entity → BO
方法名：toBO

BO / VO → Entity（如确有需要）
方法名：toEntity

示例：

UserEntity toEntity(UserCreateCmd cmd);
UserVO toVO(UserEntity entity);
UserBO toBO(UserEntity entity);

2. 关于对象后缀的统一理解（约定）

DTO：数据传输对象
VO：视图对象
BO：业务对象
Cmd：命令对象
Query：查询对象

命名中必须显式体现对象角色，禁止模糊类型。

---
六、集合与分页转换命名规范
---

1. 集合转换（List / Collection）

MapStruct 会自动识别集合类型并复用单对象方法。

命名规则：

to<目标类型>List

示例：

List<UserVO> toVOList(List<UserEntity> entities);
List<UserEntity> toEntityList(List<UserCreateCmd> cmds);

说明：

* 即使不写该方法，MapStruct 也能自动生成
* 显式声明的目的在于提高可读性

2. 分页对象（MyBatis-Plus IPage）

命名规则：

to<目标类型>Page

示例：

IPage<UserVO> toVOPage(IPage<UserEntity> page);

---
七、多来源 / 多目标场景的官方推荐处理方式
---

1. 同一目标，多来源（必须使用 From）

这是 MapStruct 中最容易产生歧义的场景。

规范要求：

* 不允许存在两个“来源不同但签名冲突”的方法
* 必须通过 FromXxx 消除歧义

示例：

UserEntity toEntityFromCreateCmd(UserCreateCmd cmd);
UserEntity toEntityFromUpdateCmd(UserUpdateCmd cmd);

2. 同一来源，多目标（允许省略 From）

示例：

UserDTO toDTO(UserEntity entity);
UserVO toVO(UserEntity entity);

这种情况不会造成歧义，符合官方默认推断逻辑。

---
八、轻量 / 局部对象命名规范
---

适用于 Simple / Lite / Brief 等轻量 VO。

命名规则：

to<目标类型>Simple

示例：

UserSimpleVO toSimpleVO(UserEntity entity);
List<UserSimpleVO> toSimpleVOList(List<UserEntity> entities);

说明：

* Simple 是目标类型的一部分
* 不要写成 toSimple() 这种无语义方法

---
九、更新场景（@MappingTarget）命名规范
---

这是 MapStruct 官方明确推荐的写法。

命名规则：

update<目标类型>

示例：

void updateEntity(@MappingTarget UserEntity entity, UserUpdateCmd cmd);

明确禁止使用：

* merge
* copy
* apply

原因：
这些词无法准确表达“基于输入对象更新已有目标对象”。

---
十、Boolean / 状态字段的官方建议
---

原则：

* Converter 不负责状态计算
* 只做字段映射

允许的写法：

@Mapping(target = "enabled", source = "status")
UserVO toVO(UserEntity entity);

禁止在 Converter 中：

* 写复杂条件判断
* 使用 expression 承担业务计算

---
十一、明确禁止与不推荐的方法命名
---

明确禁止的方法名（无语义）：

convert
map
parse
build

不推荐的方法名（历史写法，禁止新增）：

doConvert
entity2VO
vo2Entity

原因：

* 不符合 Java 语义
* 不利于 IDE 自动补全
* 不利于 MapStruct 自动方法选择

---
十二、完整示例（官方风格 + 团队约定）
---

@Mapper(componentModel = "spring")
public interface UserConverter {

``` java
UserEntity toEntity(UserCreateCmd cmd);

UserEntity toEntityFromUpdateCmd(UserUpdateCmd cmd);

void updateEntity(@MappingTarget UserEntity entity, UserUpdateCmd cmd);

UserBO toBO(UserEntity entity);

UserVO toVO(UserEntity entity);

List<UserVO> toVOList(List<UserEntity> entities);

UserSimpleVO toSimpleVO(UserEntity entity);
```

}

---
十三、重要官方经验补充（强烈建议写入规范）
---

1. 善用自动方法复用

只要存在：

UserVO toVO(UserEntity entity);

MapStruct 会自动复用该方法用于：

* List<UserEntity> → List<UserVO>
* 嵌套对象中的 UserEntity → UserVO

不需要额外配置。

2. 谨慎使用 @Named / qualifiedByName

官方建议：

* 90% 的场景不需要 qualifier
* 一旦使用，说明设计或命名存在问题

推荐优先级：

1）调整方法命名（FromXxx）
2）拆分 Converter
3）最后才使用 @Named

3. expression 是最后手段

expression 不应承载业务含义。
如需计算，应在 Service / BO 层提前完成。

4. Spring 模式官方推荐

统一使用：

@Mapper(componentModel = "spring")

不使用 Mappers.getMapper。

---
十四、结论
---

本规范的核心结论只有一句话：

**方法命名越清晰，MapStruct 自动能力越强。**

通过统一命名约定：

* 降低歧义
* 减少配置
* 提高可维护性
* 为 DDD / CQRS / 多端 VO 扩展打好基础

这是一份可以长期演进、而不需要推翻重来的规范。

—— 完 ——

如果你愿意，下一步我可以直接帮你：

* 压缩成「团队必读 1 页版」
* 或按你现有项目做一轮 **Converter 命名规范化清单**
