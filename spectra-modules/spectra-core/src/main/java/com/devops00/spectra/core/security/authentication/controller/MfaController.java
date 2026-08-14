/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.controller;

import com.devops00.spectra.core.security.authentication.MfaEnrollmentResult;
import com.devops00.spectra.core.security.authentication.MfaService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
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

    public MfaController(MfaService mfaService, SecurityContextAccessor securityContextAccessor) {
        this.mfaService = mfaService;
        this.securityContextAccessor = securityContextAccessor;
    }

    @PostMapping("/totp/enroll")
    @PreAuthorize("isAuthenticated()")
    public MfaEnrollmentResult beginTotpEnrollment() {
        return mfaService.beginTotpEnrollment(currentUserId());
    }

    @PostMapping("/totp/confirm")
    @PreAuthorize("isAuthenticated()")
    public List<String> confirmTotpEnrollment(@Valid @RequestBody ConfirmFrom from) {
        return mfaService.confirmTotpEnrollment(currentUserId(), from.enrollmentId(), from.code());
    }

    @PostMapping("/recovery/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void verifyRecoveryCode(@Valid @RequestBody RecoveryCodeFrom from) {
        if (!mfaService.consumeRecoveryCode(currentUserId(), from.code())) {
            throw new IllegalArgumentException("Recovery Code 无效或已使用");
        }
    }

    private UUID currentUserId() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前请求没有有效用户");
        }
        return userId;
    }

    public record ConfirmFrom(@NotNull UUID enrollmentId, @NotBlank String code) {
    }

    public record RecoveryCodeFrom(@NotBlank String code) {
    }
}
