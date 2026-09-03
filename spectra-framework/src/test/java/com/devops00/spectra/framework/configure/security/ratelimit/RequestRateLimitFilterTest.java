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

package com.devops00.spectra.framework.configure.security.ratelimit;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 请求级限流过滤器行为测试。
 */
class RequestRateLimitFilterTest {

    private final SecurityContextAccessor contextAccessor = mock();

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturn429AndKeepRequestIdWhenLimitIsExceeded() throws Exception {
        var limiter = limiterReturning(11, 1_500);
        var filter = filter(limiter);
        var request = request("POST", "/api/security/authentication/login", "192.0.2.10");
        request.addHeader("X-Request-Id", "request-123");
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        assertEquals("2", response.getHeader("Retry-After"));
        assertEquals("request-123", response.getHeader("X-Request-Id"));
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("请求过于频繁"));
        assertTrue(response.getContentAsString().contains("request-123"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldContinueAllowedRequestAndIgnoreForwardedForHeader() throws Exception {
        var calls = new AtomicInteger();
        var keys = new ArrayList<String>();
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            calls.incrementAndGet();
            keys.add(key);
            assertTrue(key.startsWith("sec:ratelimit:v1:authentication-login:"));
            return new RedisRateLimiter.IncrementResult(1, ttlMillis);
        }, fixedClock());
        var filter = filter(limiter);
        var request = request("POST", "/api/security/authentication/login", "192.0.2.10");
        request.addHeader("X-Forwarded-For", "198.51.100.25");
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        var forwardedRequest = request("POST", "/api/security/authentication/login", "192.0.2.10");
        forwardedRequest.addHeader("X-Forwarded-For", "203.0.113.77");
        var forwardedResponse = new MockHttpServletResponse();
        filter.doFilter(forwardedRequest, forwardedResponse, chain);

        assertEquals(2, calls.get());
        assertEquals(keys.get(0), keys.get(1));
        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
        verify(chain).doFilter(forwardedRequest, forwardedResponse);
    }

    @Test
    void shouldSkipHealthAndStaticResourceWithoutTouchingRedis() throws Exception {
        var calls = new AtomicInteger();
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            calls.incrementAndGet();
            return new RedisRateLimiter.IncrementResult(1, ttlMillis);
        }, fixedClock());
        var filter = filter(limiter);
        var chain = mock(jakarta.servlet.FilterChain.class);

        var healthRequest = request("GET", "/api/actuator/health", "192.0.2.10");
        var healthResponse = new MockHttpServletResponse();
        filter.doFilter(healthRequest, healthResponse, chain);

        var staticRequest = request("GET", "/api/assets/app.js", "192.0.2.10");
        var staticResponse = new MockHttpServletResponse();
        filter.doFilter(staticRequest, staticResponse, chain);

        assertEquals(0, calls.get());
        verify(chain).doFilter(healthRequest, healthResponse);
        verify(chain).doFilter(staticRequest, staticResponse);
    }

    @Test
    void shouldReturn503WhenRedisIsUnavailable() throws Exception {
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            throw new RedisConnectionFailureException("redis unavailable");
        }, fixedClock());
        var meterRegistry = new SimpleMeterRegistry();
        var filter = filter(limiter, meterRegistry);
        var request = request("POST", "/api/security/authentication/login", "192.0.2.10");
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("安全限流服务暂不可用"));
        assertTrue(response.getHeader("X-Request-Id") != null);
        assertEquals(1.0, meterRegistry.get("security.rate_limit.fail_closed").counter().count());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldRateLimitWhitelistedProviderCallbackInsteadOfBypassingTheFilter() throws Exception {
        var calls = new AtomicInteger();
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            calls.incrementAndGet();
            assertTrue(key.startsWith("sec:ratelimit:v1:notification-provider-callback:"));
            return new RedisRateLimiter.IncrementResult(1, ttlMillis);
        }, fixedClock());
        var filter = filter(limiter);
        var request = request("POST", "/api/notification/provider/callback/sms", "192.0.2.10");
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(1, calls.get());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldUseAuthenticatedUserForUserDimension() throws Exception {
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(contextAccessor.currentUserId()).thenReturn(userId);
        var capturedKey = new String[1];
        var limiter = new RedisRateLimiter((key, ttlMillis) -> {
            capturedKey[0] = key;
            return new RedisRateLimiter.IncrementResult(1, ttlMillis);
        }, fixedClock());
        var filter = filter(limiter);
        var request = request("GET", "/api/service/monitor/diagnostics/runtime", "192.0.2.10");
        var response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertTrue(capturedKey[0].startsWith("sec:ratelimit:v1:service-diagnostic:"));
        assertTrue(capturedKey[0].contains(":") && !capturedKey[0].contains(userId.toString()));
        verify(chain).doFilter(request, response);
    }

    private RequestRateLimitFilter filter(RedisRateLimiter limiter) {
        return filter(limiter, new SimpleMeterRegistry());
    }

    private RequestRateLimitFilter filter(RedisRateLimiter limiter, SimpleMeterRegistry meterRegistry) {
        return new RequestRateLimitFilter(limiter, contextAccessor, new ObjectMapper(), meterRegistry);
    }

    private RedisRateLimiter limiterReturning(long count, long ttlMillis) {
        return new RedisRateLimiter((key, ignoredTtlMillis) -> new RedisRateLimiter.IncrementResult(count, ttlMillis), fixedClock());
    }

    private static MockHttpServletRequest request(String method, String uri, String remoteAddr) {
        var request = new MockHttpServletRequest(method, uri);
        request.setContextPath("/api");
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-09-01T00:00:30Z"), ZoneOffset.UTC);
    }
}
