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

import com.devops00.spectra.core.security.audit.controller.SecurityAuditController;
import com.devops00.spectra.core.security.audit.javabean.from.SecurityAuditQueryFrom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityAuditControllerTest {

    @Test
    void readAndExportUseCatalogPermissions() throws NoSuchMethodException {
        var page = SecurityAuditController.class.getMethod("page", com.devops00.spectra.common.base.javabean.from.PageFrom.class,
                SecurityAuditQueryFrom.class, org.springframework.security.core.Authentication.class);
        var detail = SecurityAuditController.class.getMethod("detail", java.util.UUID.class,
                org.springframework.security.core.Authentication.class);
        var export = SecurityAuditController.class.getMethod("export", SecurityAuditQueryFrom.class,
                org.springframework.security.core.Authentication.class);

        assertEquals("hasPermission(null, 'audit:read')", page.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null, 'audit:read')", detail.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null, 'audit:export')", export.getAnnotation(PreAuthorize.class).value());
    }
}
