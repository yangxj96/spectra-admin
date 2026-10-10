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

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import com.devops00.spectra.framework.security.session.query.SecuritySessionReader;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;
import java.time.Clock;
import java.time.Duration;

/**
 * 当前安全 Session 读取用例，隔离 Servlet 上下文和 Redis 身份事实源。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@Component
@NullMarked
public class SecuritySessionReaderService implements SecuritySessionReader, SecurityTokenAccessor {

    private final SecuritySessionStore store;

    private final SecurityUserLoader securityUserLoader;

    private final Clock clock;

    public SecuritySessionReaderService(SecuritySessionStore store, SecurityUserLoader securityUserLoader, Clock clock) {
        this.store = store;
        this.securityUserLoader = securityUserLoader;
        this.clock = clock;
    }

    @Override
    public @Nullable SecurityPrincipal getCurrentUser() {
        SecurityPrincipal user = getUserFromSecurityContext();
        if (user != null) {
            String contextToken = getTokenFromSecurityContext();
            return contextToken == null || contextToken.isBlank() ? user : getCurrentUser(contextToken);
        }
        String token = getTokenFromHttpRequest();
        return token == null ? null : getCurrentUser(token);
    }

    @Override
    public @Nullable SecurityPrincipal getCurrentUser(String token) {
        return SecurityRedisExecutor.execute("读取安全会话主体", () -> getCurrentUserInternal(token));
    }

    /**
     * 查询当前用户内部。
     */
    private @Nullable SecurityPrincipal getCurrentUserInternal(String token) {
        String tokenDigest = TokenDigestService.digest(token);
        String sessionKey = SecurityRedisKey.SESSION.format(tokenDigest);
        Map<Object, Object> session = store.hash("读取待认证安全会话", sessionKey);
        if (session.isEmpty()) {
            return null;
        }
        UUID userId = SecurityRedisValueParser.requiredUuid(session.get("userId"), "Session.userId");
        String familyId = SecurityRedisValueParser.requiredText(session.get("familyId"), "Session.familyId");
        String clientType = SecurityRedisValueParser.requiredText(session.get("clientType"), "Session.clientType");
        SessionPolicy policy = store.sessionPolicy(clientType);
        SecuritySessionLifetime lifetime = SecuritySessionLifetime.from(session);
        long now = clock.millis();
        long accessDeadline = SecurityRedisValueParser.requiredLong(session.get("accessExpiresAt"),
                "Session.accessExpiresAt");
        if (lifetime.expired(policy, now) || accessDeadline <= now) {
            return null;
        }
        if (store.hasKey("检查 Token Family 撤销围栏", SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId))) {
            return null;
        }
        String accessRefreshKey = SecurityRedisKey.REFRESH_TOKEN.format(tokenDigest);
        Object refreshValue = store.value("读取 Access Refresh 映射", accessRefreshKey);
        String refreshDigest = null;
        Duration refreshTtl = Duration.ZERO;
        if (refreshValue != null) {
            refreshDigest = SecurityRedisValueParser.requiredText(refreshValue, "Access.refreshDigest");
            String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
            Map<Object, Object> refresh = store.hash("读取当前 Refresh 状态", refreshKey);
            if (refresh.isEmpty()) {
                return null;
            }
            SecuritySessionLifetime refreshLifetime = SecuritySessionLifetime.from(refresh);
            long refreshDeadline = SecurityRedisValueParser.requiredLong(refresh.get("refreshExpiresAt"),
                    "Refresh.refreshExpiresAt");
            if (refreshLifetime.expired(policy, now)
                    || refreshDeadline <= now
                    || refreshLifetime.loginTime() != lifetime.loginTime()) {
                return null;
            }
            refreshTtl = lifetime.touch(now).until(refreshDeadline, policy, now);
        }
        var activity = new SecuritySessionActivityStore.Activity(tokenDigest, familyId, refreshDigest, now,
                lifetime.touch(now).until(accessDeadline, policy, now), refreshTtl);
        if (!SecuritySessionActivityStore.touch(store.redis(), activity)) {
            return null;
        }
        return securityUserLoader.load(userId);
    }

    @Override
    public @Nullable String getCurrentToken() {
        String token = getTokenFromSecurityContext();
        return token == null || token.isBlank() ? getTokenFromHttpRequest() : token;
    }

    /**
     * 查询令牌安全上下文。
     */
    private @Nullable String getTokenFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object credentials = authentication.getCredentials();
        return credentials instanceof String token ? token : null;
    }

    /**
     * 查询令牌HTTP请求。
     */
    private @Nullable String getTokenFromHttpRequest() {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return null;
        }
        String bearer = request.getHeader("authorization");
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return null;
        }
        return bearer.substring(7);
    }

    /**
     * 查询用户安全上下文。
     */
    private @Nullable SecurityPrincipal getUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof SecurityPrincipal user ? user : null;
    }

    /**
     * 查询HTTP请求。
     */
    private @Nullable HttpServletRequest getHttpServletRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletAttributes ? servletAttributes.getRequest() : null;
    }
}
