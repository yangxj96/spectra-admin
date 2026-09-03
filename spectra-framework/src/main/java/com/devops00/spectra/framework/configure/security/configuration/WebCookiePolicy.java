/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.configuration;

import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;

/** Web Refresh Cookie 的启动前安全校验。 */
public final class WebCookiePolicy {

    private WebCookiePolicy() {
    }

    /**
     * 校验并确保数据满足当前约束（{@code validate}）。
     */
    public static void validate(SecurityProperties properties) {
        if (properties == null) {
            throw new IllegalStateException("Web Security 配置不能为空");
        }
        validateRefreshCookie(properties);
        validateCsrfNames(properties);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateRefreshCookie}）。
     */
    private static void validateRefreshCookie(SecurityProperties properties) {
        String name = properties.getRefreshCookieName();
        String path = properties.getRefreshCookiePath();
        String sameSite = properties.getRefreshCookieSameSite();
        String domain = properties.getRefreshCookieDomain();
        boolean secure = properties.isRefreshCookieSecure();
        validateRefreshCookieName(name);
        boolean hostCookie = name.startsWith("__Host-");
        validateHostCookie(path, domain);
        validateSecureMode(properties, secure, hostCookie);
        validateSameSite(properties, sameSite, secure);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateRefreshCookieName}）。
     */
    private static void validateRefreshCookieName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Web Refresh Cookie 名称不能为空");
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateHostCookie}）。
     */
    private static void validateHostCookie(String path, String domain) {
        if (path == null || !"/".equals(path) || (domain != null && !domain.isBlank())) {
            throw new IllegalStateException("Web Refresh Cookie 必须为 Host-only、Path=/ 且不能设置 Domain");
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateSecureMode}）。
     */
    private static void validateSecureMode(SecurityProperties properties, boolean secure, boolean hostCookie) {
        if (secure && !hostCookie) {
            throw new IllegalStateException("Secure Web Refresh Cookie 必须使用 __Host- 名称");
        }
        if (!secure && (!properties.isAllowInsecureRefreshCookie() || hostCookie)) {
            throw new IllegalStateException("非 Secure Web Refresh Cookie 仅允许开发环境显式开启，且不得使用 __Host- 名称");
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateSameSite}）。
     */
    private static void validateSameSite(SecurityProperties properties, String sameSite, boolean secure) {
        if (sameSite == null || !isSupportedSameSite(sameSite)) {
            throw new IllegalStateException("Web Refresh Cookie SameSite 只能为 Strict、Lax 或 None");
        }
        if ("None".equalsIgnoreCase(sameSite)
                && (!secure || !properties.isRefreshCookieSameSiteNoneAllowed())) {
            throw new IllegalStateException("SameSite=None 必须在 Secure Cookie 下经过部署安全评审并显式允许");
        }
    }

    /**
     * 判断条件是否满足（{@code isSupportedSameSite}）。
     */
    private static boolean isSupportedSameSite(String sameSite) {
        return "Strict".equalsIgnoreCase(sameSite)
                || "Lax".equalsIgnoreCase(sameSite)
                || "None".equalsIgnoreCase(sameSite);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateCsrfNames}）。
     */
    private static void validateCsrfNames(SecurityProperties properties) {
        String csrfCookieName = properties.getCsrfCookieName();
        String csrfHeaderName = properties.getCsrfHeaderName();
        if (csrfCookieName == null
                || csrfCookieName.isBlank()
                || csrfHeaderName == null
                || csrfHeaderName.isBlank()
                || csrfCookieName.equalsIgnoreCase(csrfHeaderName)
                || containsControlCharacter(csrfCookieName)
                || containsControlCharacter(csrfHeaderName)) {
            throw new IllegalStateException("CSRF Cookie/Header 名称必须为非空且互不相同的安全名称");
        }
    }

    /**
     * 判断条件是否满足（{@code containsControlCharacter}）。
     */
    private static boolean containsControlCharacter(String value) {
        return value.chars().anyMatch(character -> character < 0x21 || character > 0x7e);
    }
}
