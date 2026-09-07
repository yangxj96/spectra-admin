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

package com.devops00.spectra.framework.security.redis.value;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;

import java.util.Map;
import java.util.UUID;

/**
 * 安全 Redis 值的集中、fail-closed 解析器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@SuppressWarnings("PMD.PreserveStackTrace")
public final class SecurityRedisValueParser {

    private SecurityRedisValueParser() {
    }

    /** 读取必需的非空文本字段。 */
    public static String requiredText(Object value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw invalid(field);
        }
        return value.toString();
    }

    /** 读取必需的 long 字段，不把脏数据降级为零。 */
    public static long requiredLong(Object value, String field) {
        String text = requiredText(value, field);
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw invalid(field);
        }
    }

    /** 读取必需的 UUID 字段。 */
    public static UUID requiredUuid(Object value, String field) {
        String text = requiredText(value, field);
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException exception) {
            throw invalid(field);
        }
    }

    /** 读取必需的客户端类型，不接受未知值回退到 Web。 */
    public static ClientType requiredClientType(Object value, String field) {
        String text = requiredText(value, field);
        for (ClientType clientType : ClientType.values()) {
            if (clientType.getName().equalsIgnoreCase(text)) {
                return clientType;
            }
        }
        throw invalid(field);
    }

    /** 读取必需的 Redis Hash/JSON Map。 */
    public static Map<?, ?> requiredMap(Object value, String field) {
        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            return map;
        }
        throw invalid(field);
    }

    /** 创建不包含原始 Redis 值的脏数据异常，避免日志泄露 payload。 */
    private static SecurityRedisUnavailableException invalid(String field) {
        // 原始解析异常可能包含 Redis payload，不能作为 cause 暴露。
        return new SecurityRedisUnavailableException("安全 Redis 数据损坏，字段 " + field + " 无效", null);
    }
}
