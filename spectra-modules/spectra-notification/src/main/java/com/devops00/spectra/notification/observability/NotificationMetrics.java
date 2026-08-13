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

package com.devops00.spectra.notification.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 通知域指标门面；标签只允许低基数业务枚举，不接受用户或业务对象标识。
 */
@Component
public class NotificationMetrics {

    private final MeterRegistry registry;

    private final Map<String, AtomicLong> queueDepths = new ConcurrentHashMap<>();

    private final AtomicLong oldestPendingSeconds = new AtomicLong();

    private final Map<String, AtomicLong> channelAvailability = new ConcurrentHashMap<>();

    public NotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("notification_oldest_pending_seconds", oldestPendingSeconds);
    }

    public void recordRequest(String purpose, String status) {
        Counter.builder("notification_requests_total")
                .tags(Tags.of("purpose", safeTag(purpose), "status", safeTag(status)))
                .register(registry)
                .increment();
    }

    public void recordTask(String channel, String status, String purpose) {
        Counter.builder("notification_tasks_total")
                .tags(Tags.of("channel", safeTag(channel), "status", safeTag(status), "purpose", safeTag(purpose)))
                .register(registry)
                .increment();
    }

    public Timer.Sample startSend() {
        return Timer.start(registry);
    }

    public void stopSend(Timer.Sample sample, String channel, String provider) {
        sample.stop(Timer.builder("notification_send_duration_seconds")
                .tags(Tags.of("channel", safeTag(channel), "provider", safeTag(provider)))
                .register(registry));
    }

    public void recordRetry(String channel, String errorCode) {
        Counter.builder("notification_retry_total")
                .tags(Tags.of("channel", safeTag(channel), "error_code", safeTag(errorCode)))
                .register(registry)
                .increment();
    }

    public void setQueueDepth(String status, long value) {
        var depth = queueDepths.computeIfAbsent(safeTag(status), key -> {
            var gauge = new AtomicLong();
            registry.gauge("notification_queue_depth", Tags.of("status", key), gauge);
            return gauge;
        });
        depth.set(Math.max(0, value));
    }

    public void setOldestPendingSeconds(long value) {
        oldestPendingSeconds.set(Math.max(0, value));
    }

    public void setChannelAvailable(String channel, boolean available) {
        var value = channelAvailability.computeIfAbsent(safeTag(channel), key -> {
            var gauge = new AtomicLong();
            registry.gauge("notification_channel_available", Tags.of("channel", key), gauge);
            return gauge;
        });
        value.set(available ? 1 : 0);
    }

    private String safeTag(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
