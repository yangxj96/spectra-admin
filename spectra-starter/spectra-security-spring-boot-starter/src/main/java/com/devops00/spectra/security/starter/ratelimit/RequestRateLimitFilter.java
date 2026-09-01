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

package com.devops00.spectra.security.starter.ratelimit;

import com.devops00.spectra.common.response.R;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import io.micrometer.core.instrument.Counter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 当前 API 的请求级限流过滤器。
 *
 * <p>过滤器只处理策略目录中明确列出的接口；未命中策略的普通业务请求不进入限流器。
 * 过滤器放在 Token 鉴权之后，因此已认证请求可以按用户维度参与限流。Redis 不可用时直接返回
 * 503，不能降级成本地计数或放行请求。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/1
 */
@NullMarked
@Slf4j
public final class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    private final RedisRateLimiter rateLimiter;

    private final SecurityContextAccessor securityContextAccessor;

    private final ObjectMapper objectMapper;

    private final Counter failClosedCounter;

    /**
     * 创建请求限流过滤器。
     *
     * @param rateLimiter             Redis 限流器
     * @param securityContextAccessor 安全上下文窄端口
     * @param objectMapper            API 响应序列化器
     * @param meterRegistry           指标注册表
     */
    public RequestRateLimitFilter(RedisRateLimiter rateLimiter,
                                  SecurityContextAccessor securityContextAccessor,
                                  ObjectMapper objectMapper,
                                  io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        this.rateLimiter = rateLimiter;
        this.securityContextAccessor = securityContextAccessor;
        this.objectMapper = objectMapper;
        this.failClosedCounter = Counter.builder("security.rate_limit.fail_closed")
                .description("Redis 限流依赖不可用导致请求被拒绝的次数")
                .register(meterRegistry);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = requestPath(request);
        if (isInfrastructureOrStaticPath(path)) {
            return true;
        }
        return RateLimitPolicy.resolve(request.getMethod(), path).isEmpty();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = requestPath(request);
        var policy = RateLimitPolicy.resolve(request.getMethod(), path).orElse(null);
        if (policy == null) {
            chain.doFilter(request, response);
            return;
        }

        String requestId = requestId(request);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            var decision = rateLimiter.tryAcquire(policy, subject(request, policy));
            if (!decision.allowed()) {
                response.setHeader(RETRY_AFTER_HEADER, Long.toString(decision.retryAfterSeconds()));
                writeFailure(response, HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试", requestId,
                        decision.retryAfterSeconds());
                return;
            }
            chain.doFilter(request, response);
        } catch (DataAccessException exception) {
            failClosedCounter.increment();
            log.warn("安全 Redis 限流依赖不可用，拒绝当前请求: {} {}", request.getMethod(), path, exception);
            writeFailure(response, HttpStatus.SERVICE_UNAVAILABLE, "安全限流服务暂不可用", requestId, 0);
        }
    }

    /**
     * 从请求中读取认证用户和客户端地址。
     */
    private RateLimitPolicy.Subject subject(HttpServletRequest request, RateLimitPolicy policy) {
        UUID userId = null;
        if (policy.subjectDimension() != RateLimitPolicy.SubjectDimension.IP) {
            userId = authenticatedUserId();
        }
        return new RateLimitPolicy.Subject(request.getRemoteAddr(), userId == null ? null : userId.toString());
    }

    /**
     * 优先使用已建立的认证上下文，测试和非标准适配场景再通过窄端口读取用户。
     */
    private UUID authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof SecurityUser user) {
            return user.getId();
        }
        return securityContextAccessor.currentUserId();
    }

    /**
     * 生成或复用安全的请求 ID。
     */
    private String requestId(HttpServletRequest request) {
        String supplied = request.getHeader(REQUEST_ID_HEADER);
        return supplied != null && SAFE_REQUEST_ID.matcher(supplied).matches()
                ? supplied
                : UUID.randomUUID().toString();
    }

    /**
     * 输出统一 API 错误结构。
     */
    private void writeFailure(HttpServletResponse response, HttpStatus status, String message, String requestId,
                              long retryAfterSeconds)
            throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        var data = new LinkedHashMap<String, Object>();
        data.put("requestId", requestId);
        if (retryAfterSeconds > 0) {
            data.put("retryAfter", retryAfterSeconds);
        }
        R<Map<String, Object>> body = R.<Map<String, Object>>builder()
                .code(status.value())
                .msg(message)
                .data(data)
                .build();
        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * 去除应用 context path，统一使用 API 路径匹配策略。
     */
    private String requestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        return requestUri.isEmpty() ? "/" : requestUri;
    }

    /**
     * 跳过基础设施探针和静态资源。
     */
    private boolean isInfrastructureOrStaticPath(String path) {
        return "/".equals(path)
                || "/favicon.ico".equals(path)
                || "/index.html".equals(path)
                || path.startsWith("/assets/")
                || path.startsWith("/static/")
                || path.startsWith("/webjars/")
                || "/actuator/health".equals(path)
                || path.startsWith("/actuator/health/")
                || "/actuator/info".equals(path)
                || path.startsWith("/actuator/info/");
    }
}
