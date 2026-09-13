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

package com.devops00.spectra.common.context;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Java 25 ScopedValue 上下文契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/9
 */
class ScopedValueContextContractTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void requestCorrelationShouldExposeEmptyContextWhenUnbound() {
        RequestCorrelationContext.Context context = RequestCorrelationContext.current();

        assertTrue(context.isEmpty());
        assertNull(context.requestId());
        assertNull(context.correlationId());
    }

    @Test
    void requestCorrelationShouldRestoreNestedContextAndMdc() throws Exception {
        RequestCorrelationContext.Context outer = RequestCorrelationContext.forHttp("request-outer", "correlation-outer");
        RequestCorrelationContext.Context inner = RequestCorrelationContext.forHttp("request-inner", "correlation-inner");

        RequestCorrelationContext.callWithMdc(outer, () -> {
            assertContextAndMdc("request-outer", "correlation-outer");
            RequestCorrelationContext.callWithMdc(inner, () -> {
                assertContextAndMdc("request-inner", "correlation-inner");
                return null;
            });
            assertContextAndMdc("request-outer", "correlation-outer");
            return null;
        });

        assertTrue(RequestCorrelationContext.current().isEmpty());
        assertNull(MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertNull(MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void requestCorrelationShouldRestoreContextAndMdcAfterFailure() {
        RequestCorrelationContext.Context context = RequestCorrelationContext.forHttp("request-failure", "correlation-failure");

        assertThrows(IllegalStateException.class, () -> RequestCorrelationContext.callWithMdc(context, () -> {
            assertContextAndMdc("request-failure", "correlation-failure");
            throw new IllegalStateException("context failure");
        }));

        assertTrue(RequestCorrelationContext.current().isEmpty());
        assertNull(MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertNull(MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void requestCorrelationShouldRestoreExistingMdcValues() {
        MDC.put(RequestCorrelationContext.REQUEST_ID_MDC_KEY, "previous-request");
        MDC.put(RequestCorrelationContext.CORRELATION_ID_MDC_KEY, "previous-correlation");

        RequestCorrelationContext.runWithMdc(
                RequestCorrelationContext.forHttp("request-current", "correlation-current"),
                () -> assertContextAndMdc("request-current", "correlation-current"));

        assertEquals("previous-request", MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertEquals("previous-correlation", MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void taskCorrelationShouldNotInventRequestId() throws Exception {
        RequestCorrelationContext.Context context = RequestCorrelationContext.callTask("task-123", () -> {
            RequestCorrelationContext.Context current = RequestCorrelationContext.current();
            assertNull(current.requestId());
            assertEquals("task-123", current.correlationId());
            assertNull(MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
            assertEquals("task-123", MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
            return current;
        });

        assertNull(context.requestId());
        assertEquals("task-123", context.correlationId());
        assertTrue(RequestCorrelationContext.current().isEmpty());
    }

    @Test
    void virtualTaskContextsShouldNotLeakAcrossConcurrentTasks() throws Exception {
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> RequestCorrelationContext.callTask("task-a", () -> {
                entered.countDown();
                await(release);
                return new ContextSnapshot(RequestCorrelationContext.current().requestId(),
                        RequestCorrelationContext.current().correlationId(),
                        MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY),
                        MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
            }));
            var second = executor.submit(() -> RequestCorrelationContext.callTask("task-b", () -> {
                entered.countDown();
                await(release);
                return new ContextSnapshot(RequestCorrelationContext.current().requestId(),
                        RequestCorrelationContext.current().correlationId(),
                        MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY),
                        MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
            }));

            assertTrue(entered.await(5, TimeUnit.SECONDS));
            release.countDown();
            ContextSnapshot firstContext = first.get(5, TimeUnit.SECONDS);
            ContextSnapshot secondContext = second.get(5, TimeUnit.SECONDS);
            assertNull(firstContext.requestId());
            assertEquals("task-a", firstContext.correlationId());
            assertNull(firstContext.requestMdc());
            assertEquals("task-a", firstContext.correlationMdc());
            assertNull(secondContext.requestId());
            assertEquals("task-b", secondContext.correlationId());
            assertNull(secondContext.requestMdc());
            assertEquals("task-b", secondContext.correlationMdc());
        }

        assertTrue(RequestCorrelationContext.current().isEmpty());
    }

    @Test
    void dataScopeShouldBindRequestAndNestedBypassDepthLexically() throws Exception {
        assertFalse(DataScopeContextHolder.isBypassed());

        DataScopeContextHolder.callWithRequest(() -> {
            assertFalse(DataScopeContextHolder.isBypassed());
            assertTrue(DataScopeContextHolder.withBypass(() -> {
                assertTrue(DataScopeContextHolder.isBypassed());
                assertTrue(DataScopeContextHolder.withBypass(
                        (java.util.function.Supplier<Boolean>) DataScopeContextHolder::isBypassed));
                assertTrue(DataScopeContextHolder.isBypassed());
                return true;
            }));
            assertFalse(DataScopeContextHolder.isBypassed());
            return null;
        });

        assertFalse(DataScopeContextHolder.isBypassed());
        DataScopeContextHolder.withBypass(() -> assertTrue(DataScopeContextHolder.isBypassed()));
        assertFalse(DataScopeContextHolder.isBypassed());
    }

    @Test
    void dataScopeShouldRestoreAfterFailure() {
        assertThrows(IllegalStateException.class, () -> DataScopeContextHolder.callWithRequest(() -> {
            DataScopeContextHolder.withBypass(() -> {
                throw new IllegalStateException("scope failure");
            });
            return null;
        }));

        assertFalse(DataScopeContextHolder.isBypassed());
    }

    @Test
    void virtualRequestScopesShouldNotLeakAcrossConcurrentTasks() throws Exception {
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> DataScopeContextHolder.callWithRequest(() -> {
                entered.countDown();
                await(release);
                assertFalse(DataScopeContextHolder.isBypassed());
                return DataScopeContextHolder.withBypass(
                        (java.util.function.Supplier<Boolean>) DataScopeContextHolder::isBypassed);
            }));
            var second = executor.submit(() -> DataScopeContextHolder.callWithRequest(() -> {
                entered.countDown();
                await(release);
                assertFalse(DataScopeContextHolder.isBypassed());
                return DataScopeContextHolder.withBypass(
                        (java.util.function.Supplier<Boolean>) DataScopeContextHolder::isBypassed);
            }));

            assertTrue(entered.await(5, TimeUnit.SECONDS));
            release.countDown();
            assertTrue(first.get(5, TimeUnit.SECONDS));
            assertTrue(second.get(5, TimeUnit.SECONDS));
        }

        assertFalse(DataScopeContextHolder.isBypassed());
    }

    @Test
    void contextHoldersShouldNotRetainThreadLocalOrLegacyScopeApis() {
        assertFalse(hasThreadLocalField(RequestCorrelationContext.class));
        assertFalse(hasThreadLocalField(DataScopeContextHolder.class));
        assertFalse(hasMethodNamed(RequestCorrelationContext.class, "open"));
        assertFalse(hasMethodNamed(RequestCorrelationContext.class, "openWithMdc"));
        assertFalse(hasMethodNamed(RequestCorrelationContext.class, "openTask"));
        assertFalse(hasMethodNamed(DataScopeContextHolder.class, "beginRequest"));
        assertFalse(hasMethodNamed(DataScopeContextHolder.class, "endRequest"));
        assertFalse(Arrays.stream(RequestCorrelationContext.class.getDeclaredClasses())
                .anyMatch(type -> type.getSimpleName().equals("Scope")));
    }

    /**
     * 处理上下文MDC相关数据。
     */
    private static void assertContextAndMdc(String requestId, String correlationId) {
        assertEquals(requestId, RequestCorrelationContext.current().requestId());
        assertEquals(correlationId, RequestCorrelationContext.current().correlationId());
        assertEquals(requestId, MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertEquals(correlationId, MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
    }

    /**
     * 等待值上下文合同完成。
     */
    private static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * 判断本地。
     */
    private static boolean hasThreadLocalField(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(Field::getType).anyMatch(ThreadLocal.class::equals);
    }

    /**
     * 判断方法。
     */
    private static boolean hasMethodNamed(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods()).map(Method::getName).anyMatch(name::equals);
    }

    /**
     * 为 {@code ScopedValueContextContractTest} 测试提供 {@code ContextSnapshot} 测试类型。
     *
     * @param requestId      请求标识
     * @param correlationId  关联标识
     * @param requestMdc     请求MDC
     * @param correlationMdc 关联MDC
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record ContextSnapshot(String requestId, String correlationId, String requestMdc, String correlationMdc) {

        private ContextSnapshot {
            assertNotNull(correlationId);
            assertNotNull(correlationMdc);
        }
    }
}
