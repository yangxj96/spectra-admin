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

package com.devops00.spectra.framework.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准key生成器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/2 17:17
 */
@NullMarked
@Component
public class StandardCacheKeyGenerator implements KeyGenerator {

    /**
     * 根据目标方法和调用参数生成稳定、可复现的缓存键。
     *
     * @param target 被调用缓存方法所属的目标对象；仅用于确定调用上下文。
     * @param method 被调用的业务方法，用于把方法名写入缓存键。
     * @param params 业务方法的实参；集合和 Map 会先规范化，null 参数保留为固定文本。
     * @return 返回由目标类、方法名和参数稳定摘要组成的缓存键；无参数时使用 {@code noargs}，结果始终为非空字符串。
     */
    @Override
    public String generate(Object target, Method method, @Nullable Object... params) {
        String methodName = method.getName();
        String argsHash = hashParams(params);
        return methodName + ":" + argsHash;
    }

    /**
     * 计算缓存参数的稳定哈希值（{@code hashParams}）。
     */
    private String hashParams(@Nullable Object[] params) {

        if (params == null || params.length == 0) {
            return "noargs";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            for (Object param : params) {
                md.update(stableString(param).getBytes(StandardCharsets.UTF_8));
            }

            return HexFormat.of().formatHex(md.digest()).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * 将缓存参数规范化为稳定字符串（{@code stableString}）。
     */
    private String stableString(@Nullable Object param) {
        return switch (param) {
            case null -> "null";
            case Collection<?> c -> c.stream().map(String::valueOf).sorted().collect(Collectors.joining(","));
            case Map<?, ?> map -> map.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(e -> String.valueOf(e.getKey())))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            default -> String.valueOf(param);
        };
    }
}
