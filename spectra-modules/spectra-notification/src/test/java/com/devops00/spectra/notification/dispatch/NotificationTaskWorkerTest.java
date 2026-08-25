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

package com.devops00.spectra.notification.dispatch;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.sender.NotificationSender;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Worker 的租约、过期、失败和 Delivery 测试。
 */
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
    void shouldSendTaskAndCreateDelivery() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        var completed = task("SENT", task.getScheduledAt(), task.getExpiresAt());
        completed.setId(task.getId());
        completed.setNotificationRequestId(task.getNotificationRequestId());
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(completed));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(deliveryMapper.insert(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(sender.send(task)).thenReturn(ChannelSendResult.sent("MOCK_SMS", "provider-1", "accepted"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        var deliveryCaptor = org.mockito.ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(deliveryMapper).insert(deliveryCaptor.capture());
        assertEquals("SENT", deliveryCaptor.getValue().getResultStatus());
        verify(sender).send(task);
    }

    @Test
    void shouldKeepSuccessfulInAppSummary() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        task.setChannel(NotificationChannel.IN_APP.name());
        var completed = task("SENT", task.getScheduledAt(), task.getExpiresAt());
        completed.setId(task.getId());
        completed.setNotificationRequestId(task.getNotificationRequestId());
        completed.setChannel(NotificationChannel.IN_APP.name());
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(completed));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(deliveryMapper.insert(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.IN_APP);
        when(sender.send(task)).thenReturn(ChannelSendResult.sent("IN_APP", null, "站内信已写入收件箱"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        var deliveryCaptor = org.mockito.ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(deliveryMapper).insert(deliveryCaptor.capture());
        assertEquals("SENT", deliveryCaptor.getValue().getResultStatus());
        assertEquals(Map.of("summary", "站内信已写入收件箱"), deliveryCaptor.getValue().getResponseSummary());
        assertNull(deliveryCaptor.getValue().getErrorMessageSanitized());
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
        expired.setNotificationRequestId(task.getNotificationRequestId());
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(expired));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        verify(sender, never()).send(any(NotificationTaskEntity.class));
        verify(deliveryMapper, never()).insert(any(NotificationDeliveryEntity.class));
        verify(requestMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldRecordUnknownProviderFailureWithoutRetry() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        var unknown = task("UNKNOWN", task.getScheduledAt(), task.getExpiresAt());
        unknown.setId(task.getId());
        unknown.setNotificationRequestId(task.getNotificationRequestId());
        unknown.setAttemptCount(1);
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(unknown));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(deliveryMapper.insert(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(sender.send(task)).thenThrow(new IllegalStateException("provider unavailable"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        var deliveryCaptor = org.mockito.ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(deliveryMapper).insert(deliveryCaptor.capture());
        assertEquals("UNKNOWN", deliveryCaptor.getValue().getResultStatus());
        var updateCaptor = org.mockito.ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(taskMapper, org.mockito.Mockito.times(3)).update(isNull(), updateCaptor.capture());
        var resultUpdate = updateCaptor.getAllValues().get(2).getParamNameValuePairs();
        assertTrue(resultUpdate.containsValue("UNKNOWN"));
        assertTrue(resultUpdate.containsValue(1));
    }

    @Test
    void shouldRetryOnlyExplicitRateLimitedResult() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("PENDING", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        task.setMaxAttempts(3);
        var retrying = task("RETRYING", task.getScheduledAt(), task.getExpiresAt());
        retrying.setId(task.getId());
        retrying.setNotificationRequestId(task.getNotificationRequestId());
        retrying.setAttemptCount(1);
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(retrying));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(deliveryMapper.insert(any(NotificationDeliveryEntity.class))).thenReturn(1);
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(sender.send(task)).thenReturn(ChannelSendResult.failed("RATE_LIMITED", null,
                "PROVIDER_RATE_LIMITED"));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        var deliveryCaptor = org.mockito.ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(deliveryMapper).insert(deliveryCaptor.capture());
        assertEquals("FAILED", deliveryCaptor.getValue().getResultStatus());
        var updateCaptor = org.mockito.ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(taskMapper, org.mockito.Mockito.times(3)).update(isNull(), updateCaptor.capture());
        var resultUpdate = updateCaptor.getAllValues().get(2).getParamNameValuePairs();
        assertTrue(resultUpdate.containsValue("RETRYING"));
        assertTrue(resultUpdate.containsValue(1));
    }

    @Test
    void shouldNotAutomaticallyRetryUnknownTask() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var requestMapper = mock(NotificationRequestMapper.class);
        var sender = mock(NotificationSender.class);
        var task = task("UNKNOWN", Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        when(taskMapper.selectPendingTasks(any(Instant.class), anyInt())).thenReturn(List.of(task));
        when(taskMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(task));
        var worker = worker(taskMapper, deliveryMapper, requestMapper, List.of(sender));

        assertEquals(1, worker.processPending(50));

        verify(sender, never()).send(any(NotificationTaskEntity.class));
        verify(deliveryMapper, never()).insert(any(NotificationDeliveryEntity.class));
        verify(taskMapper, times(2)).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    private NotificationTaskWorker worker(NotificationTaskMapper taskMapper,
                                          NotificationDeliveryMapper deliveryMapper, NotificationRequestMapper requestMapper,
                                          List<NotificationSender> senders) {
        var worker = new NotificationTaskWorker(taskMapper, deliveryMapper,
                new NotificationRequestStatusUpdater(taskMapper, requestMapper), senders);
        setField(worker, "processingTimeoutSeconds", 300L);
        setField(worker, "retryBaseDelaySeconds", 1L);
        return worker;
    }

    private NotificationTaskEntity task(String status, Instant scheduledAt, Instant expiresAt) {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setNotificationRequestId(UUID.randomUUID());
        task.setChannel(NotificationChannel.SMS.name());
        task.setStatus(status);
        task.setAttemptCount(0);
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
