/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.upload.javabean.from.FileAdminOperationFrom;
import com.devops00.spectra.core.upload.javabean.from.FileUploadAdminPageRequest;
import com.devops00.spectra.core.upload.service.FileUploadAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** 文件上传管理接口契约测试。 */
class FileUploadAdminControllerTest {

    @Test
    void adminEndpointsExposeDedicatedReadAndManagePermissions() throws Exception {
        assertThat(FileUploadAdminController.class.getMethod("page", PageFrom.class, FileUploadAdminPageRequest.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:read')");
        assertThat(FileUploadAdminController.class.getMethod("detail", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:read')");
        assertThat(FileUploadAdminController.class.getMethod("cancel", UUID.class, FileAdminOperationFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value()).isEqualTo("hasPermission(null, 'file:admin:manage')");
    }

    @Test
    void controllerIsConstructibleWithAdminService() {
        assertThat(new FileUploadAdminController(mock(FileUploadAdminService.class))).isNotNull();
    }
}
