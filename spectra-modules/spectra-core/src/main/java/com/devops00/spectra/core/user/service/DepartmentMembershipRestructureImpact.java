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

package com.devops00.spectra.core.user.service;

import java.util.Set;
import java.util.UUID;

/**
 * 部门重组涉及的成员关系计数。
 *
 * @param affectedUserIds             需推进安全版本的用户 ID 集合
 * @param primaryDepartmentCount      被替换的主部门关系数
 * @param associatedDepartmentCount   被替换的关联部门关系数
 * @param deduplicatedAssociatedCount 合并或主关联冲突中被去重的关系数
 * @param stateFingerprint           成员关系快照摘要，用于 Apply 检查 Preview 后的变化
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public record DepartmentMembershipRestructureImpact(
        Set<UUID> affectedUserIds,
        int primaryDepartmentCount,
        int associatedDepartmentCount,
        int deduplicatedAssociatedCount,
        String stateFingerprint) {

    public DepartmentMembershipRestructureImpact {
        affectedUserIds = Set.copyOf(affectedUserIds);
    }

    public int affectedUserCount() {
        return affectedUserIds.size();
    }
}
