/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** 首次 MFA 登记确认请求。 */
public record MfaSetupConfirmFrom(@NotBlank String challengeId, @NotNull UUID enrollmentId,
                                  @NotBlank String code) {
}
