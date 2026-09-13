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

package com.devops00.spectra.core.system.javabean.enums;

import com.devops00.spectra.core.system.constant.SystemConfigKeys;

/**
 * 系统配置的业务分类。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public enum ConfiguredCategory {
    SYSTEM,
    SECURITY,
    NOTIFICATION,
    OTHER;

    /**
     * 根据稳定配置键确定业务分类。
     *
     * @param key 配置键。
     * @return 配置业务分类。
     */
    public static ConfiguredCategory fromKey(String key) {
        if (key == null) {
            return OTHER;
        }
        if (key.startsWith("system.") || key.startsWith("copyright.")) {
            return SYSTEM;
        }
        if (key.startsWith("security.") || key.startsWith("crypto.") || key.startsWith("user.")) {
            return SECURITY;
        }
        if (key.startsWith("notification.")) {
            return NOTIFICATION;
        }
        return OTHER;
    }

    /**
     * 判断配置键是否由系统内部维护，不允许从通用配置表单读取或修改原值。
     *
     * @param key 配置键。
     * @return 系统维护项返回 true。
     */
    public static boolean isSystemManagedKey(String key) {
        return SystemConfigKeys.NOTIFICATION_ADDRESS_ENCRYPTION_KEY.equals(key)
                || SystemConfigKeys.NOTIFICATION_SENSITIVE_PAYLOAD_KEY.equals(key)
                || key != null && key.startsWith("notification.provider.");
    }
}
