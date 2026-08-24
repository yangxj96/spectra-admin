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

package com.devops00.spectra.notification.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.dispatch.NotificationRequestStatusUpdater;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Provider 回执验签、状态更新和重复回执幂等测试。
 */
class NotificationProviderCallbackServiceTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry()
                .register(UUID.class, JdbcType.OTHER,
                        ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-callback-test");
        TableInfoHelper.initTableInfo(assistant, NotificationTaskEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationRequestEntity.class);
    }

    @Test
    void shouldApplyAndDeduplicateSignedCallback() throws Exception {
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var providerAdminService = mock(NotificationProviderAdminService.class);
        var configuration = configuration();
        var taskId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var delivery = new NotificationDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setNotificationTaskId(taskId);
        delivery.setResultStatus("SENT");
        delivery.setResponseSummary(Map.of());
        var task = new NotificationTaskEntity();
        task.setId(taskId);
        task.setNotificationRequestId(requestId);
        task.setStatus("SENT");
        when(providerAdminService.resolve(NotificationChannel.SMS)).thenReturn(configuration);
        when(deliveryMapper.selectByProviderMessageId("HTTP_JSON", "message-1", "SMS")).thenReturn(delivery);
        when(deliveryMapper.updateById(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(taskMapper.selectList(any())).thenReturn(List.of(task));
        when(taskMapper.update(any(), any())).thenReturn(1);
        when(requestMapper.update(any(), any())).thenReturn(1);

        var service = new NotificationProviderCallbackServiceImpl(deliveryMapper, taskMapper,
                new NotificationRequestStatusUpdater(taskMapper, requestMapper),
                providerAdminService, new ObjectMapper());
        var body = "{\"messageId\":\"message-1\",\"status\":\"DELIVERED\"}";
        var signature = signature(body, configuration.secret());

        var applied = service.handle(NotificationChannel.SMS, signature, body);
        var duplicate = service.handle(NotificationChannel.SMS, signature, body);

        assertEquals("APPLIED", applied.status());
        assertEquals("SENT", applied.resultStatus());
        assertEquals("DUPLICATE", duplicate.status());
        verify(deliveryMapper, times(1)).updateById(any(NotificationDeliveryEntity.class));
        verify(taskMapper, times(1)).update(any(), any());
    }

    @Test
    void shouldRejectInvalidCallbackSignature() {
        var providerAdminService = mock(NotificationProviderAdminService.class);
        when(providerAdminService.resolve(NotificationChannel.EMAIL)).thenReturn(configuration());
        var taskMapper = mock(NotificationTaskMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var service = new NotificationProviderCallbackServiceImpl(mock(NotificationDeliveryMapper.class), taskMapper,
                new NotificationRequestStatusUpdater(taskMapper, requestMapper), providerAdminService,
                new ObjectMapper());

        assertThrows(DataSaveException.class,
                () -> service.handle(NotificationChannel.EMAIL, "sha256=" + "0".repeat(64),
                        "{\"messageId\":\"message-1\",\"status\":\"FAILED\"}"));
    }

    private NotificationProviderConfiguration configuration() {
        return new NotificationProviderConfiguration(NotificationChannel.SMS, "HTTP_JSON", true,
                "https://provider.example/send", 0, null, null, null, null, null, null, false, false,
                5_000, 10, 3, null, null, "callback-secret", "sms-key", null);
    }

    private String signature(String body, String secret) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
