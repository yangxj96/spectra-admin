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
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.core.security.authorization.controller.AuthorizationController;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditAspectContractTest {

    @Test
    void aspectMustSubmitAuditRecordWithoutLegacyLogAnnotation() throws NoSuchMethodException {
        Method advice = AuditAspect.class.getDeclaredMethod("handleAround", ProceedingJoinPoint.class);
        String pointcut = advice.getAnnotation(Around.class).value();

        assertTrue(pointcut.contains("com.devops00.spectra.common.audit.Audit"));
        assertTrue(!pointcut.contains("ULog"));
        assertEquals(6, AuditAspect.class.getDeclaredConstructors()[0].getParameterCount());
    }

    @Test
    void explicitSecurityAuditMustReachUnifiedService() throws Throwable {
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        AuditService auditService = recorded::set;
        AuditSanitizer sanitizer = snapshot -> Map.of("sanitized", true);
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), auditService, sanitizer,
                transactionOperations(), new AuditFailureResolver(new DefaultAuditSanitizer()),
                mock(AuditFailureRecorder.class));
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
    void quotedAuditValueMustBeStoredAsItsResolvedDescription() throws Throwable {
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), recorded::set,
                new DefaultAuditSanitizer(), transactionOperations(),
                new AuditFailureResolver(new DefaultAuditSanitizer()), mock(AuditFailureRecorder.class));

        aspect.handleAround(point(Fixture.class.getDeclaredMethod("auditWithQuotedDescription")));

        assertEquals("读取用户详情", recorded.get().reason());
    }

    @Test
    void operationAuditFailureMustPropagateToRollbackOwningTransaction() throws Throwable {
        AuditService auditService = record -> {
            throw new AuditService.AuditRecordingException("operation outbox unavailable");
        };
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), auditService,
                snapshot -> Map.of("sanitized", true), transactionOperations(),
                new AuditFailureResolver(new DefaultAuditSanitizer()), mock(AuditFailureRecorder.class));
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
                snapshot -> Map.of("sanitized", true), transactionOperations(),
                new AuditFailureResolver(new DefaultAuditSanitizer()), mock(AuditFailureRecorder.class));
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
        }

        var context = recorded.get().context();
        assertTrue(!"invalid request id".equals(context.requestId()));
        assertEquals("correlation-456", context.correlationId());
    }

    @Test
    void sensitiveAuditCanExcludeArgumentsAndResultSnapshots() throws Throwable {
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), recorded::set,
                snapshot -> new java.util.LinkedHashMap<>(snapshot), transactionOperations(),
                new AuditFailureResolver(new DefaultAuditSanitizer()), mock(AuditFailureRecorder.class));
        Method method = Fixture.class.getDeclaredMethod("sensitiveAudit", String.class);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[]{"value"});
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[]{"sensitive-value"});
        when(point.proceed()).thenReturn("sensitive-result");

        aspect.handleAround(point);

        assertTrue(!recorded.get().before().containsKey("arguments"));
        assertTrue(!recorded.get().after().containsKey("result"));
    }

    @Test
    void generatedAuditEventTypeMustFitPersistenceLimit() throws Exception {
        Method auditedMethod = AuthorizationController.class.getDeclaredMethod(
                "departmentCreatePreview", OrganizationChangeFrom.class);
        Method resolver = AuditAspect.class.getDeclaredMethod(
                "resolveDescriptor", Method.class, ProceedingJoinPoint.class);
        assertTrue(resolver.trySetAccessible());

        Object descriptor = resolver.invoke(new AuditAspect(null, null, null, null, null, null), auditedMethod, null);
        Method eventType = descriptor.getClass().getDeclaredMethod("eventType");
        assertTrue(eventType.trySetAccessible());

        assertEquals("AuthorizationController#departmentCreatePreview", eventType.invoke(descriptor));
        assertTrue(((String) eventType.invoke(descriptor)).length() <= 100);
    }

    @Test
    void failedBusinessCallMustBeRecordedAfterRollbackAndKeepTheOriginalException() throws Throwable {
        AtomicBoolean rollbackComplete = new AtomicBoolean();
        AtomicReference<AuditRecord> recorded = new AtomicReference<>();
        TransactionOperations transactions = rollbackTransactionOperations(rollbackComplete);
        AuditFailureRecorder recorder = mock(AuditFailureRecorder.class);
        doAnswer(invocation -> {
            assertTrue(rollbackComplete.get());
            recorded.set(invocation.getArgument(0));
            return null;
        }).when(recorder).record(org.mockito.ArgumentMatchers.any(AuditRecord.class));
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), record -> {
            throw new AssertionError("thrown business call must use the failure recorder");
        }, new DefaultAuditSanitizer(), transactions,
                new AuditFailureResolver(new DefaultAuditSanitizer()), recorder);
        Method method = Fixture.class.getDeclaredMethod("explicitOperationAudit");
        IllegalStateException original = new IllegalStateException("business failure");
        ProceedingJoinPoint point = point(method);
        when(point.proceed()).thenThrow(original);

        Throwable thrown = assertThrows(IllegalStateException.class, () -> aspect.handleAround(point));

        assertSame(original, thrown);
        assertEquals(AuditRecord.Result.FAILED, recorded.get().result());
        assertEquals("IllegalStateException", recorded.get().failure().type());
        assertEquals("操作执行失败", recorded.get().failure().reason());
        verify(recorder).record(org.mockito.ArgumentMatchers.any(AuditRecord.class));
    }

    @Test
    void failureRecorderErrorMustBeSuppressedOnTheOriginalBusinessException() throws Throwable {
        AuditFailureRecorder recorder = mock(AuditFailureRecorder.class);
        doThrow(new AuditService.AuditRecordingException("writer unavailable"))
                .when(recorder).record(org.mockito.ArgumentMatchers.any(AuditRecord.class));
        TransactionOperations transactions = rollbackTransactionOperations(new AtomicBoolean());
        AuditAspect aspect = new AuditAspect(mock(SecurityContextAccessor.class), record -> {
            throw new AssertionError("thrown business call must use the failure recorder");
        }, new DefaultAuditSanitizer(), transactions,
                new AuditFailureResolver(new DefaultAuditSanitizer()), recorder);
        Method method = Fixture.class.getDeclaredMethod("explicitOperationAudit");
        IllegalStateException original = new IllegalStateException("business failure");
        ProceedingJoinPoint point = point(method);
        when(point.proceed()).thenThrow(original);

        Throwable thrown = assertThrows(IllegalStateException.class, () -> aspect.handleAround(point));

        assertSame(original, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertTrue(thrown.getSuppressed()[0] instanceof AuditService.AuditRecordingException);
    }

    private static ProceedingJoinPoint point(Method method) {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[0]);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[0]);
        return point;
    }

    private static TransactionOperations rollbackTransactionOperations(AtomicBoolean rollbackComplete) {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                try {
                    return action.doInTransaction(new SimpleTransactionStatus());
                } catch (RuntimeException failure) {
                    rollbackComplete.set(true);
                    throw failure;
                }
            }
        };
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

        @Audit(captureArguments = false, captureResult = false)
        String sensitiveAudit(String value) {
            return value;
        }

        @Audit(value = "'读取用户详情'", eventType = "USER_DETAIL_READ")
        String auditWithQuotedDescription() {
            return "ok";
        }
    }
}
