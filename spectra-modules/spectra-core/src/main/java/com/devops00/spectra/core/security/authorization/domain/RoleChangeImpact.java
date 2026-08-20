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

package com.devops00.spectra.core.security.authorization.domain;

import java.util.Set;

/**
 * Role 变更影响分析结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record RoleChangeImpact(Set<String> addedPermissions,
                               Set<String> removedPermissions,
                               Set<String> addedGrantablePermissions,
                               Set<String> removedGrantablePermissions,
                               boolean authorityLevelChanged,
                               boolean expandsEffectiveAuthority,
                               int affectedAssignmentCount,
                               int affectedUserCount) {

    public RoleChangeImpact {
        addedPermissions = addedPermissions == null ? Set.of() : Set.copyOf(addedPermissions);
        removedPermissions = removedPermissions == null ? Set.of() : Set.copyOf(removedPermissions);
        addedGrantablePermissions = addedGrantablePermissions == null ? Set.of() : Set.copyOf(addedGrantablePermissions);
        removedGrantablePermissions = removedGrantablePermissions == null ? Set.of() : Set.copyOf(removedGrantablePermissions);
    }
}
