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

import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderHealthState;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 通用 HTTP Provider 的本地沙箱契约测试。
 */
class HttpJsonNotificationProviderTest {

    @Test
    void shouldHealthCheckAndSendProtectedAddressToStandardJsonEndpoint() throws Exception {
        var requestBody = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/provider", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes()));
            var response = "{\"status\":\"ACCEPTED\",\"messageId\":\"message-1\"}".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            var key = Base64.getEncoder().encodeToString(new byte[32]);
            var properties = new NotificationModuleProperties(true, key, key, List.of());
            var protector = new NotificationPayloadProtector(properties, new ObjectMapper());
            var provider = new HttpJsonNotificationProvider(protector, new ObjectMapper());
            var configuration = new NotificationProviderConfiguration(null, "HTTP_JSON", true,
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/provider", 0, null, null, null, null,
                    null, null, false, false, 2_000, 10, 3, "template-1", null, "test-secret", "sms-key",
                    null);
            var task = new NotificationTaskEntity();
            task.setId(UUID.randomUUID());
            task.setChannel("SMS");
            task.setRecipientCiphertext(protector.protectAddress("13800138000"));
            task.setTitle("通知标题");
            task.setContent("通知正文");

            assertEquals(NotificationProviderHealthState.HEALTHY, provider.health(configuration).state());
            var result = provider.send(task, configuration);

            assertEquals(ChannelSendStatus.SENT, result.status());
            assertEquals("message-1", result.providerMessageId());
            assertTrue(requestBody.get().contains("13800138000"));
            assertTrue(requestBody.get().contains("template-1"));
            assertTrue(!requestBody.get().contains("test-secret"));
        } finally {
            server.stop(0);
        }
    }
}
