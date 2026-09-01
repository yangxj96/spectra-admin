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

package com.devops00.spectra.framework.configure.mvc.filter;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @AfterEach
    void clearContext() {
        RequestCorrelationContext.clear();
    }

    @Test
    void shouldPropagateValidIdsToResponseMdcAndSharedContext() throws Exception {
        var request = request();
        request.addHeader("X-Request-ID", "request-123");
        request.addHeader("X-Correlation-ID", "correlation-456");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain((servletRequest, servletResponse) -> {
            assertEquals("request-123", servletRequest.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE));
            assertEquals("request-123", response.getHeader("X-Request-ID"));
            assertEquals("correlation-456", response.getHeader("X-Correlation-ID"));
            assertEquals("request-123", org.slf4j.MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
            assertEquals("correlation-456", org.slf4j.MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
            assertEquals("request-123", RequestCorrelationContext.current().requestId());
            assertEquals("correlation-456", RequestCorrelationContext.current().correlationId());
        }));

        assertEquals("request-123", response.getHeader("X-Request-ID"));
        assertEquals("correlation-456", response.getHeader("X-Correlation-ID"));
        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
        assertTrue(RequestCorrelationContext.current().isEmpty());
    }

    @Test
    void shouldRebuildInvalidRequestIdAndNeverExposeIt() throws Exception {
        var request = request();
        request.addHeader("X-Request-ID", "invalid request id");
        request.addHeader("X-Correlation-ID", "invalid/correlation");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain((servletRequest, servletResponse) -> {
            var requestId = RequestCorrelationContext.current().requestId();
            var correlationId = RequestCorrelationContext.current().correlationId();
            assertNotEquals("invalid request id", requestId);
            assertNotEquals("invalid/correlation", correlationId);
            assertEquals(requestId, correlationId);
            assertTrue(isUuid(requestId));
            assertEquals(requestId, response.getHeader("X-Request-ID"));
            assertEquals(requestId, response.getHeader("X-Correlation-ID"));
        }));

        assertNotEquals("invalid request id", response.getHeader("X-Request-ID"));
        assertNotEquals("invalid/correlation", response.getHeader("X-Correlation-ID"));
        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
    }

    @Test
    void shouldClearIdsWhenDownstreamThrows() throws Exception {
        var request = request();
        var response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, chain((servletRequest, servletResponse) -> {
                assertTrue(RequestCorrelationContext.current().requestId() != null);
                throw new IllegalStateException("downstream failure");
            }));
        } catch (IllegalStateException exception) {
            assertEquals("downstream failure", exception.getMessage());
        }

        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.REQUEST_ID_MDC_KEY));
        assertNull(org.slf4j.MDC.get(RequestCorrelationContext.CORRELATION_ID_MDC_KEY));
        assertTrue(RequestCorrelationContext.current().isEmpty());
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/api/test");
    }

    private FilterChain chain(FilterChain chain) {
        return chain;
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
