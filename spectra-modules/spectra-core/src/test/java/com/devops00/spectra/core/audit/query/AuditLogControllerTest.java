/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLogControllerTest {

    @Test
    void endpointsUseUnifiedPathVersionAuditAndCatalogPermissions() throws NoSuchMethodException {
        assertEquals("/audit", AuditLogController.class.getAnnotation(
                org.springframework.web.bind.annotation.RequestMapping.class).value()[0]);

        var page = AuditLogController.class.getMethod("page", PageFrom.class,
                AuditLogQueryFrom.class, Authentication.class);
        var detail = AuditLogController.class.getMethod("detail", UUID.class, Instant.class, Authentication.class);
        var export = AuditLogController.class.getMethod("export", AuditLogQueryFrom.class, Authentication.class);

        assertEndpoint(page, "hasPermission(null, 'audit:read')");
        assertEndpoint(detail, "hasPermission(null, 'audit:read')");
        assertEndpoint(export, "hasPermission(null, 'audit:export')");
        assertEquals(AuditLogPageVO.class, page.getReturnType());
        assertEquals(AuditLogVO.class, detail.getReturnType());
    }

    private static void assertEndpoint(java.lang.reflect.Method method, String permission) {
        var mapping = method.getAnnotation(GetMapping.class);
        assertNotNull(mapping);
        assertEquals("1.0.0", mapping.version());
        assertEquals(permission, method.getAnnotation(PreAuthorize.class).value());
        assertNotNull(method.getAnnotation(Audit.class));
    }
}
