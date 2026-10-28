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
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 HTTP JSON Provider 发送解密后的真实安全通知载荷。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class HttpJsonNotificationProviderTest {

    @Test
    void sendsResolvedSensitivePayload() throws Exception {
        var captured = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            server.createContext("/send", exchange -> {
                captured.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                var response = "{\"status\":\"SENT\",\"messageId\":\"message-1\"}"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();

            var protector = mock(NotificationPayloadProtector.class);
            when(protector.unprotectAddress("recipient-cipher")).thenReturn("13800000000");
            when(protector.unprotectParameters("payload-cipher")).thenReturn(Map.of(
                    "title", "真实标题",
                    "content", "验证码 123456",
                    "parameters", Map.of("code", "123456")));
            var provider = new HttpJsonNotificationProvider(protector, new ObjectMapper());
            var configuration = new NotificationProviderConfiguration(NotificationChannel.SMS, "HTTP_JSON",
                    true, "http://127.0.0.1:" + server.getAddress().getPort() + "/send", 0, null, null, null,
                    null, null, null, false, false, 2000, 0, 1, "template-a", null, "secret", "key", Instant.now());

            var result = provider.send(task(), configuration);

            assertEquals(ChannelSendStatus.SENT, result.status());
            assertTrue(captured.get().contains("真实标题"));
            assertTrue(captured.get().contains("验证码 123456"));
            assertTrue(captured.get().contains("\"code\":\"123456\""));
            assertTrue(captured.get().contains("\"templateCode\":\"template-a\""));
        } finally {
            server.stop(0);
        }
    }

    private static NotificationTaskEntity task() {
        var task = new NotificationTaskEntity();
        task.setId(java.util.UUID.randomUUID());
        task.setChannel("SMS");
        task.setTitle("安全通知");
        task.setContent("敏感通知内容已加密");
        task.setRecipientCiphertext("recipient-cipher");
        task.setSensitiveParametersCiphertext("payload-cipher");
        task.setExtra(Map.of("__provider_template_code", "template-a"));
        return task;
    }
}
