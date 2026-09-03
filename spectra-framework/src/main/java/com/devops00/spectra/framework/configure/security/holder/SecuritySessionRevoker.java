/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.holder;

import com.devops00.spectra.common.constant.ClientType;

import java.util.UUID;

/** Security Session 撤销窄端口。 */
public interface SecuritySessionRevoker {

    /** 撤销指定 Access Token。 */
    void deleteToken(String token);

    /** 按 Refresh Token 撤销其关联会话。 */
    void deleteByRefreshToken(String refreshToken);

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
