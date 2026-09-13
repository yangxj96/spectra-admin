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

package com.devops00.spectra.core.security.authorization.javabean.vo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 封装授权上下文相关的响应数据。
 *
 * @param permissions          角色拥有的权限集合
 * @param grantablePermissions 角色可授予的权限集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record AuthorizationContextVO(Set<String> permissions, Set<String> grantablePermissions) {

    public AuthorizationContextVO {
        permissions = immutableSet(permissions);
        grantablePermissions = immutableSet(grantablePermissions);
    }

    @Override
    public Set<String> permissions() {
        return immutableSet(permissions);
    }

    @Override
    public Set<String> grantablePermissions() {
        return immutableSet(grantablePermissions);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableSet}）。
     */
    private static Set<String> immutableSet(Set<String> source) {
        return source == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }
}
