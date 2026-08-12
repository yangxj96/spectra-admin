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

package com.devops00.spectra.common.utils;

import com.google.common.base.Strings;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 字符串工具类，替代 Apache Commons Lang3 的 StringUtils。
 *
 * <ul>
 * <li>兼容 JDK 8+（包括 JDK 25）</li>
 * <li>利用 Guava 处理 null/empty</li>
 * <li>行为与 org.apache.commons.lang3.StringUtils 一致</li>
 * </ul>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
public final class StrUtils {

    private StrUtils() {
        // 工具类禁止实例化
    }

    /**
     * 判断字符串是否为 null 或 ""
     */
    public static boolean isEmpty(@Nullable String str) {
        return Strings.isNullOrEmpty(str);
    }

    /**
     * 判断字符串是否非 null 且非 ""
     */
    public static boolean isNotEmpty(@Nullable String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为 null、"" 或仅由空白字符组成（如空格、制表符、换行符等）
     */
    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    /**
     * 判断字符串是否非 null、非 "" 且包含非空白字符
     */
    public static boolean isNotBlank(@Nullable String str) {
        return !isBlank(str);
    }

    /**
     * 安全截取子串 [start, end)
     *
     * <ul>
     * <li>若 str 为 null，返回 null</li>
     * <li>自动处理越界（不会抛 IndexOutOfBoundsException）</li>
     * <li>行为等同于 org.apache.commons.lang3.StringUtils.substring(str, start,
     * end)</li>
     * </ul>
     */
    public static @Nullable String substring(@Nullable String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // 处理负数起始位置（Commons 行为：负数视为 0）
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        if (start > end) {
            return "";
        }

        int len = str.length();
        if (start >= len) {
            return "";
        }
        if (end > len) {
            end = len;
        }

        return str.substring(start, end);
    }

    /**
     * 按「最大字节数」安全截取（方案一:逐字符计算）
     * <p>
     * 适用场景：
     * <ul>
     * <li>数据库 VARCHAR(n) 按字节限制</li>
     * <li>中文 / 英文 / 数字混合</li>
     * <li>业务系统首选（稳定、可读性好）</li>
     * </ul>
     * <p>
     * 特性：
     * <ul>
     * <li>不会截断半个中文字符</li>
     * <li>超出 maxBytes 自动停止</li>
     * <li>str 为 null 时返回 null</li>
     * </ul>
     */
    public static @Nullable String substringByByte(@Nullable String str, int maxBytes, Charset charset) {
        if (str == null) {
            return null;
        }
        if (maxBytes <= 0) {
            return "";
        }

        int usedBytes = 0;
        int endIndex = 0;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int byteLen = String.valueOf(ch).getBytes(charset).length;

            if (usedBytes + byteLen > maxBytes) {
                break;
            }

            usedBytes += byteLen;
            endIndex = i + 1;
        }

        return str.substring(0, endIndex);
    }

    /**
     * UTF-8 快捷方法（最常用）
     */
    public static @Nullable String substringByByteUtf8(@Nullable String str, int maxBytes) {
        return substringByByte(str, maxBytes, StandardCharsets.UTF_8);
    }

    /**
     * 按「最大字节数」安全截取（方案二:CharsetEncoder）
     * <p>
     * 适用场景：
     * <ul>
     * <li>高并发 / 大文本</li>
     * <li>对编码行为要求极严格</li>
     * <li>通用基础组件</li>
     * </ul>
     * <p>
     * 特性：
     * <ul>
     * <li>由 CharsetEncoder 保证字符完整性</li>
     * <li>性能略优于逐字符方式</li>
     * <li>实现更底层、更专业</li>
     * </ul>
     */
    public static @Nullable String substringByByteWithEncoder(@Nullable String str, int maxBytes, Charset charset) {
        if (str == null) {
            return null;
        }
        if (maxBytes <= 0) {
            return "";
        }

        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer byteBuffer = ByteBuffer.allocate(maxBytes);
        CharBuffer charBuffer = CharBuffer.wrap(str);

        encoder.encode(charBuffer, byteBuffer, true);
        byteBuffer.flip();

        return charset.decode(byteBuffer).toString();
    }

    /**
     * UTF-8 Encoder 快捷方法
     */
    public static @Nullable String substringByByteUtf8WithEncoder(@Nullable String str, int maxBytes) {
        return substringByByteWithEncoder(str, maxBytes, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串转换为 camelCase 或 PascalCase 格式 > 前提：str 不得为 null（Servlet 参数名、配置 key
     * 等场景天然满足）
     *
     * @param str                   非 null 输入字符串（如 "user_first_name"）
     * @param capitalizeFirstLetter true → PascalCase ("UserName")，false → camelCase ("userName")
     * @param delimiter             分隔符（如 '_'）
     * @return 转换后的非 null 字符串
     */
    public static String toCamelCase(String str, boolean capitalizeFirstLetter, char delimiter) {
        if (str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = capitalizeFirstLetter;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == delimiter) {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }
}