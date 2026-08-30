/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.controller;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.upload.javabean.from.FileTypePolicySaveFrom;
import com.devops00.spectra.upload.service.FileTypeManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** 文件类型策略管理接口契约测试。 */
class FileTypeControllerTest {

    @Test
    void readAndWriteEndpointsUseSeparatedPermissions() throws Exception {
        assertThat(FileTypeController.class.getMethod("page", PageFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:read')");
        assertThat(FileTypeController.class.getMethod("get", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:read')");
        assertThat(FileTypeController.class.getMethod("create", FileTypePolicySaveFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:manage')");
        assertThat(FileTypeController.class.getMethod("modify", UUID.class, FileTypePolicySaveFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:manage')");
        assertThat(FileTypeController.class.getMethod("enable", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:manage')");
        assertThat(FileTypeController.class.getMethod("disable", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:manage')");
    }

    @Test
    void controllerIsConstructibleWithManagementService() {
        assertThat(new FileTypeController(mock(FileTypeManagementService.class))).isNotNull();
    }
}
