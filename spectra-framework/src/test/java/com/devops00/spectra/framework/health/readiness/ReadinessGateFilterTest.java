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

package com.devops00.spectra.framework.health.readiness;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ReadinessGateFilter} 的 503 响应契约验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class ReadinessGateFilterTest {

    private static FilterChain chain(AtomicBoolean called) {
        return (request, response) -> called.set(true);
    }

    @Test
    void unreadyReturnsStableErrorCode() throws Exception {
        var state = new DependencyReadinessState();
        state.markUnready("SQLException");
        var response = new MockHttpServletResponse();
        var called = new AtomicBoolean();

        new ReadinessGateFilter(state).doFilter(new MockHttpServletRequest("GET", "/api/security/context"),
                response, chain(called));

        assertEquals(503, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error_code\":\"DEPENDENCY_NOT_READY\""));
        assertFalse(called.get());
    }

    @Test
    void readyRequestPasses() throws Exception {
        var response = new MockHttpServletResponse();
        var called = new AtomicBoolean();

        new ReadinessGateFilter(new DependencyReadinessState()).doFilter(
                new MockHttpServletRequest("GET", "/api/security/context"), response, chain(called));

        assertTrue(called.get());
        assertEquals(200, response.getStatus());
    }

    @Test
    void actuatorProbePassesWhenUnready() throws Exception {
        var state = new DependencyReadinessState();
        state.markUnready("SQLException");
        var response = new MockHttpServletResponse();
        var called = new AtomicBoolean();

        new ReadinessGateFilter(state).doFilter(new MockHttpServletRequest("GET", "/actuator/health"),
                response, chain(called));

        assertTrue(called.get());
        assertEquals(200, response.getStatus());
    }
}
