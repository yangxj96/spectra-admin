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

package com.devops00.spectra.common.audit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 审计快照的唯一默认脱敏实现。
 *
 * <p>该实现位于 common，保证日志、认证和业务模块不会各自维护敏感字段规则。它只创建用于审计的
 * 结构化副本，不修改业务对象；敏感字段统一替换为 {@value AuditSanitizer#REDACTED_VALUE}，文本中
 * 的 URL 凭据和 Bearer Token 也会被处理。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public final class DefaultAuditSanitizer implements AuditSanitizer {

    private static final Set<Class<?>> SCALAR_TYPES = Set.of(Boolean.class, Character.class, Byte.class,
            Short.class, Integer.class, Long.class, Float.class, Double.class,
            java.math.BigInteger.class, java.math.BigDecimal.class, java.util.UUID.class,
            java.time.Instant.class, java.time.LocalDate.class, java.time.LocalTime.class,
            java.time.LocalDateTime.class, java.time.OffsetDateTime.class, java.time.ZonedDateTime.class);

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
            "emailcode");

    private static final Pattern URL_SECRET = Pattern.compile(
            "(?i)([?&](?:x-amz-signature|x-amz-credential|signature|token|access_token|refresh_token|api_key|apikey|secret)=)[^&#\\s]*");

    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)(\\bbearer\\s+)[A-Za-z0-9._~+/=-]+");

    private static final Pattern INLINE_CREDENTIAL = Pattern.compile(
            "(?i)(\\b[a-z0-9_.-]*(?:password|passwd|pwd|token|secret|api[_-]?key|private[_-]?key|"
                    + "authorization|cookie|credential|captcha|verification[_-]?code|sms[_-]?code|"
                    + "email[_-]?code|signature)[\\\"']?\\s*[:=]\\s*)(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;&}]+)");

    /**
     * 创建无状态的默认审计脱敏器。
     */
    public DefaultAuditSanitizer() {
    }

    @Override
    public Map<String, Object> sanitize(Map<String, ?> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return Map.of();
        }
        var path = new IdentityHashMap<Object, Boolean>();
        path.put(snapshot, Boolean.TRUE);
        return sanitizeMap(snapshot, path);
    }

    /**
     * 递归清洗 Map，并保留原有字段顺序。
     */
    private Map<String, Object> sanitizeMap(Map<?, ?> source, IdentityHashMap<Object, Boolean> path) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key == null) {
                throw new IllegalArgumentException("审计快照字段名不能为空");
            }
            String field = String.valueOf(key);
            sanitized.put(field, isSensitiveKey(field) ? REDACTED_VALUE : sanitizeValue(value, path));
        });
        return sanitized;
    }

    /**
     * 递归清洗任意审计快照值。
     */
    private Object sanitizeValue(Object value, IdentityHashMap<Object, Boolean> path) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return sanitizeText(text);
        }
        if (SCALAR_TYPES.contains(value.getClass())) {
            return value;
        }
        if (value instanceof Enum<?> enumeration) {
            return enumeration.name();
        }
        if (path.size() >= 64 || path.containsKey(value)) {
            return REDACTED_VALUE;
        }
        path.put(value, Boolean.TRUE);
        try {
            return sanitizeStructure(value, path);
        } finally {
            path.remove(value);
        }
    }

    /** 只保留结构化副本；任意对象须先在序列化边界转换，不能延迟展开。 */
    private Object sanitizeStructure(Object value, IdentityHashMap<Object, Boolean> path) {
        if (value instanceof Map<?, ?> map) {
            return sanitizeMap(map, path);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitized = new ArrayList<>();
            iterable.forEach(element -> sanitized.add(sanitizeValue(element, path)));
            return sanitized;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> sanitized = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                sanitized.add(sanitizeValue(Array.get(value, index), path));
            }
            return sanitized;
        }
        return REDACTED_VALUE;
    }

    /**
     * 判断字段是否属于敏感字段。
     */
    private boolean isSensitiveKey(String key) {
        String normalized = key.replaceAll("[-_.]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(normalized)
                || normalized.endsWith("password")
                || normalized.endsWith("token")
                || normalized.endsWith("secret")
                || normalized.endsWith("apikey")
                || normalized.endsWith("privatekey");
    }

    /**
     * 清洗文本中内嵌的凭据。
     */
    private String sanitizeText(String text) {
        String sanitized = URL_SECRET.matcher(text).replaceAll("$1" + REDACTED_VALUE);
        sanitized = BEARER_TOKEN.matcher(sanitized).replaceAll("$1" + REDACTED_VALUE);
        return INLINE_CREDENTIAL.matcher(sanitized).replaceAll("$1" + REDACTED_VALUE);
    }
}
