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

package com.devops00.spectra.core.notification.security;

import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;

/**
 * 通知 feature 的内容摘要适配器。
 *
 * <p>通知业务只依赖“生成稳定内容摘要”的语义，不直接依赖 SHA-256 算法实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public final class NotificationDigest {

    private NotificationDigest() {
    }

    /**
     * 生成通知幂等键、回调校验或模板版本使用的十六进制摘要。
     *
     * @param value 已按通知协议规范化的内容；不能为 null
     * @return 固定 64 个十六进制字符的摘要；算法不可用时抛出 {@link IllegalStateException}
     */
    public static String hash(String value) {
        return SHA256Utils.hash(value);
    }
}
