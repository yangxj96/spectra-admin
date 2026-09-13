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

/**
 * 承载权限边界相关的不可变数据。
 *
 * @param permission 本次授权操作对应的权限
 * @param scope      该权限允许访问的数据范围定义
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record PermissionBoundary(String permission, AuthorizationScope scope) {

    public PermissionBoundary {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("permission 不能为空");
        }
        if (scope == null) {
            throw new IllegalArgumentException("permission scope 不能为空");
        }
    }
}
