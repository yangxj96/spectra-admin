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

import java.util.UUID;

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

    public SecuritySessionReaderService(SecuritySessionStore store, SecurityUserLoader securityUserLoader) {
        this.store = store;
        this.securityUserLoader = securityUserLoader;
    }

    @Override
    public @Nullable SecurityPrincipal getCurrentUser() {
        SecurityPrincipal user = getUserFromSecurityContext();
        if (user != null) {
            return user;
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
        Object userIdValue = SecurityRedisExecutor.execute("读取 Session 用户标识",
                () -> store.redis().opsForHash().get(sessionKey, "userId"));
        if (userIdValue == null) {
            return null;
        }
        UUID userId = SecurityRedisValueParser.requiredUuid(userIdValue, "Session.userId");
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
