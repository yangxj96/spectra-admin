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
 * 承载角色相关的不可变数据。
 *
 * @param addedPermissions            本次新增的权限集合
 * @param removedPermissions          本次移除的权限集合
 * @param addedGrantablePermissions   本次新增的可授予权限集合
 * @param removedGrantablePermissions 本次移除的可授予权限集合
 * @param authorityLevelChanged       角色权限等级是否发生变化
 * @param expandsEffectiveAuthority   本次变更是否扩大用户的有效权限
 * @param affectedAssignmentCount     本次变更影响的权限分配数量
 * @param affectedUserCount           本次变更影响的用户数量
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
