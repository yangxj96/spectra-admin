/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 文件管理操作请求。 */
@Data
public class FileAdminOperationFrom {

    @NotBlank(message = "幂等键不能为空")
    @Size(max = 128, message = "幂等键不能超过128个字符")
    private String idempotencyKey;

    @NotBlank(message = "操作原因不能为空")
    @Size(max = 500, message = "操作原因不能超过500个字符")
    private String reason;
}
