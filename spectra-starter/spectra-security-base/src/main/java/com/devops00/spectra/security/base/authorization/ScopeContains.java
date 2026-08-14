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

package com.devops00.spectra.security.base.authorization;

import java.util.Objects;

/**
 * Permission-specific Scope 包含关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class ScopeContains {

    private ScopeContains() {
    }

    /**
     * 使用精确部门关系判断 Scope 包含关系。
     */
    public static boolean contains(AuthorizationScope container, AuthorizationScope requested) {
        return contains(container, requested, Objects::equals);
    }

    /**
     * 判断 container 是否覆盖 requested。调用方必须先确保两者属于同一 Permission。
     */
    public static boolean contains(AuthorizationScope container,
                                   AuthorizationScope requested,
                                   DepartmentHierarchy hierarchy) {
        if (container == null || requested == null || hierarchy == null) {
            return false;
        }
        if (container.mode() == ScopeMode.ALL) {
            return true;
        }
        if (requested.mode() == ScopeMode.NONE) {
            return true;
        }
        if (container.mode() == ScopeMode.NONE) {
            return false;
        }
        if (container.mode() == ScopeMode.SELF) {
            return requested.mode() == ScopeMode.SELF;
        }
        if (container.mode() != ScopeMode.RULES || requested.mode() != ScopeMode.RULES) {
            return false;
        }
        for (var requestedDepartment : requested.departmentIds()) {
            boolean covered = container.departmentIds().stream()
                    .anyMatch(containerDepartment -> containerDepartment.equals(requestedDepartment)
                            || (container.includeDescendants() && hierarchy.contains(containerDepartment, requestedDepartment)));
            if (!covered) {
                return false;
            }
        }
        if (requested.includeDescendants() && !container.includeDescendants()) {
            return false;
        }
        return true;
    }
}
