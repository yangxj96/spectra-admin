/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * 首次 MFA 登记确认请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Data
public class MfaSetupConfirmFrom {

    @NotBlank(message = "MFA 挑战 ID 不能为空")
    private String challengeId;

    @NotNull(message = "MFA 登记 ID 不能为空")
    private UUID enrollmentId;

    @NotBlank(message = "MFA 验证码不能为空")
    private String code;
}
