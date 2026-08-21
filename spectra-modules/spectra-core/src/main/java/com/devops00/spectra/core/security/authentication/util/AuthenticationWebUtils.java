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

package com.devops00.spectra.core.security.authentication.util;

import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.TokenDigestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/** 认证接口的 HTTP、Cookie 和 CSRF 工具。 */
public final class AuthenticationWebUtils {

    private AuthenticationWebUtils() {
    }

    public static ClientType clientType(HttpServletRequest request) {
        return ClientType.fromName(request.getHeader("X-Client-Type"));
    }

    public static boolean isWebClient(ClientType clientType) {
        return clientType == ClientType.WEB;
    }

    public static TokenVO writeWebToken(HttpServletResponse response, TokenVO token,
                                        SecurityProperties securityProperties, ClientType clientType) {
        issueWebCookies(response, token.getRefreshToken(), securityProperties, clientType);
        if (isWebClient(clientType)) {
            token.setRefreshToken(null);
        }
        return token;
    }

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

    public static void clearWebCookies(HttpServletResponse response, SecurityProperties securityProperties) {
        addCookie(response, securityProperties, securityProperties.getRefreshCookieName(), "", true,
                securityProperties.getRefreshCookieSameSite(), 0);
        addCookie(response, securityProperties, securityProperties.getCsrfCookieName(), "", false,
                securityProperties.getRefreshCookieSameSite(), 0);
    }

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
