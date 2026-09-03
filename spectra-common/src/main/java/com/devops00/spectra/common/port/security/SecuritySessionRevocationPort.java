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

import java.util.UUID;

/**
 * 安全变更后的用户 Session 撤销端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@FunctionalInterface
public interface SecuritySessionRevocationPort {

    /** 撤销用户全部 Session。 */
    void revokeUserSessions(UUID userId);

    /** 撤销用户除当前 Access Token 外的其他 Session。 */
    default void revokeUserSessionsExceptToken(UUID userId, String accessToken) {
        revokeUserSessions(userId);
    }
}
