/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 文件类型策略管理服务契约测试。 */
class FileTypeManagementServiceTest {

    @Test
    void managementServiceMustExposePolicyLifecycle() {
        assertThat(FileTypeManagementService.class.getDeclaredMethods())
                .extracting(java.lang.reflect.Method::getName)
                .contains("page", "get", "create", "modify", "enable", "disable");
    }
}
