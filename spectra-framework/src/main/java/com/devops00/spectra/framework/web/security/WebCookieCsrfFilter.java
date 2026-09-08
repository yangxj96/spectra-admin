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

package com.devops00.spectra.framework.web.security;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Web Cookie 双提交 CSRF 过滤器。
 *
 * <p>只拦截带 Web Cookie 的非安全方法；Header Token 客户端不依赖 Cookie，因此不会被该策略误伤。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/8
 */
@NullMarked
public final class WebCookieCsrfFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final SecurityProperties securityProperties;
    private final AccessDeniedHandler accessDeniedHandler;

    /** 创建 Web Cookie CSRF 过滤器。 */
    public WebCookieCsrfFilter(SecurityProperties securityProperties, AccessDeniedHandler accessDeniedHandler) {
        this.securityProperties = securityProperties;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (requiresCsrf(request)) {
            try {
                AuthenticationWebUtils.validateCsrf(request, securityProperties);
            } catch (IllegalArgumentException exception) {
                accessDeniedHandler.handle(request, response, new AccessDeniedException("CSRF 校验失败"));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /** 判断当前请求是否同时满足 Web Cookie、非安全方法和 Cookie-bearing 条件。 */
    private boolean requiresCsrf(HttpServletRequest request) {
        return ClientType.WEB == AuthenticationWebUtils.clientType(request)
                && !SAFE_METHODS.contains(request.getMethod().toUpperCase(Locale.ROOT))
                && hasCookie(request, securityProperties.getRefreshCookieName(), securityProperties.getCsrfCookieName());
    }

    private static boolean hasCookie(HttpServletRequest request, String... names) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            for (String name : names) {
                if (name.equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
