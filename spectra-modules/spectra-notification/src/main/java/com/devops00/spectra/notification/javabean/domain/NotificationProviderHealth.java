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

package com.devops00.spectra.notification.javabean.domain;

import java.time.Instant;

/**
 * Provider 健康检查结果；状态必须使用通知渠道统一状态。
 *
 * @param state     Provider 健康状态
 * @param reason    脱敏状态原因
 * @param checkedAt 检查时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public record NotificationProviderHealth(NotificationProviderHealthState state, String reason, Instant checkedAt) {

    public static NotificationProviderHealth healthy(String reason, Instant checkedAt) {
        return new NotificationProviderHealth(NotificationProviderHealthState.HEALTHY, reason, checkedAt);
    }

    public static NotificationProviderHealth unhealthy(String reason, Instant checkedAt) {
        return new NotificationProviderHealth(NotificationProviderHealthState.UNHEALTHY, reason, checkedAt);
    }

    public static NotificationProviderHealth notConfigured(String reason, Instant checkedAt) {
        return new NotificationProviderHealth(NotificationProviderHealthState.NOT_CONFIGURED, reason, checkedAt);
    }

    public static NotificationProviderHealth disabled(String reason, Instant checkedAt) {
        return new NotificationProviderHealth(NotificationProviderHealthState.DISABLED, reason, checkedAt);
    }

    public static NotificationProviderHealth blocked(String reason, Instant checkedAt) {
        return new NotificationProviderHealth(NotificationProviderHealthState.BLOCKED, reason, checkedAt);
    }
}
