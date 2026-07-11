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

package com.devops00.spectra.common.mybatis;

import com.devops00.spectra.common.constant.DataScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/// 数据范围提供者接口 — 由 core 模块实现，供 framework 层的拦截器调用
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
public interface DataScopeProvider {

    /// 计算指定用户的最终有效数据范围
    ///
    /// 规则：
    /// 1. 用户自定义范围优先于角色范围
    /// 2. 多角色时取最大范围（GLOBAL > DEPT_AND_CHILDREN > DEPT > CUSTOM > SELF）
    /// 3. 所有 CUSTOM 的 targetIds 合并
    ///
    /// @param userId 用户ID
    /// @return 有效数据范围
    EffectiveScope resolve(UUID userId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class EffectiveScope {
        /// 数据范围类型
        private DataScopeType scopeType;
        /// 用户所属部门ID
        private UUID departmentId;
        /// 自定义目标部门ID列表（CUSTOM + DEPT_AND_CHILDREN 时填充）
        private List<UUID> targetIds;
    }
}
