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

package com.devops00.spectra.notification.dispatch.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import com.devops00.spectra.notification.request.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.request.mapper.NotificationRequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Worker 的租约、过期、失败和 Delivery 隔离测试。 */
class NotificationTaskWorkerTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, JdbcType.OTHER, ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-worker-test");
        TableInfoHelper.initTableInfo(assistant, NotificationTaskEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationRequestEntity.class);
    }

    @Test
    void shouldSendTaskAndKeepDeliveryInSameTenant() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        var completed = task("SENT", task.getScheduledAt(), task.getExpiresAt());
        completed.setId(task.getId());
        completed.setRequestId(task.getRequestId());
        completed.setTenantId(task.getTenantId());
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(task), List.of(completed));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(deliveryMapper.insert(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(sender.send(task)).thenReturn(new ChannelSendResult("SENT", "MOCK_SMS", "provider-1", "accepted"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        var deliveryCaptor = org.mockito.ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(deliveryMapper).insert(deliveryCaptor.capture());
        assertEquals(task.getTenantId(), deliveryCaptor.getValue().getTenantId());
        assertEquals("SENT", deliveryCaptor.getValue().getStatus());
        verify(sender).send(task);
    }

    @Test
    void shouldExpireTaskWithoutCallingSender() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().minusSeconds(1));
        var expired = task("EXPIRED", task.getScheduledAt(), task.getExpiresAt());
        expired.setId(task.getId());
        expired.setRequestId(task.getRequestId());
        expired.setTenantId(task.getTenantId());
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(task), List.of(expired));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        verify(sender, never()).send(any(NotificationTaskEntity.class));
        verify(deliveryMapper, never()).insert(any(NotificationDeliveryEntity.class));
        verify(requestMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldRetrySenderFailureWithoutCreatingDelivery() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        var retrying = task("RETRYING", task.getScheduledAt(), task.getExpiresAt());
        retrying.setId(task.getId());
        retrying.setRequestId(task.getRequestId());
        retrying.setTenantId(task.getTenantId());
        retrying.setRetryCount(1);
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(task), List.of(retrying));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(sender.send(task)).thenThrow(new IllegalStateException("provider unavailable"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        verify(deliveryMapper, never()).insert(any(NotificationDeliveryEntity.class));
        var updateCaptor = org.mockito.ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(taskMapper, org.mockito.Mockito.times(3)).update(isNull(), updateCaptor.capture());
        var failureUpdate = updateCaptor.getAllValues().get(2).getParamNameValuePairs();
        assertTrue(failureUpdate.containsValue("RETRYING"));
        assertTrue(failureUpdate.containsValue(1));
    }

    private NotificationTaskWorker worker(NotificationTaskMapper taskMapper,
            NotificationDeliveryMapper deliveryMapper, NotificationRequestMapper requestMapper,
            List<NotificationSender> senders) {
        var worker = new NotificationTaskWorker(taskMapper, deliveryMapper, requestMapper, senders);
        setField(worker, "processingTimeoutSeconds", 300L);
        setField(worker, "retryBaseDelaySeconds", 1L);
        return worker;
    }

    private NotificationTaskEntity task(String status, Instant scheduledAt, Instant expiresAt) {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setRequestId(UUID.randomUUID());
        task.setTenantId(UUID.randomUUID());
        task.setChannel(NotificationChannel.SMS.name());
        task.setStatus(status);
        task.setRetryCount(0);
        task.setScheduledAt(scheduledAt);
        task.setExpiresAt(expiresAt);
        task.setUpdatedAt(Instant.now());
        return task;
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
