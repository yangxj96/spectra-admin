/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.change;

import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;

/**
 * 认证生命周期端口。
 *
 * <p>业务和 Web 适配器只依赖认证动作，不直接持有底层会话策略。</p>
 */
public interface SecurityAuthenticationPort {

    /** 为认证主体签发会话令牌。 */
    TokenVO login(SecurityUser user);

    /** 撤销指定 Access Token。 */
    void logout(String token);

    /** 根据 Refresh Token 撤销会话。 */
    void logoutByRefreshToken(String refreshToken);

    /** 轮换 Refresh Token 并签发新的会话令牌。 */
    TokenVO refreshByRefreshToken(String refreshToken);

    /** 检查主体是否处于登录失败锁定窗口。 */
    boolean isLockedOut(String username);

    /** 记录一次认证失败。 */
    void recordLoginFail(String username);

    /** 清理主体的认证失败计数。 */
    void clearLoginFail(String username);
}
