/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.service;

import com.devops00.spectra.core.security.authentication.javabean.vo.MfaEnrollmentResult;
import com.devops00.spectra.security.base.mfa.SecurityMfaVerifier;

import java.util.List;
import java.util.UUID;

/** MFA 登记、挑战和 Recovery Code 应用服务。 */
public interface MfaService extends SecurityMfaVerifier {

    /**
     * 创建或构建目标数据（{@code beginTotpEnrollment}）。
     */
    MfaEnrollmentResult beginTotpEnrollment(UUID userId);

    /**
     * 处理内部业务逻辑（{@code confirmTotpEnrollment}）。
     */
    List<String> confirmTotpEnrollment(UUID userId, UUID enrollmentId, String code);

    /**
     * 处理内部业务逻辑（{@code verifyTotp}）。
     */
    @Override
    boolean verifyTotp(UUID userId, String code);

    /**
     * 更新或推进目标状态（{@code consumeRecoveryCode}）。
     */
    @Override
    boolean consumeRecoveryCode(UUID userId, String code);

    /** 原子作废旧 Recovery Code 并生成一组新码；明文只在本次响应中返回。 */
    List<String> rotateRecoveryCodes(UUID userId);
}
