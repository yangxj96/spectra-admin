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

package com.devops00.spectra.security.base.change;

import java.util.UUID;

/**
 * 安全变更后的用户 Session 撤销端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@FunctionalInterface
public interface SecuritySessionRevocationPort {

    /**
     * 撤销用户全部 Session。
     */
    void revokeUserSessions(UUID userId);

    /**
     * 撤销用户除当前 Access Token 外的其他 Session。
     *
     * <p>用于安全变更接口的当前操作者：其他会话必须失效，但当前请求需要继续完成响应后的页面刷新。
     * 未提供专用实现时默认退化为撤销全部 Session。</p>
     */
    default void revokeUserSessionsExceptToken(UUID userId, String accessToken) {
        /**
         * 更新或推进目标状态（{@code revokeUserSessions}）。
         */
        revokeUserSessions(userId);
    }
}
