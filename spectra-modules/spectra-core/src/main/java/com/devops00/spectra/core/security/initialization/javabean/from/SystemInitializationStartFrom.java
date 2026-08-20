/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 首次初始化的 DEV_OPS 账号参数。 */
@Data
public class SystemInitializationStartFrom {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(min = 12, max = 128)
    private String password;

    @Size(max = 50)
    private String realName;
}
