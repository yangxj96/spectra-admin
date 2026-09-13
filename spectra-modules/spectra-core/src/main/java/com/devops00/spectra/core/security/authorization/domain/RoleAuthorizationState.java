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
 * 承载角色授权状态相关的不可变数据。
 *
 * @param authorityLevel       角色当前的权限等级
 * @param permissions          角色拥有的权限集合
 * @param grantablePermissions 角色可授予的权限集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record RoleAuthorizationState(int authorityLevel,
                                     Set<String> permissions,
                                     Set<String> grantablePermissions) {

    public RoleAuthorizationState {
        if (authorityLevel <= 0) {
            throw new IllegalArgumentException("authorityLevel 必须大于 0");
        }
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        grantablePermissions = grantablePermissions == null ? Set.of() : Set.copyOf(grantablePermissions);
    }
}
