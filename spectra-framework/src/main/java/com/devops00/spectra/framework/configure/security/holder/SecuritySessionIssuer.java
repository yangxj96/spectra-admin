/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.holder;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;

/** Security Session 签发与 Refresh Rotation 窄端口。 */
public interface SecuritySessionIssuer {

    /** 按请求客户端签发会话令牌。 */
    SecurityToken createToken(SecurityPrincipal user);

    /** 按指定客户端签发会话令牌。 */
    SecurityToken createToken(SecurityPrincipal user, ClientType clientType);

    /** 消费 Refresh Token 并签发下一代令牌对。 */
    SecurityToken refreshByRefreshToken(String refreshToken);
}
