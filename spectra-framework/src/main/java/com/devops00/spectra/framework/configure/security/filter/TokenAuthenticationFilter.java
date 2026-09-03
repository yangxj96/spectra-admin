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

package com.devops00.spectra.framework.configure.security.filter;

import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * token鉴权过滤器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/2 23:45
 */
@NullMarked
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityContextAccessor securityContextAccessor;

    private final SecurityUserLookupPort securityUserLookupPort;

    public TokenAuthenticationFilter(SecurityContextAccessor securityContextAccessor, SecurityUserLookupPort securityUserLookupPort) {
        this.securityContextAccessor = securityContextAccessor;
        this.securityUserLookupPort = securityUserLookupPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = securityContextAccessor.currentToken();
        if (StrUtils.isNotBlank(token)) {
            try {
                SecurityPrincipal user = securityUserLookupPort.findByToken(token);
                if (user == null) {
                    SecurityContextHolder.clearContext();
                } else if (user.isPasswordChangeRequired() && !isPasswordChangeRequest(request)) {
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "请先修改密码");
                    return;
                } else {
                    var authorities = user.getAuthorityNames()
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    var auth = new UsernamePasswordAuthenticationToken(user, token, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (SecurityRedisUnavailableException | DataAccessException exception) {
                // Redis 是当前 opaque session 的事实源；依赖不可用时禁止请求继续进入业务层。
                SecurityContextHolder.clearContext();
                log.warn("Redis 会话依赖不可用，拒绝当前请求: {} {}", request.getMethod(), request.getRequestURI(), exception);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "安全会话服务暂不可用");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 判断条件是否满足（{@code isPasswordChangeRequest}）。
     */
    private boolean isPasswordChangeRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return path.endsWith("/menu/current")
                    || path.endsWith("/security/context")
                    || path.endsWith("/user/profile")
                    || path.endsWith("/system/guide/status")
                    || path.endsWith("/system/crypto/keypair/client-private");
        }
        return ("PUT".equalsIgnoreCase(request.getMethod()) && path.endsWith("/user/password"))
                || ("POST".equalsIgnoreCase(request.getMethod()) && path.endsWith("/security/authentication/logout"));
    }
}
