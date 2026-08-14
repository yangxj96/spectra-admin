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

/**
 * 一个 Permission 的授权变更请求。Access/Grant Boundary 始终保持在 Permission 粒度。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record AuthorizationGrantRequest(String permission,
                                        AuthorizationScope accessScope,
                                        AuthorizationScope grantScope,
                                        int targetAuthorityLevel) {

    public AuthorizationGrantRequest {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("授权 Permission 不能为空");
        }
        if (targetAuthorityLevel <= 0) {
            throw new IllegalArgumentException("目标 authorityLevel 必须大于 0");
        }
    }
}
