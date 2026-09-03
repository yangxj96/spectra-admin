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

package com.devops00.spectra.core.notification.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.core.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 通知模块健康 contributor；站内信是必需通道，外部通道按可选能力计算 DEGRADED。
 */
@Component("notification")
@RequiredArgsConstructor
public class NotificationHealthIndicator implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final NotificationModuleProperties properties;

    private final List<NotificationSender> senders;

    @Override
    public String contributorName() {
        return "notification";
    }

    @Override
    public String moduleName() {
        return "notification";
    }

    @Override
    public String dependencyType() {
        return "NOTIFICATION";
    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        var checkedAt = Instant.now();
        if (!properties.enabled()) {
            return result(DependencyHealthStatus.UNKNOWN, start, checkedAt, "MODULE_DISABLED", "通知模块未启用");
        }
        if (!senderAvailable(NotificationChannel.IN_APP)) {
            return result(DependencyHealthStatus.DOWN, start, checkedAt, "IN_APP_UNAVAILABLE", "站内信通道不可用");
        }
        var optionalChannelUnavailable = java.util.Arrays.stream(NotificationChannel.values())
                .filter(channel -> channel != NotificationChannel.IN_APP)
                .anyMatch(channel -> sender(channel).map(sender -> !sender.available()).orElse(false));
        if (optionalChannelUnavailable) {
            return result(DependencyHealthStatus.DEGRADED, start, checkedAt,
                    "OPTIONAL_CHANNEL_UNAVAILABLE", "站内信正常，部分可选通知通道不可用");
        }
        return result(DependencyHealthStatus.UP, start, checkedAt, null, "通知通道检查正常");
    }

    private boolean senderAvailable(NotificationChannel channel) {
        return sender(channel)
                .map(NotificationSender::available)
                .orElse(false);
    }

    private java.util.Optional<NotificationSender> sender(NotificationChannel channel) {
        return senders.stream()
                .filter(item -> item.channel() == channel)
                .findFirst();
    }

    private DependencyHealthResult result(DependencyHealthStatus status, long start, Instant checkedAt,
                                          String errorCode, String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), checkedAt, errorCode, safeSummary);
    }
}
