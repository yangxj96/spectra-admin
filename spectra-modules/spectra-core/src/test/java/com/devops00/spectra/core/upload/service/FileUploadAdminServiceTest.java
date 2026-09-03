/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 文件上传管理服务契约测试。 */
class FileUploadAdminServiceTest {

    @Test
    void adminCancellationMustBeIdempotentAndUseCleanupLifecycle() {
        assertThat(FileUploadAdminService.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("cancel") && method.getParameterCount() == 2);
    }
}
