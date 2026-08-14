/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.holder;

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;

/** Security Session 签发与 Refresh Rotation 窄端口。 */
public interface SecuritySessionIssuer {

    /** 按请求客户端签发会话令牌。 */
    TokenVO createToken(SecurityUser user);

    /** 按指定客户端签发会话令牌。 */
    TokenVO createToken(SecurityUser user, ClientType clientType);

    /** 消费 Refresh Token 并签发下一代令牌对。 */
    TokenVO refreshByRefreshToken(String refreshToken);
}
