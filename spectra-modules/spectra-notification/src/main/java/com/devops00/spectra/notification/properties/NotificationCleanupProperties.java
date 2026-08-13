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

package com.devops00.spectra.notification.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通知敏感载荷清理策略。
 *
 * @param enabled          是否启用定时清理
 * @param fixedDelayMs     清理任务固定间隔
 * @param batchSize        单次最多处理的请求/任务数
 * @param retentionSeconds 终态记录保留敏感载荷的秒数
 */
@ConfigurationProperties(prefix = "spectra.notification.cleanup")
public record NotificationCleanupProperties(boolean enabled, long fixedDelayMs, int batchSize,
                                            long retentionSeconds) {

    public NotificationCleanupProperties {
        fixedDelayMs = fixedDelayMs > 0 ? fixedDelayMs : 3_600_000L;
        batchSize = batchSize > 0 ? Math.min(batchSize, 1_000) : 100;
        retentionSeconds = retentionSeconds > 0 ? retentionSeconds : 86_400L;
    }
}
