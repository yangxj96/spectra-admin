/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaConfirmFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaRecoveryCodeFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaSetupChallengeFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaSetupConfirmFrom;
import com.devops00.spectra.core.security.authentication.javabean.vo.MfaEnrollmentResult;
import com.devops00.spectra.core.security.authentication.service.MfaService;
import com.devops00.spectra.core.security.authentication.util.AuthenticationContextUtils;
import com.devops00.spectra.core.security.authentication.util.MfaChallengeUtils;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.log.base.enums.SysLogType;
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
        return mfaService.beginTotpEnrollment(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor));
    }

    @ULog(value = "'开始首次 MFA 登记'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/enroll", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public MfaEnrollmentResult beginSetupTotpEnrollment(@Valid @RequestBody MfaSetupChallengeFrom from,
                                                        HttpServletRequest request) {
        MfaLoginChallenge challenge = MfaChallengeUtils.requireSetupChallenge(mfaChallengePort, from.challengeId(), request);
        return mfaService.beginTotpEnrollment(challenge.userId());
    }

    @PostMapping("/totp/confirm")
    @PreAuthorize("isAuthenticated()")
    public List<String> confirmTotpEnrollment(@Valid @RequestBody MfaConfirmFrom from) {
        return mfaService.confirmTotpEnrollment(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor),
                from.enrollmentId(), from.code());
    }

    @ULog(value = "'确认首次 MFA 登记'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PostMapping(value = "/setup/totp/confirm", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public List<String> confirmSetupTotpEnrollment(@Valid @RequestBody MfaSetupConfirmFrom from,
                                                   HttpServletRequest request) {
        MfaLoginChallenge challenge = MfaChallengeUtils.requireSetupChallenge(mfaChallengePort, from.challengeId(), request);
        List<String> recoveryCodes = mfaService.confirmTotpEnrollment(challenge.userId(), from.enrollmentId(), from.code());
        if (!MfaChallengeUtils.requireChallengePort(mfaChallengePort).markEnrollmentCompleted(from.challengeId())) {
            throw new IllegalStateException("MFA 挑战状态更新失败");
        }
        return recoveryCodes;
    }

    @PostMapping("/recovery/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void verifyRecoveryCode(@Valid @RequestBody MfaRecoveryCodeFrom from) {
        if (!mfaService.consumeRecoveryCode(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor),
                from.code())) {
            throw new IllegalArgumentException("Recovery Code 无效或已使用");
        }
    }

    @PostMapping("/recovery/rotate")
    @PreAuthorize("isAuthenticated()")
    public List<String> rotateRecoveryCodes() {
        return mfaService.rotateRecoveryCodes(AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor));
    }
}
