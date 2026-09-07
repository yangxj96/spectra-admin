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

package com.devops00.spectra.core.security.audit;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.security.audit.archive.SecurityAuditArchiveOrchestrator;
import com.devops00.spectra.core.security.audit.controller.SecurityAuditController;
import com.devops00.spectra.core.security.audit.javabean.from.SecurityAuditQueryFrom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityAuditControllerTest {

    private static final String ARCHIVE_API_VO_NAME = "com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditArchiveManifestVO";

    @Test
    void readAndExportUseCatalogPermissions() throws NoSuchMethodException {
        var page = SecurityAuditController.class.getMethod("page", PageFrom.class,
                SecurityAuditQueryFrom.class, Authentication.class);
        var detail = SecurityAuditController.class.getMethod("detail", UUID.class,
                Authentication.class);
        var export = SecurityAuditController.class.getMethod("export", SecurityAuditQueryFrom.class,
                Authentication.class);

        assertEquals("hasPermission(null, 'audit:read')", page.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null, 'audit:read')", detail.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null, 'audit:export')", export.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void archiveEndpointsExposeApiVoWithoutInstantFields() throws NoSuchMethodException {
        var plan = SecurityAuditController.class.getMethod("planArchive", String.class, String.class, String.class);
        var archive = SecurityAuditController.class.getMethod("archive", UUID.class);
        var retry = SecurityAuditController.class.getMethod("retryArchive", UUID.class);
        var restore = SecurityAuditController.class.getMethod("requestArchiveRestore", UUID.class);
        var verify = SecurityAuditController.class.getMethod("verifyArchive", UUID.class);

        assertAll(
                () -> assertEquals(ARCHIVE_API_VO_NAME, plan.getReturnType().getName()),
                () -> assertEquals(ARCHIVE_API_VO_NAME, archive.getReturnType().getName()),
                () -> assertEquals(ARCHIVE_API_VO_NAME, retry.getReturnType().getName()),
                () -> assertEquals(ARCHIVE_API_VO_NAME, restore.getReturnType().getName()),
                () -> assertEquals(ARCHIVE_API_VO_NAME, verify.getReturnType().getName()),
                () -> assertFalse(Arrays.stream(plan.getReturnType().getDeclaredFields())
                        .anyMatch(field -> field.getType() == Instant.class)));
    }

    @Test
    void archiveWriteEndpointsDeclareSecurityAndAuditContract() throws NoSuchMethodException {
        var methods = new Method[]{
                SecurityAuditController.class.getMethod("planArchive", String.class, String.class, String.class),
                SecurityAuditController.class.getMethod("retryArchive", UUID.class),
                SecurityAuditController.class.getMethod("requestArchiveRestore", UUID.class),
                SecurityAuditController.class.getMethod("verifyArchive", UUID.class)
        };

        for (var method : methods) {
            var mapping = method.getAnnotation(PostMapping.class);
            assertAll(method.getName(),
                    () -> assertNotNull(mapping),
                    () -> assertEquals("1.0.0", mapping.version()),
                    () -> assertNotNull(method.getAnnotation(PreAuthorize.class)),
                    () -> assertNotNull(method.getAnnotation(Audit.class)));
        }
    }

    @Test
    void archiveStatusEndpointDeclaresSecurityAndApiVersion() throws NoSuchMethodException {
        var method = SecurityAuditController.class.getMethod("archive", UUID.class);
        var mapping = method.getAnnotation(GetMapping.class);

        assertAll(
                () -> assertNotNull(mapping),
                () -> assertEquals("1.0.0", mapping.version()),
                () -> assertNotNull(method.getAnnotation(PreAuthorize.class)));
    }

    @Test
    void archiveConverterMapsInternalManifestToApiVo() throws ReflectiveOperationException {
        var converter = Class.forName(
                "com.devops00.spectra.core.security.audit.javabean.converter.SecurityAuditArchiveConverter");
        var method = converter.getMethod("toManifestVO", SecurityAuditArchiveOrchestrator.ManifestView.class);

        assertEquals(ARCHIVE_API_VO_NAME, method.getReturnType().getName());
    }
}
