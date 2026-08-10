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

package com.devops00.spectra.log.base.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 审计日志敏感字段脱敏器。
 *
 * <p>
 * 仅创建用于日志持久化的副本，不修改业务对象。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public final class AuditLogSanitizer {

    public static final String REDACTED_VALUE = "***";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "passwd",
            "pwd",
            "authorization",
            "cookie",
            "setcookie",
            "credential",
            "credentials",
            "captcha",
            "captchacode",
            "kaptcha",
            "verificationcode",
            "verifycode",
            "smscode",
            "emailcode",
            "otp",
            "totp"
    );

    private static final Pattern URL_SECRET = Pattern.compile(
            "(?i)([?&](?:x-amz-signature|x-amz-credential|signature|token|access_token|refresh_token|api_key|apikey|secret)=)[^&#\\s]*");

    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)(\\bbearer\\s+)[A-Za-z0-9._~+/=-]+");

    private AuditLogSanitizer() {
    }

    /**
     * 递归清洗可序列化对象。
     */
    public static Object sanitize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return sanitizeMap(map);
        }
        if (value instanceof Iterable<?> iterable) {
            return sanitizeList(iterable);
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> sanitized = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                sanitized.add(sanitize(Array.get(value, i)));
            }
            return sanitized;
        }
        if (value instanceof String text) {
            return sanitizeText(text);
        }
        return value;
    }

    /**
     * 递归清洗 Map，并保留原有字段顺序。
     */
    public static Map<String, Object> sanitizeMap(Map<?, ?> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            String field = String.valueOf(key);
            sanitized.put(field, isSensitiveKey(field) ? REDACTED_VALUE : sanitize(value));
        });
        return sanitized;
    }

    /**
     * 递归清洗集合。
     */
    public static List<Object> sanitizeList(Iterable<?> source) {
        List<Object> sanitized = new ArrayList<>();
        source.forEach(value -> sanitized.add(sanitize(value)));
        return sanitized;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key.replaceAll("[-_.]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(normalized)
            || normalized.endsWith("password")
            || normalized.endsWith("token")
            || normalized.endsWith("secret")
            || normalized.endsWith("apikey")
            || normalized.endsWith("privatekey");
    }

    private static String sanitizeText(String text) {
        String sanitized = URL_SECRET.matcher(text).replaceAll("$1" + REDACTED_VALUE);
        return BEARER_TOKEN.matcher(sanitized).replaceAll("$1" + REDACTED_VALUE);
    }
}
