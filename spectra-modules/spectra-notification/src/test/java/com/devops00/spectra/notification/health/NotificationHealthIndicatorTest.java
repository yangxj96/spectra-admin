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
import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.notification.sender.NotificationSender;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 通知模块健康状态回归。
 */
class NotificationHealthIndicatorTest {

    @Test
    void shouldReportDownWhenInAppIsUnavailable() {
        var sender = sender(NotificationChannel.IN_APP, false);
        var indicator = new NotificationHealthIndicator(new NotificationModuleProperties(true, "", "", List.of()),
                List.of(sender));

        assertEquals(DependencyHealthStatus.DOWN, indicator.check().status());
        assertEquals("IN_APP_UNAVAILABLE", indicator.check().errorCode());
    }

    @Test
    void shouldReportUpWhenInAppIsAvailable() {
        var sender = sender(NotificationChannel.IN_APP, true);
        var indicator = new NotificationHealthIndicator(new NotificationModuleProperties(true, "", "", List.of()),
                List.of(sender));

        assertEquals(DependencyHealthStatus.UP, indicator.check().status());
    }

    @Test
    void shouldReportUnknownWhenModuleIsDisabled() {
        var indicator = new NotificationHealthIndicator(new NotificationModuleProperties(false, "", "", List.of()),
                List.of());

        assertEquals(DependencyHealthStatus.UNKNOWN, indicator.check().status());
        assertEquals("MODULE_DISABLED", indicator.check().errorCode());
    }

    @Test
    void shouldReportDegradedWhenOptionalChannelIsUnavailable() {
        var inApp = sender(NotificationChannel.IN_APP, true);
        var email = sender(NotificationChannel.EMAIL, false);
        var indicator = new NotificationHealthIndicator(new NotificationModuleProperties(true, "", "", List.of()),
                List.of(inApp, email));

        assertEquals(DependencyHealthStatus.DEGRADED, indicator.check().status());
        assertEquals("OPTIONAL_CHANNEL_UNAVAILABLE", indicator.check().errorCode());
    }

    private NotificationSender sender(NotificationChannel channel, boolean available) {
        var sender = mock(NotificationSender.class);
        when(sender.channel()).thenReturn(channel);
        when(sender.available()).thenReturn(available);
        when(sender.send(org.mockito.ArgumentMatchers.any())).thenReturn(ChannelSendResult.sent("TEST", null,
                "ok"));
        return sender;
    }
}
