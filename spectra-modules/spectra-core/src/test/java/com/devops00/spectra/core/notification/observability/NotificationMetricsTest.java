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

package com.devops00.spectra.core.notification.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 通知指标低基数标签与匿名 Gauge 回归。
 */
class NotificationMetricsTest {

    @Test
    void shouldRecordOnlyNormalizedLowCardinalityTags() {
        var registry = new SimpleMeterRegistry();
        var metrics = new NotificationMetrics(registry);

        metrics.recordRequest(" system_notice ", "accepted");
        metrics.recordTask("in_app", "pending", "system_notice");
        metrics.setQueueDepth("pending", 4);
        metrics.setOldestPendingSeconds(9);
        metrics.setChannelAvailable("in_app", true);

        assertEquals(1, registry.get("notification_requests_total")
                .tag("purpose", "SYSTEM_NOTICE")
                .tag("status", "ACCEPTED")
                .counter()
                .count());
        assertEquals(4, registry.get("notification_queue_depth").tag("status", "PENDING").gauge().value());
        assertEquals(9, registry.get("notification_oldest_pending_seconds").gauge().value());
        assertEquals(1, registry.get("notification_channel_available").tag("channel", "IN_APP").gauge().value());
    }
}
