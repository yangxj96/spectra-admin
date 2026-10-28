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

package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.dispatch.NotificationRequestStatusUpdater;
import com.devops00.spectra.core.notification.javabean.domain.NotificationCallbackStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.service.NotificationProviderAdminService;
import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 Provider 回执只接受当前 attempt，并遵守终态单调迁移。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class NotificationProviderCallbackServiceTest {

    private static final String SECRET = "callback-secret";

    @BeforeAll
    static void initializeTableMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, new UUIDTypeHandler());
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""),
                NotificationTaskEntity.class);
    }

    private final NotificationDeliveryMapper deliveryMapper = mock(NotificationDeliveryMapper.class);
    private final NotificationTaskMapper taskMapper = mock(NotificationTaskMapper.class);
    private final NotificationRequestStatusUpdater requestStatusUpdater = mock(NotificationRequestStatusUpdater.class);
    private final NotificationProviderAdminService providerAdminService = mock(NotificationProviderAdminService.class);
    private final NotificationProviderCallbackServiceImpl service = new NotificationProviderCallbackServiceImpl(
            deliveryMapper, taskMapper, requestStatusUpdater, providerAdminService, new ObjectMapper());

    @Test
    void rejectsCallbackForOlderAttempt() {
        var delivery = delivery(1, "SENT");
        var task = task(2, "SENT");
        stubLookup(delivery, task);
        var body = body(delivery.getProviderMessageId(), "FAILED");

        var result = service.handle(NotificationChannel.SMS, signature(body), body);

        assertEquals(NotificationCallbackStatus.STALE.name(), result.status());
        verify(deliveryMapper, never()).updateById(any(NotificationDeliveryEntity.class));
    }

    @Test
    void rejectsSuccessAfterTerminalFailure() {
        var delivery = delivery(2, "FAILED");
        var task = task(2, "FAILED");
        stubLookup(delivery, task);
        var body = body(delivery.getProviderMessageId(), "SENT");

        var result = service.handle(NotificationChannel.SMS, signature(body), body);

        assertEquals(NotificationCallbackStatus.STALE.name(), result.status());
        verify(deliveryMapper, never()).updateById(any(NotificationDeliveryEntity.class));
    }

    @Test
    void appliesCallbackForCurrentAttempt() {
        var delivery = delivery(2, "SENT");
        var task = task(2, "SENT");
        stubLookup(delivery, task);
        when(deliveryMapper.updateById(delivery)).thenReturn(1);
        when(taskMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        var body = body(delivery.getProviderMessageId(), "FAILED");

        var result = service.handle(NotificationChannel.SMS, signature(body), body);

        assertEquals(NotificationCallbackStatus.APPLIED.name(), result.status());
        verify(deliveryMapper).updateById(delivery);
        verify(requestStatusUpdater).refresh(task.getNotificationRequestId());
    }

    private void stubLookup(NotificationDeliveryEntity delivery, NotificationTaskEntity task) {
        delivery.setNotificationTaskId(task.getId());
        when(providerAdminService.resolve(NotificationChannel.SMS)).thenReturn(configuration());
        when(deliveryMapper.selectByProviderMessageId("MOCK", delivery.getProviderMessageId(), "SMS"))
                .thenReturn(delivery);
        when(taskMapper.selectById(task.getId())).thenReturn(task);
    }

    private static NotificationDeliveryEntity delivery(int attemptNo, String status) {
        var delivery = new NotificationDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setNotificationTaskId(UUID.randomUUID());
        delivery.setAttemptNo(attemptNo);
        delivery.setProviderMessageId("message-" + UUID.randomUUID());
        delivery.setResultStatus(status);
        delivery.setResponseSummary(Map.of());
        return delivery;
    }

    private static NotificationTaskEntity task(int attemptCount, String status) {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setNotificationRequestId(UUID.randomUUID());
        task.setAttemptCount(attemptCount);
        task.setStatus(status);
        return task;
    }

    private static NotificationProviderConfiguration configuration() {
        return new NotificationProviderConfiguration(NotificationChannel.SMS, "MOCK", true, "", 0, null, null,
                null, null, null, null, false, false, 0, 0, 1, null, null, SECRET, "key", Instant.now());
    }

    private static String body(String messageId, String status) {
        return "{\"messageId\":\"" + messageId + "\",\"status\":\"" + status + "\"}";
    }

    private static String signature(String body) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
