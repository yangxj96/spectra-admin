/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.session;

import com.devops00.spectra.security.base.properties.SecurityProperties;

/** Web Refresh Cookie 的启动前安全校验。 */
public final class WebCookiePolicy {

    private WebCookiePolicy() {
    }

    public static void validate(SecurityProperties properties) {
        if (properties == null) {
            throw new IllegalStateException("Web Security 配置不能为空");
        }
        String name = properties.getRefreshCookieName();
        String path = properties.getRefreshCookiePath();
        String sameSite = properties.getRefreshCookieSameSite();
        String domain = properties.getRefreshCookieDomain();
        if (name == null
                || !name.startsWith("__Host-")
                || !properties.isRefreshCookieSecure()
                || path == null
                || !"/".equals(path)
                || (domain != null && !domain.isBlank())) {
            throw new IllegalStateException("Web Refresh Cookie 必须为 Secure、Host-only、Path=/ 的 __Host- Cookie");
        }
        if (sameSite == null
                || !("Strict".equalsIgnoreCase(sameSite)
                        || "Lax".equalsIgnoreCase(sameSite)
                        || "None".equalsIgnoreCase(sameSite))) {
            throw new IllegalStateException("Web Refresh Cookie SameSite 只能为 Strict、Lax 或 None");
        }
        if ("None".equalsIgnoreCase(sameSite) && !properties.isRefreshCookieSameSiteNoneAllowed()) {
            throw new IllegalStateException("SameSite=None 必须经过部署安全评审并显式允许");
        }
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

    private static boolean containsControlCharacter(String value) {
        return value.chars().anyMatch(character -> character < 0x21 || character > 0x7e);
    }
}
