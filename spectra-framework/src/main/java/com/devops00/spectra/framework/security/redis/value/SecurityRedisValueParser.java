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

    /**
     * 读取必需的非空文本字段。
     *
     * @param value 从安全 Redis 读取的原始字段值。
     * @param field 字段业务名称，用于定位损坏数据但不会拼接原始 payload。
     * @return 返回安全 Redis 中非空字段的文本值；字段缺失、为空或类型异常时抛出 fail-closed 异常，不返回 null 或空字符串。
     */
    public static String requiredText(Object value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw invalid(field);
        }
        return value.toString();
    }

    /**
     * 读取必需的 long 字段，不把脏数据降级为零。
     *
     * @param value 从安全 Redis 读取的原始整数值。
     * @param field 整数字段业务名称，用于生成不泄露值内容的错误信息。
     * @return 返回安全 Redis 字段解析出的 long 数值；字段缺失、为空或不是合法整数时抛出异常，不以 0 代替错误。
     */
    public static long requiredLong(Object value, String field) {
        String text = requiredText(value, field);
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw invalid(field);
        }
    }

    /**
     * 读取必需的 UUID 字段。
     *
     * @param value 从安全 Redis 读取的原始 UUID 字段值。
     * @param field UUID 字段业务名称，用于生成不泄露值内容的错误信息。
     * @return 返回安全 Redis 字段解析出的 UUID；字段缺失、为空或格式非法时抛出异常，不返回 null。
     */
    public static UUID requiredUuid(Object value, String field) {
        String text = requiredText(value, field);
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException exception) {
            throw invalid(field);
        }
    }

    /**
     * 读取必需的客户端类型，不接受未知值回退到 Web。
     *
     * @param value 从安全 Redis 读取的原始客户端类型字段值。
     * @param field 客户端类型字段业务名称，用于生成不泄露值内容的错误信息。
     * @return 返回安全 Redis 字段对应的已知客户端类型；缺失或未知值时抛出异常，不回退到 WEB，也不返回 null。
     */
    public static ClientType requiredClientType(Object value, String field) {
        String text = requiredText(value, field);
        for (ClientType clientType : ClientType.values()) {
            if (clientType.getName().equalsIgnoreCase(text)) {
                return clientType;
            }
        }
        throw invalid(field);
    }

    /**
     * 读取必需的 Redis Hash/JSON Map。
     *
     * @param value 从安全 Redis 读取的原始 Map 字段值。
     * @param field Map 字段业务名称，用于生成不泄露值内容的错误信息。
     * @return 返回非空的安全 Redis Map 值；值缺失、为空 Map 或类型不符时抛出异常，不返回 null 或空 Map。
     */
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
