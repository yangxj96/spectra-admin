/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditAspectContractTest {

    @Test
    void aspectMustSubmitAuditRecordWithoutLegacyLogAnnotation() throws NoSuchMethodException {
        Method advice = AuditAspect.class.getDeclaredMethod("handleAround", ProceedingJoinPoint.class);
        String pointcut = advice.getAnnotation(Around.class).value();

        assertTrue(pointcut.contains("com.devops00.spectra.common.audit.Audit"));
        assertTrue(!pointcut.contains("ULog"));
        assertEquals(4, AuditAspect.class.getDeclaredConstructors()[0].getParameterCount());
    }

    @Test
    void explicitSecurityAuditMustReachUnifiedService() throws Throwable {
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        AuditService auditService = recorded::set;
        AuditSanitizer sanitizer = snapshot -> Map.of("sanitized", true);
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), auditService, sanitizer,
                transactionOperations());
        Method method = Fixture.class.getDeclaredMethod("explicitSecurityAudit");

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[0]);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[0]);
        when(point.proceed()).thenReturn("ok");

        aspect.handleAround(point);

        assertEquals(AuditCategory.SECURITY, recorded.get().category());
        assertEquals("SECURITY_PROFILE_CHANGED", recorded.get().eventType());
    }

    @Test
    void operationAuditFailureMustPropagateToRollbackOwningTransaction() throws Throwable {
        AuditService auditService = record -> {
            throw new AuditService.AuditRecordingException("operation outbox unavailable");
        };
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), auditService,
                snapshot -> Map.of("sanitized", true), transactionOperations());
        Method method = Fixture.class.getDeclaredMethod("explicitOperationAudit");

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[0]);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[0]);
        when(point.proceed()).thenReturn("ok");

        assertThrows(AuditService.AuditRecordingException.class, () -> aspect.handleAround(point));
    }

    @Test
    void shouldUseSanitizedRequestContextInsteadOfRawHeaders() throws Throwable {
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), recorded::set,
                snapshot -> Map.of("sanitized", true), transactionOperations());
        Method method = Fixture.class.getDeclaredMethod("explicitOperationAudit");
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[0]);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[0]);
        when(point.proceed()).thenReturn("ok");

        var request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader(RequestCorrelationContext.REQUEST_ID_HEADER, "invalid request id");
        request.addHeader(RequestCorrelationContext.CORRELATION_ID_HEADER, "correlation-456");
        var response = new MockHttpServletResponse();
        var attributes = new org.springframework.web.context.request.ServletRequestAttributes(request, response);
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
        try {
            aspect.handleAround(point);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
            RequestCorrelationContext.clear();
        }

        var context = recorded.get().context();
        assertTrue(!"invalid request id".equals(context.requestId()));
        assertEquals("correlation-456", context.correlationId());
    }

    private static TransactionOperations transactionOperations() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(new SimpleTransactionStatus());
            }
        };
    }

    static class Fixture {

        @Audit(category = AuditCategory.SECURITY, eventType = "SECURITY_PROFILE_CHANGED")
        String explicitSecurityAudit() {
            return "ok";
        }

        @Audit(category = AuditCategory.OPERATION, eventType = "USER.UPDATE")
        String explicitOperationAudit() {
            return "ok";
        }
    }
}
