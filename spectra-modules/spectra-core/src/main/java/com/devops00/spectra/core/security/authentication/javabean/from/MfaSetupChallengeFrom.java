/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.from;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 首次 MFA 登记挑战请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Data
public class MfaSetupChallengeFrom {

    @NotBlank(message = "MFA 挑战 ID 不能为空")
    private String challengeId;
}
