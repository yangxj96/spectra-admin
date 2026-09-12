/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.core.audit.query.AuditLogPageVO;
import com.devops00.spectra.core.audit.query.AuditLogQueryFrom;
import com.devops00.spectra.core.audit.query.AuditLogVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.jackson.JacksonConfiguration;
import com.devops00.spectra.framework.serialization.jackson.JacksonProperties;
import com.devops00.spectra.framework.web.advice.crypto.ResponseEncryptAdvice;
import com.devops00.spectra.framework.web.advice.crypto.ResponseModifyAdvice;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import com.devops00.spectra.framework.web.filter.RequestGetParamsFilter;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertEquals("occurredAt", detail.getParameters()[1].getAnnotation(RequestParam.class).value(),
                "GET 参数过滤器会把 occurred_at 转换为 occurredAt 再交给 Controller");
    }

    @Test
    void pageEndpointUsesUnifiedResponseAndEncryptionAdvices() throws NoSuchMethodException {
        var page = AuditLogController.class.getMethod("page", PageFrom.class,
                AuditLogQueryFrom.class, Authentication.class);
        var returnType = new MethodParameter(page, -1);
        var converter = JacksonJsonHttpMessageConverter.class;
        var responseModifyAdvice = new ResponseModifyAdvice();
        var cryptoKeyManager = mock(CryptoKeyManager.class);
        when(cryptoKeyManager.isConfiguredEnabled()).thenReturn(true);
        var responseEncryptAdvice = new ResponseEncryptAdvice(cryptoKeyManager, new ObjectMapper());

        assertTrue(responseModifyAdvice.supports(returnType, converter),
                "audit page response should receive the standard {code, msg, data} envelope");
        assertTrue(responseEncryptAdvice.supports(returnType, converter),
                "audit page response should be encrypted when API encryption is enabled");
    }

    @Test
    void auditRowJsonPreservesTheExactPartitionTimestamp() throws Exception {
        Instant occurredAt = Instant.parse("2026-09-13T03:53:16.442Z");
        var builder = JsonMapper.builder();
        new JacksonConfiguration(new JacksonProperties()).jsonMapperBuilderCustomizer().customize(builder);
        var mapper = builder.build();
        var row = new AuditLogVO(UUID.randomUUID(), occurredAt, AuditCategory.OPERATION, "TEST_EVENT",
                UUID.randomUUID(), "张三", null, null, null, null, null, null, null, null, java.util.Map.of(),
                java.util.Map.of(), null, AuditRecord.Result.SUCCEEDED, null, null, null, null);

        String json = mapper.writeValueAsString(row);
        assertTrue(json.contains("\"occurred_at\":\"2026-09-13T03:53:16.442Z\""));
        assertTrue(json.contains("\"operator_name\":\"张三\""));
    }

    @Test
    void getParameterFilterConvertsTheExternalPartitionKeyToControllerParameterName() throws Exception {
        String occurredAt = "2026-09-13T03:53:16.442Z";
        var request = new MockHttpServletRequest("GET", "/api/audit/" + UUID.randomUUID());
        request.addParameter("occurred_at", occurredAt);
        var chain = new MockFilterChain();

        new RequestGetParamsFilter().doFilter(request, new MockHttpServletResponse(), chain);

        var filteredRequest = (jakarta.servlet.http.HttpServletRequest) chain.getRequest();
        assertEquals(occurredAt, filteredRequest.getParameter("occurredAt"));
    }

    private static void assertEndpoint(java.lang.reflect.Method method, String permission) {
        var mapping = method.getAnnotation(GetMapping.class);
        assertNotNull(mapping);
        assertEquals("1.0.0", mapping.version());
        assertEquals(permission, method.getAnnotation(PreAuthorize.class).value());
        assertNotNull(method.getAnnotation(Audit.class));
    }
}
