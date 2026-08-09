/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.assembler;

import com.devops00.spectra.framework.assembler.converter.IdConverter;
import com.devops00.spectra.framework.assembler.converter.UuidConverter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/2 16:26
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
        Class<ID> type = (Class<ID>) UUID.class;
        return type;
    }

    /// ID 转换器（默认 UUID）
    default IdConverter<ID> idConverter() {
        @SuppressWarnings("unchecked")
        var converter = (IdConverter<ID>) new UuidConverter();
        return converter;
    }

    /// 批量查询 ID 对应的名称映射
    ///
    /// 返回 Map 中:
    /// * Key:ID
    /// * Value:对应的展示名称
    ///
    /// @param ids ID 集合（不为空）
    /// @return ID → Name 的映射关系
    Map<ID, String> getNameMap(Set<ID> ids);
}
