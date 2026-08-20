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
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

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

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    void shouldReportUpWhenInAppIsAvailable() {
        var sender = sender(NotificationChannel.IN_APP, true);
        var indicator = new NotificationHealthIndicator(new NotificationModuleProperties(true, "", "", List.of()),
                List.of(sender));

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    private NotificationSender sender(NotificationChannel channel, boolean available) {
        var sender = mock(NotificationSender.class);
        when(sender.channel()).thenReturn(channel);
        when(sender.available()).thenReturn(available);
        when(sender.send(org.mockito.ArgumentMatchers.any())).thenReturn(new ChannelSendResult("SENT", "TEST", null,
                "ok"));
        return sender;
    }
}
