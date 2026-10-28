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

package com.devops00.spectra.core.notification.provider.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderHealthState;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证腾讯云短信不再显式设置 JDK 受限 Host 头，且健康检查读取嵌套响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class TencentSmsNotificationProviderTest {

    @Test
    void healthAndSendWorkAgainstNestedResponse() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            server.createContext("/", exchange -> {
                var action = exchange.getRequestHeaders().getFirst("X-TC-Action");
                var response = "SendSms".equals(action)
                        ? "{\"Response\":{\"RequestId\":\"r1\",\"SendStatusSet\":[{\"Code\":\"Ok\","
                                + "\"SerialNo\":\"serial-1\"}]}}"
                        : "{\"Response\":{\"RequestId\":\"r2\",\"TemplateStatusSet\":[]}}";
                var bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            var protector = mock(NotificationPayloadProtector.class);
            when(protector.unprotectAddress("recipient-cipher")).thenReturn("13800000000");
            var provider = new TencentSmsNotificationProvider(protector, new ObjectMapper());
            var configuration = new NotificationProviderConfiguration(NotificationChannel.SMS, "TENCENT_SMS",
                    true, "http://127.0.0.1:" + server.getAddress().getPort() + "/", 0, "ap-test", "secret-id",
                    "app-id", "sign", null, null, false, false, 3000, 0, 1, "template-a", null, "secret", "key",
                    Instant.now());

            var health = provider.health(configuration);
            var result = provider.send(task(), configuration);

            assertEquals(NotificationProviderHealthState.HEALTHY, health.state());
            assertEquals(ChannelSendStatus.SENT, result.status());
        } finally {
            server.stop(0);
        }
    }

    private static NotificationTaskEntity task() {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setChannel("SMS");
        task.setTitle("title");
        task.setContent("content");
        task.setRecipientCiphertext("recipient-cipher");
        task.setExtra(java.util.Map.of());
        return task;
    }
}
