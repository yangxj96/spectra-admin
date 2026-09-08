/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.user.imports.security;

import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 用户导入 Preview 一次性 token 的摘要适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public final class PreviewTokenDigest {

    private PreviewTokenDigest() {
    }

    /**
     * 生成存储用的 Preview token 摘要。
     *
     * @param token 仅在当前流程中使用的明文 token
     * @return 固定 64 个十六进制字符的摘要，不返回明文 token
     */
    public static String hash(String token) {
        return SHA256Utils.hash(token);
    }

    /**
     * 使用常量时间比较校验 Preview token。
     *
     * @param token        当前请求提交的明文 token
     * @param expectedHash 数据库中保存的十六进制摘要
     * @return 摘要相等返回 true；期望摘要为 null 或摘要不匹配返回 false
     */
    public static boolean matches(String token, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(hash(token).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
