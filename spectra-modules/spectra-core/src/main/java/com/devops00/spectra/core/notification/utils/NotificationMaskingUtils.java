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

package com.devops00.spectra.core.notification.utils;

import org.springframework.util.StringUtils;

/**
 * 通知地址脱敏工具。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public final class NotificationMaskingUtils {

    private NotificationMaskingUtils() {
    }

    /**
     * 对邮件地址、手机号或其他短地址执行统一的保守脱敏。
     *
     * @param address 原始地址
     * @return 脱敏地址；空白输入返回 {@code null}
     */
    public static String maskAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        var value = address.trim();
        var at = value.indexOf('@');
        if (at > 1) {
            return value.charAt(0) + "***" + value.substring(at);
        }
        return value.length() > 4 ? value.substring(0, 3) + "****" + value.substring(value.length() - 2) : "***";
    }

    /**
     * 对地址执行统一脱敏，并在地址为空时返回固定占位符。
     *
     * @param address 原始地址
     * @return 脱敏地址或 {@code ***}
     */
    public static String maskAddressOrPlaceholder(String address) {
        var masked = maskAddress(address);
        return masked == null ? "***" : masked;
    }
}
