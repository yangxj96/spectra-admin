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

package com.devops00.spectra.security.base.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Security Audit 快照脱敏器。
 * <p>
 * 使用敏感字段 deny-list 作为最后一道防线：命中的字段不会进入审计快照；嵌套 Map/List 也会递归处理。
 * 业务调用方仍应优先构造字段白名单快照，不能把该类当作任意对象序列化器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class SecurityAuditSnapshotSanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordhash", "accesstoken", "refreshtoken", "token", "secret", "providersecret",
            "privatekey", "totpsecret", "recoverycode", "recoverycodes", "clientsecret", "authorization");

    private SecurityAuditSnapshotSanitizer() {
    }

    /**
     * 递归生成可写入审计的结构化快照。
     *
     * @param snapshot 原始快照
     * @return 脱敏后的快照
     */
    public static Map<String, Object> sanitize(Map<String, ?> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        snapshot.forEach((key, value) -> {
            if (key == null || isSensitiveKey(key)) {
                return;
            }
            sanitized.put(key, sanitizeValue(value));
        });
        return Collections.unmodifiableMap(new LinkedHashMap<>(sanitized));
    }

    private static Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> nested) {
            Map<String, Object> nestedMap = new LinkedHashMap<>();
            nested.forEach((key, nestedValue) -> {
                if (key != null && !isSensitiveKey(key.toString())) {
                    nestedMap.put(key.toString(), sanitizeValue(nestedValue));
                }
            });
            return Collections.unmodifiableMap(new LinkedHashMap<>(nestedMap));
        }
        if (value instanceof Collection<?> collection) {
            return Collections.unmodifiableList(collection.stream().map(SecurityAuditSnapshotSanitizer::sanitizeValue).toList());
        }
        if (value != null && value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            var values = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                values.add(sanitizeValue(java.lang.reflect.Array.get(value, index)));
            }
            return Collections.unmodifiableList(new ArrayList<>(values));
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(normalized) || normalized.endsWith("secret") || normalized.endsWith("token");
    }
}
