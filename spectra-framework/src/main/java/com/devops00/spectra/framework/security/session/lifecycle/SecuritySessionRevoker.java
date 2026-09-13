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

package com.devops00.spectra.framework.security.session.lifecycle;

import com.devops00.spectra.common.constant.ClientType;

import java.util.UUID;

/**
 * Security Session 撤销窄端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecuritySessionRevoker {

    /** 撤销指定 Access Token。 */
    void deleteToken(String token);

    /** 按 Refresh Token 撤销其关联会话。 */
    void deleteByRefreshToken(String refreshToken);

    /** 按在线管理的随机会话句柄撤销对应 Refresh Token Family。 */
    void deleteBySessionId(String sessionId);

    /** 撤销用户的全部会话。 */
    void deleteByUserId(UUID userId);

    /** 撤销用户除指定 Access Token 外的其他会话。 */
    default void deleteByUserIdExceptToken(UUID userId, String accessToken) {
        /**
         * 更新或推进目标状态（{@code deleteByUserId}）。
         */
        deleteByUserId(userId);
    }

    /** 撤销用户指定客户端的会话。 */
    void deleteByUserIdAndClient(String userId, ClientType clientType);
}
