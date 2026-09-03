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

package com.devops00.spectra.common.security.authorization;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 可绑定到单个 Permission 的 AccessScope 或 GrantScope。
 */
public record AuthorizationScope(ScopeMode mode, Set<UUID> departmentIds, boolean includeDescendants) {

    /**
     * 创建或构建目标数据（{@code of}）。
     */
    public static AuthorizationScope of(ScopeMode mode) {
        return new AuthorizationScope(mode, Set.of(), false);
    }

    public AuthorizationScope {
        if (mode == null) {
            throw new IllegalArgumentException("scope mode 不能为空");
        }
        departmentIds = departmentIds == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(departmentIds));
        if ((mode == ScopeMode.RULES) && departmentIds.isEmpty()) {
            throw new IllegalArgumentException("RULES scope 必须包含部门规则");
        }
        if (mode != ScopeMode.RULES && !departmentIds.isEmpty()) {
            throw new IllegalArgumentException("只有 RULES scope 可以包含部门规则");
        }
        if (mode != ScopeMode.RULES && includeDescendants) {
            throw new IllegalArgumentException("只有 RULES scope 可以包含下级部门");
        }
    }

    @Override
    public Set<UUID> departmentIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(departmentIds));
    }

    /**
     * 查询或获取目标数据（{@code allows}）。
     */
    public boolean allows(ScopeQuery query) {
        if (query == null) {
            return false;
        }
        return switch (mode) {
            case NONE, ALL -> true;
            case SELF -> query.subjectId() != null && query.subjectId().equals(query.ownerId());
            case RULES -> query.departmentId() != null
                    && (departmentIds.contains(query.departmentId())
                            || (includeDescendants && query.departmentLineage().stream().anyMatch(departmentIds::contains)));
        };
    }
}
