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

package com.devops00.spectra.common.port.security;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 业务层读取当前安全上下文的窄端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public interface SecurityContextAccessor {

    /**
     * 返回当前请求的安全主体；未认证时返回 null。
     *
     * @return 当前安全主体；未认证时为 null。
     */
    @Nullable
    SecurityPrincipal currentUser();

    /**
     * 返回当前安全主体的用户标识；未认证时返回 null。
     *
     * @return 当前用户标识；未认证时为 null。
     */
    @Nullable
    UUID currentUserId();

    /**
     * 返回当前请求使用的访问令牌。
     *
     * @return 当前请求使用的访问令牌。
     */
    @Nullable
    String currentToken();

    /**
     * 返回当前用户有效的时区标识。
     *
     * @return 当前用户有效的时区标识。
     */
    String currentUserZoneId();

    /**
     * 返回当前安全主体的用户名。
     *
     * @return 当前用户名。
     */
    String currentUsername();
}
