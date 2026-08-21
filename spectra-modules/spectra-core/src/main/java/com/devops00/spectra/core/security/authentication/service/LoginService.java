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

package com.devops00.spectra.core.security.authentication.service;

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;

/**
 * 登录认证用例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
public interface LoginService {

    /** 执行账号主认证，必要时返回 MFA 预认证挑战。 */
    TokenVO login(LoginFrom params, ClientType clientType);

    /** 校验已完成主认证的 MFA 挑战并签发会话。 */
    TokenVO verifyMfa(String challengeId, String code, ClientType clientType);

    /** 首次完成 MFA 登记后消费挑战并签发会话。 */
    TokenVO completeMfaEnrollment(String challengeId, ClientType clientType);

    /** 撤销当前会话和刷新会话。 */
    void logout(String token, String refreshToken, ClientType clientType);

    /** 轮换刷新令牌并返回新会话。 */
    TokenVO refresh(String refreshToken, ClientType clientType);
}
