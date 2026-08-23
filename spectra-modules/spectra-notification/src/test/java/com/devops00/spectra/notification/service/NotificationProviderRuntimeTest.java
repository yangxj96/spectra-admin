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

package com.devops00.spectra.notification.service;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provider Runtime 的健康门禁、渠道选择和安全阻断测试。
 */
class NotificationProviderRuntimeTest {

    @Test
    void shouldRequireHealthCheckBeforeSendingAndUseSelectedProvider() {
        var configurationService = mock(NotificationProviderAdminService.class);
        var configuration = configuration();
        when(configurationService.resolve(NotificationChannel.SMS)).thenReturn(configuration);
        var provider = new TestProvider();
        var runtime = new NotificationProviderRuntime(configurationService, List.of(provider),
                new NotificationModuleProperties(true, "", "", List.of()));

        assertFalse(runtime.available(NotificationChannel.SMS));
        assertEquals("HEALTH_CHECK_REQUIRED", runtime.unavailableReason(NotificationChannel.SMS));

        assertEquals("HEALTHY", runtime.check(NotificationChannel.SMS).state());
        assertTrue(runtime.available(NotificationChannel.SMS));
        assertEquals("SENT", runtime.send(NotificationChannel.SMS, new NotificationTaskEntity()).status());
        assertTrue(provider.sendCalled);
    }

    @Test
    void shouldInvalidateHealthCacheWhenConfigurationChanges() {
        var configurationService = mock(NotificationProviderAdminService.class);
        var first = configuration();
        var second = new NotificationProviderConfiguration(NotificationChannel.SMS, "HTTP_JSON", true,
                first.endpoint(), first.timeoutMs(), first.rateLimitPerSecond(), first.maxAttempts(),
                first.templateCode(), first.secret(), first.secretKeyId(), first.updatedAt().plusSeconds(1));
        when(configurationService.resolve(NotificationChannel.SMS)).thenReturn(first, second);
        var runtime = new NotificationProviderRuntime(configurationService, List.of(new TestProvider()),
                new NotificationModuleProperties(true, "", "", List.of()));

        runtime.check(NotificationChannel.SMS);
        assertEquals("HEALTH_CHECK_REQUIRED", runtime.snapshot(NotificationChannel.SMS).reason());
    }

    @Test
    void shouldBlockWhenProviderIsNotRegistered() {
        var configurationService = mock(NotificationProviderAdminService.class);
        when(configurationService.resolve(NotificationChannel.EMAIL)).thenReturn(
                new NotificationProviderConfiguration(NotificationChannel.EMAIL, "UNKNOWN", true, "https://example.test",
                        2_000, 10, 3, null, "secret", "key", Instant.now()));
        var runtime = new NotificationProviderRuntime(configurationService, List.of(),
                new NotificationModuleProperties(true, "", "", List.of()));

        var health = runtime.check(NotificationChannel.EMAIL);

        assertEquals("BLOCKED", health.state());
        assertEquals("PROVIDER_NOT_REGISTERED", health.reason());
    }

    private NotificationProviderConfiguration configuration() {
        return new NotificationProviderConfiguration(NotificationChannel.SMS, "HTTP_JSON", true,
                "https://example.test/provider", 2_000, 10, 3, "template-1", "secret", "key-1", Instant.now());
    }

    private static final class TestProvider implements NotificationProvider {

        private boolean sendCalled;

        @Override
        public String code() {
            return "HTTP_JSON";
        }

        @Override
        public boolean supports(NotificationChannel channel) {
            return channel == NotificationChannel.SMS;
        }

        @Override
        public NotificationProviderHealth health(NotificationProviderConfiguration configuration) {
            return new NotificationProviderHealth("HEALTHY", "TEST_OK", Instant.now());
        }

        @Override
        public ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration) {
            sendCalled = true;
            return new ChannelSendResult("SENT", "HTTP_JSON", "message-1", "TEST_SENT");
        }
    }
}
