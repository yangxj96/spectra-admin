/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** 首次初始化的 TOTP 确认参数。 */
@Data
public class SystemInitializationMfaConfirmFrom {

    @NotBlank
    private String initializationId;

    @NotNull
    private UUID enrollmentId;

    @NotBlank
    private String code;
}
