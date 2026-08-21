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

package com.devops00.spectra.core.security.authentication.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 认证身份标识摘要。摘要输入先 trim 并使用小写，避免同一邮箱产生多条身份。
 */
public final class AuthenticationIdentifierHash {

    private AuthenticationIdentifierHash() {
    }

    public static String digest(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("认证身份标识不能为空");
        }
        try {
            var normalized = identifier.trim().toLowerCase(java.util.Locale.ROOT);
            var hash = MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JRE 缺少 SHA-256", exception);
        }
    }
}
