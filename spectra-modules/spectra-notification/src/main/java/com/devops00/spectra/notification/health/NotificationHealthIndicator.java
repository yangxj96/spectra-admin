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

package com.devops00.spectra.notification.health;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

/**
 * 通知模块健康检查；站内信不可用时整体标记为 DOWN。
 */
@Component("notification")
@RequiredArgsConstructor
public class NotificationHealthIndicator implements HealthIndicator {

    private final NotificationModuleProperties properties;

    private final List<NotificationSender> senders;

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.down().withDetail("reason", "MODULE_DISABLED").build();
        }
        var channels = new EnumMap<NotificationChannel, String>(NotificationChannel.class);
        for (var channel : NotificationChannel.values()) {
            var sender = senders.stream().filter(item -> item.channel() == channel).findFirst();
            channels.put(channel, sender.map(item -> item.available() ? "AVAILABLE" : item.unavailableReason())
                    .orElse("CHANNEL_NOT_REGISTERED"));
        }
        var inApp = channels.get(NotificationChannel.IN_APP);
        var builder = "AVAILABLE".equals(inApp)
                ? Health.up()
                : Health.down().withDetail("reason", "IN_APP_UNAVAILABLE");
        return builder.withDetail("enabled", true).withDetail("channels", channels).build();
    }
}
