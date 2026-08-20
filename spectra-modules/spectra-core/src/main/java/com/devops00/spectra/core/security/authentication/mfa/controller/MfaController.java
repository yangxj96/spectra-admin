/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.mfa.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.authentication.mfa.javabean.vo.MfaEnrollmentResult;
import com.devops00.spectra.core.security.authentication.mfa.service.MfaService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.log.base.enums.SysLogType;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

/** MFA 登记和恢复码 API。 */
@Validated
@RestController
@RequestMapping("/security/mfa")
public class MfaController {

    private final MfaService mfaService;

    private final SecurityContextAccessor securityContextAccessor;

    private final SecurityMfaChallengePort mfaChallengePort;

    public MfaController(MfaService mfaService, SecurityContextAccessor securityContextAccessor,
                         ObjectProvider<SecurityMfaChallengePort> mfaChallengeProvider) {
        this.mfaService = mfaService;
        this.securityContextAccessor = securityContextAccessor;
        this.mfaChallengePort = mfaChallengeProvider.getIfAvailable();
    }

    @PostMapping("/totp/enroll")
    @PreAuthorize("isAuthenticated()")
    public MfaEnrollmentResult beginTotpEnrollment() {
        return mfaService.beginTotpEnrollment(currentUserId());
    }

    @ULog(value = "'开始首次 MFA 登记'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/enroll", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public MfaEnrollmentResult beginSetupTotpEnrollment(@Valid @RequestBody SetupChallengeFrom from,
                                                        HttpServletRequest request) {
        MfaLoginChallenge challenge = setupChallenge(from.challengeId());
        validateChallengeClient(challenge, request);
        return mfaService.beginTotpEnrollment(challenge.userId());
    }

    @PostMapping("/totp/confirm")
    @PreAuthorize("isAuthenticated()")
    public List<String> confirmTotpEnrollment(@Valid @RequestBody ConfirmFrom from) {
        return mfaService.confirmTotpEnrollment(currentUserId(), from.enrollmentId(), from.code());
    }

    @ULog(value = "'确认首次 MFA 登记'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/confirm", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public List<String> confirmSetupTotpEnrollment(@Valid @RequestBody SetupConfirmFrom from,
                                                   HttpServletRequest request) {
        MfaLoginChallenge challenge = setupChallenge(from.challengeId());
        validateChallengeClient(challenge, request);
        List<String> recoveryCodes = mfaService.confirmTotpEnrollment(challenge.userId(), from.enrollmentId(), from.code());
        if (!requireChallengePort().markEnrollmentCompleted(from.challengeId())) {
            throw new IllegalStateException("MFA 挑战状态更新失败");
        }
        return recoveryCodes;
    }

    @PostMapping("/recovery/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void verifyRecoveryCode(@Valid @RequestBody RecoveryCodeFrom from) {
        if (!mfaService.consumeRecoveryCode(currentUserId(), from.code())) {
            throw new IllegalArgumentException("Recovery Code 无效或已使用");
        }
    }

    @PostMapping("/recovery/rotate")
    @PreAuthorize("isAuthenticated()")
    public List<String> rotateRecoveryCodes() {
        return mfaService.rotateRecoveryCodes(currentUserId());
    }

    private UUID currentUserId() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前请求没有有效用户");
        }
        return userId;
    }

    private MfaLoginChallenge setupChallenge(String challengeId) {
        MfaLoginChallenge challenge = requireChallengePort().find(challengeId);
        if (challenge == null || !challenge.enrollmentRequired() || challenge.enrollmentCompleted()) {
            throw new IllegalArgumentException("MFA 登记挑战不存在或已失效");
        }
        return challenge;
    }

    private SecurityMfaChallengePort requireChallengePort() {
        if (mfaChallengePort == null) {
            throw new IllegalStateException("MFA 挑战存储未配置");
        }
        return mfaChallengePort;
    }

    private void validateChallengeClient(MfaLoginChallenge challenge, HttpServletRequest request) {
        String clientType = request.getHeader("X-Client-Type");
        ClientType actual = ClientType.fromName(clientType);
        if (challenge.clientType() != actual) {
            throw new IllegalArgumentException("MFA 挑战客户端不匹配");
        }
    }

    public record ConfirmFrom(@NotNull UUID enrollmentId, @NotBlank String code) {
    }

    public record SetupChallengeFrom(@NotBlank String challengeId) {
    }

    public record SetupConfirmFrom(@NotBlank String challengeId, @NotNull UUID enrollmentId,
                                   @NotBlank String code) {
    }

    public record RecoveryCodeFrom(@NotBlank String code) {
    }
}
