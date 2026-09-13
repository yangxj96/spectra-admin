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

package com.devops00.spectra.core.notification.sender;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 通知发送器 Registry 的渠道解析契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/8
 */
class NotificationSenderRegistryTest {

    @Test
    void requireAndFindReturnRegisteredSenderEvenWhenUnavailable() {
        var sender = sender(NotificationChannel.SMS, false);
        var registry = new NotificationSenderRegistry(List.of(sender));

        assertSame(sender, registry.require(NotificationChannel.SMS));
        assertSame(sender, registry.find(NotificationChannel.SMS).orElseThrow());
    }

    @Test
    void findReturnsEmptyForNullOrUnknownChannel() {
        var registry = new NotificationSenderRegistry(List.of(sender(NotificationChannel.IN_APP, true)));

        assertTrue(registry.find(null).isEmpty());
        assertTrue(registry.find(NotificationChannel.EMAIL).isEmpty());
    }

    @Test
    void requireRejectsNullOrUnknownChannelWithNotificationUnavailableError() {
        var registry = new NotificationSenderRegistry(List.of(sender(NotificationChannel.IN_APP, true)));

        assertThrows(DataSaveException.class, () -> registry.require(null));
        assertThrows(DataSaveException.class, () -> registry.require(NotificationChannel.EMAIL));
    }

    @Test
    void constructorRejectsNullAndDuplicateChannelDeclarations() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationSenderRegistry(null));
        var nullSenderList = new ArrayList<NotificationSender>();
        nullSenderList.add(null);
        assertThrows(IllegalArgumentException.class,
                () -> new NotificationSenderRegistry(nullSenderList));
        assertThrows(IllegalStateException.class, () -> new NotificationSenderRegistry(List.of(
                sender(NotificationChannel.SMS, true), sender(NotificationChannel.SMS, false))));
    }

    @Test
    void constructorCopiesSenderListBeforeIndexing() {
        var senders = new ArrayList<>(List.of(sender(NotificationChannel.SMS, true)));
        var registry = new NotificationSenderRegistry(senders);
        senders.add(sender(NotificationChannel.EMAIL, true));

        assertTrue(registry.find(NotificationChannel.EMAIL).isEmpty());
    }

    /**
     * 处理发送器相关数据。
     */
    private NotificationSender sender(NotificationChannel channel, boolean available) {
        var sender = mock(NotificationSender.class);
        when(sender.channel()).thenReturn(channel);
        when(sender.available()).thenReturn(available);
        return sender;
    }
}
