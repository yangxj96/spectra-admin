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

/**
 * 认证生命周期端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public interface SecurityAuthenticationPort {

    /** 为认证主体签发会话令牌。 */
    SecurityToken login(SecurityPrincipal principal);

    /** 撤销指定 Access Token。 */
    void logout(String accessToken);

    /** 根据 Refresh Token 撤销会话。 */
    void logoutByRefreshToken(String refreshToken);

    /** 轮换 Refresh Token 并签发新的会话令牌。 */
    SecurityToken refreshByRefreshToken(String refreshToken);

    /** 检查主体是否处于登录失败锁定窗口。 */
    boolean isLockedOut(String username);

    /** 记录一次认证失败。 */
    void recordLoginFail(String username);

    /** 清理主体的认证失败计数。 */
    void clearLoginFail(String username);
}
