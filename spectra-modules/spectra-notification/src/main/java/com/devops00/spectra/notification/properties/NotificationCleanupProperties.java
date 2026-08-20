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

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NotificationCleanupProperties {

    private boolean enabled = true;

    private long fixedDelayMs = 3_600_000L;

    private int batchSize = 100;

    private long retentionSeconds = 86_400L;

    private SystemConfigValueProvider systemConfigValueProvider;

    public NotificationCleanupProperties() {
    }

    public NotificationCleanupProperties(boolean enabled, long fixedDelayMs, int batchSize, long retentionSeconds) {
        this.enabled = enabled;
        this.fixedDelayMs = normalizeDelay(fixedDelayMs);
        this.batchSize = normalizeBatchSize(batchSize);
        this.retentionSeconds = normalizeRetention(retentionSeconds);
    }

    @Autowired(required = false)
    public void setSystemConfigValueProvider(SystemConfigValueProvider systemConfigValueProvider) {
        this.systemConfigValueProvider = systemConfigValueProvider;
    }

    public boolean enabled() {
        return systemValue("notification.cleanup.enabled").map(Boolean::parseBoolean).orElse(enabled);
    }

    public long fixedDelayMs() {
        return normalizeDelay(fixedDelayMs);
    }

    public int batchSize() {
        return normalizeBatchSize(batchSize);
    }

    public long retentionSeconds() {
        return normalizeRetention(retentionSeconds);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFixedDelayMs(long fixedDelayMs) {
        this.fixedDelayMs = fixedDelayMs;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setRetentionSeconds(long retentionSeconds) {
        this.retentionSeconds = retentionSeconds;
    }

    private java.util.Optional<String> systemValue(String key) {
        return systemConfigValueProvider == null ? java.util.Optional.empty() : systemConfigValueProvider.find(key);
    }

    private static long normalizeDelay(long value) {
        return value > 0 ? value : 3_600_000L;
    }

    private static int normalizeBatchSize(int value) {
        return value > 0 ? Math.min(value, 1_000) : 100;
    }

    private static long normalizeRetention(long value) {
        return value > 0 ? value : 86_400L;
    }
}
