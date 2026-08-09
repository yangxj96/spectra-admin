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

package com.devops00.spectra.framework.configure.cache;

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

/// 标准key生成器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/2 17:17
@NullMarked
@Component
public class StandardCacheKeyGenerator implements KeyGenerator {

    @Override
    public String generate(Object target, Method method, @Nullable Object... params) {
        String methodName = method.getName();
        String argsHash = hashParams(params);
        return methodName + ":" + argsHash;
    }

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

    private String stableString(@Nullable Object param) {
        return switch (param) {
            case null -> "null";
            case Collection<?> c -> c.stream()
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.joining(","));
            case Map<?, ?> map -> map.entrySet().stream()
                    .sorted(Comparator.comparing(
                            e -> String.valueOf(e.getKey())))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            default -> String.valueOf(param);
        };
    }
}
