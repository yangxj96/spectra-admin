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

import com.devops00.spectra.common.foundation.lang.StrUtils;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * 认证接口的 HTTP、Cookie 和 CSRF 技术适配。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public final class AuthenticationWebUtils {

    private AuthenticationWebUtils() {
    }

    /**
     * 执行 Framework 的 clientType 处理流程。
     *
     * @param request 当前 HTTP 请求或待处理的安全业务请求。
     * @return 返回请求头 {@code X-Client-Type} 解析出的客户端类型；缺失或未知值按 {@code ClientType.fromName} 的约定回退到 WEB，不返回 null。
     */
    public static ClientType clientType(HttpServletRequest request) {
        return ClientType.fromName(request.getHeader("X-Client-Type"));
    }

    /**
     * 获取或判断 Framework 的 isWebClient 结果。
     *
     * @param clientType 客户端类型，用于选择对应的安全会话策略。
     * @return 返回客户端是否为浏览器 Web 类型；仅 {@code ClientType.WEB} 返回 true，其余类型和 null 返回 false。
     */
    public static boolean isWebClient(ClientType clientType) {
        return clientType == ClientType.WEB;
    }

    /**
     * 执行 Framework 的 writeWebToken 处理流程。
     *
     * @param response           当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @param token              待摘要、校验或撤销的访问令牌；不得写入日志。
     * @param securityProperties Web 会话 Cookie 名称、SameSite 属性和过期时间等安全配置。
     * @param clientType         客户端类型，用于选择对应的安全会话策略。
     * @return 返回写入 Web Cookie 后的访问令牌；Web 客户端会清除返回对象中的 refreshToken，非 Web 客户端保留原令牌，不返回 null。
     */
    public static SecurityToken writeWebToken(HttpServletResponse response, SecurityToken token,
                                              SecurityProperties securityProperties, ClientType clientType) {
        issueWebCookies(response, token.getRefreshToken(), securityProperties, clientType);
        if (isWebClient(clientType)) {
            token.setRefreshToken(null);
        }
        return token;
    }

    /**
     * 获取或判断 Framework 的 issueWebCookies 结果。
     *
     * @param response           当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @param refreshToken       待轮换或撤销的刷新令牌；不得写入日志。
     * @param securityProperties 用于确定刷新令牌和 CSRF Cookie 属性的安全配置。
     * @param clientType         客户端类型，用于选择对应的安全会话策略。
     */
    public static void issueWebCookies(HttpServletResponse response, String refreshToken,
                                       SecurityProperties securityProperties, ClientType clientType) {
        if (!isWebClient(clientType) || StrUtils.isBlank(refreshToken)) {
            return;
        }
        addCookie(response, securityProperties, securityProperties.getRefreshCookieName(), refreshToken, true,
                securityProperties.getRefreshCookieSameSite(), securityProperties.getRefreshTokenExpire());
        addCookie(response, securityProperties, securityProperties.getCsrfCookieName(), TokenDigestService.generateToken(),
                false, securityProperties.getRefreshCookieSameSite(), securityProperties.getRefreshTokenExpire());
    }

    /**
     * 执行 Framework 的 clearWebCookies 处理流程。
     *
     * @param response           当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @param securityProperties 用于确定要清除的刷新令牌和 CSRF Cookie 名称及属性的安全配置。
     */
    public static void clearWebCookies(HttpServletResponse response, SecurityProperties securityProperties) {
        addCookie(response, securityProperties, securityProperties.getRefreshCookieName(), "", true,
                securityProperties.getRefreshCookieSameSite(), 0);
        addCookie(response, securityProperties, securityProperties.getCsrfCookieName(), "", false,
                securityProperties.getRefreshCookieSameSite(), 0);
    }

    /**
     * 执行 Framework 的 validateCsrf 处理流程。
     *
     * @param request            当前 HTTP 请求或待处理的安全业务请求。
     * @param securityProperties 提供 CSRF Header 和 Cookie 名称的安全配置。
     */
    public static void validateCsrf(HttpServletRequest request, SecurityProperties securityProperties) {
        String header = request.getHeader(securityProperties.getCsrfHeaderName());
        String cookie = readCookie(request, securityProperties.getCsrfCookieName());
        if (StrUtils.isBlank(header)
                || StrUtils.isBlank(cookie)
                || !MessageDigest.isEqual(header.getBytes(StandardCharsets.UTF_8),
                        cookie.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("CSRF 校验失败");
        }
    }

    /**
     * 获取或判断 Framework 的 readCookie 结果。
     *
     * @param request 当前 HTTP 请求或待处理的安全业务请求。
     * @param name    要读取的 Cookie 名称。
     * @return 返回指定名称 Cookie 的值；请求没有 Cookie 或名称不存在时返回 null。
     */
    public static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 处理身份认证对象Web相关数据。
     */
    private static void addCookie(HttpServletResponse response, SecurityProperties securityProperties, String name,
                                  String value, boolean httpOnly, String sameSite, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(securityProperties.isRefreshCookieSecure())
                .path(securityProperties.getRefreshCookiePath())
                .sameSite(sameSite)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
