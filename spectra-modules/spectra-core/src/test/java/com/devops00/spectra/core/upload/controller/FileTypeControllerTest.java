/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.upload.javabean.from.FileTypePolicySaveFrom;
import com.devops00.spectra.core.upload.service.FileTypeManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 文件类型策略管理接口契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
