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
 * 一个 Permission 与其 Scope 的绑定。禁止把 Scope 从绑定中抽出来全局合并。
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
