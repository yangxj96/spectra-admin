package io.github.yangxj96.spectra.core.configure.assembler;

import java.util.Map;
import java.util.Set;

/// 名称查询抽象接口
///
/// 该接口用于定义「ID → Name」的批量查询能力，
/// 是 {@link NameFill} 注解的核心扩展点。
///
/// 实现类职责：
/// * 只关心如何根据 ID 获取名称
/// * 可以使用数据库、缓存、RPC 等任意方式
/// * 必须支持<strong>批量查询</strong>
///
/// 设计约束：
/// * 不得包含业务逻辑
/// * 不得产生副作用
/// * 建议实现类是无状态的
///
/// @param <ID> ID 类型（如 Long / String）
public interface NameLookup<ID> {

    /// 声明 Lookup 支持的 ID 类型
    ///
    /// 默认返回 String.class，
    /// 若使用其他类型（如 Long），
    /// 实现类应显式覆写该方法。
    ///
    /// @return ID 的 Class 类型
    default Class<ID> idType() {
        @SuppressWarnings("unchecked")
        Class<ID> type = (Class<ID>) String.class;
        return type;
    }

    /// 批量查询 ID 对应的名称映射
    ///
    /// 返回 Map 中:
    /// * Key:ID
    /// * Value:对应的展示名称
    /// @param ids ID 集合（不为空）
    /// @return ID → Name 的映射关系
    Map<ID, String> getNameMap(Set<ID> ids);
}
