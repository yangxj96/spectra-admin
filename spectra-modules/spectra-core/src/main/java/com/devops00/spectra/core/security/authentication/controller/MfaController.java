/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaConfirmFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaDisableFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaRecoveryCodeFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaSetupChallengeFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaSetupConfirmFrom;
import com.devops00.spectra.core.security.authentication.javabean.vo.MfaEnrollmentResult;
import com.devops00.spectra.core.security.authentication.javabean.vo.MfaStatusVO;
import com.devops00.spectra.core.security.authentication.service.MfaService;
import com.devops00.spectra.core.security.authentication.util.AuthenticationContextUtils;
import com.devops00.spectra.core.security.authentication.util.MfaChallengeUtils;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/** MFA 登记和恢复码 API。 */
@Validated
@RestController
@RequestMapping("/security/mfa")
public class MfaController {

    private final MfaService mfaService;

    private final SecurityContextAccessor securityContextAccessor;

    private final ObjectProvider<SecurityMfaChallengePort> mfaChallengeProvider;

    public MfaController(MfaService mfaService, SecurityContextAccessor securityContextAccessor,
                         ObjectProvider<SecurityMfaChallengePort> mfaChallengeProvider) {
        this.mfaService = mfaService;
        this.securityContextAccessor = securityContextAccessor;
        this.mfaChallengeProvider = mfaChallengeProvider;
    }

    /**
     * 创建或构建目标数据（{@code beginTotpEnrollment}）。
     */
    @Audit(value = "'开始 MFA 登记'", category = AuditCategory.SECURITY)
    @PostMapping(value = "/totp/enroll", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public MfaEnrollmentResult beginTotpEnrollment() {
        return mfaService.beginTotpEnrollment(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor));
    }

    /** 查询当前用户的 MFA 状态。 */
    @GetMapping(value = "/status", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public MfaStatusVO status() {
        return mfaService.status(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor));
    }

    /**
     * 创建或构建目标数据（{@code beginSetupTotpEnrollment}）。
     */
    @Audit(value = "'开始首次 MFA 登记'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/enroll", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public MfaEnrollmentResult beginSetupTotpEnrollment(@Valid @RequestBody MfaSetupChallengeFrom from,
                                                        HttpServletRequest request) {
        MfaLoginChallenge challenge = MfaChallengeUtils.requireSetupChallenge(mfaChallengeProvider.getIfAvailable(), from.getChallengeId(), request);
        return mfaService.beginTotpEnrollment(challenge.userId());
    }

    /**
     * 处理内部业务逻辑（{@code confirmTotpEnrollment}）。
     */
    @Audit(value = "'确认 MFA 登记'", category = AuditCategory.SECURITY)
    @PostMapping(value = "/totp/confirm", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public List<String> confirmTotpEnrollment(@Valid @RequestBody MfaConfirmFrom from) {
        return mfaService.confirmTotpEnrollment(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor),
                from.getEnrollmentId(), from.getCode());
    }

    /** 停用当前用户的 TOTP MFA。 */
    @Audit(value = "'停用 MFA'", category = AuditCategory.SECURITY)
    @PostMapping(value = "/totp/disable", version = "1.0.0")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void disableTotp(@Valid @RequestBody MfaDisableFrom from) {
        mfaService.disableTotp(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor), from.getCode());
    }

    /**
     * 处理内部业务逻辑（{@code confirmSetupTotpEnrollment}）。
     */
    @Audit(value = "'确认首次 MFA 登记'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/confirm", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public List<String> confirmSetupTotpEnrollment(@Valid @RequestBody MfaSetupConfirmFrom from,
                                                   HttpServletRequest request) {
        MfaLoginChallenge challenge = MfaChallengeUtils.requireSetupChallenge(mfaChallengeProvider.getIfAvailable(), from.getChallengeId(), request);
        List<String> recoveryCodes = mfaService.confirmTotpEnrollment(challenge.userId(), from.getEnrollmentId(), from.getCode());
        if (!MfaChallengeUtils.requireChallengePort(mfaChallengeProvider.getIfAvailable()).markEnrollmentCompleted(from.getChallengeId())) {
            throw new IllegalStateException("MFA 挑战状态更新失败");
        }
        return recoveryCodes;
    }

    /**
     * 处理内部业务逻辑（{@code verifyRecoveryCode}）。
     */
    @PostMapping(value = "/recovery/verify", version = "1.0.0")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void verifyRecoveryCode(@Valid @RequestBody MfaRecoveryCodeFrom from) {
        if (!mfaService.consumeRecoveryCode(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor),
                from.getCode())) {
            throw new IllegalArgumentException("Recovery Code 无效或已使用");
        }
    }

    /**
     * 处理内部业务逻辑（{@code rotateRecoveryCodes}）。
     */
    @PostMapping(value = "/recovery/rotate", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public List<String> rotateRecoveryCodes() {
        return mfaService.rotateRecoveryCodes(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor));
    }
}
