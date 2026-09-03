/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    /** @return 当前安全主体 */
    @Nullable
    SecurityPrincipal currentUser();

    /** @return 当前用户 ID */
    @Nullable
    UUID currentUserId();

    /** @return 当前请求的 Access Token */
    @Nullable
    String currentToken();

    /** @return 当前请求的有效时区 ID */
    String currentUserZoneId();

    /** @return 当前用户名 */
    String currentUsername();
}
