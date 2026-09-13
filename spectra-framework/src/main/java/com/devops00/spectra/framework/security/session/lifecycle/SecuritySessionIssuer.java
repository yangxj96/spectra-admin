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
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;

/**
 * Security Session 签发窄端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecuritySessionIssuer {

    /** 按请求客户端签发会话令牌。 */
    SecurityToken createToken(SecurityPrincipal user);

    /** 按指定客户端签发会话令牌。 */
    SecurityToken createToken(SecurityPrincipal user, ClientType clientType);

}
