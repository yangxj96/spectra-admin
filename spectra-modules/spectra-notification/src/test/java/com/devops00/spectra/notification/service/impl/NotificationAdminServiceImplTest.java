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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.notification.javabean.converter.NotificationAdminConverter;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.from.NotificationOverviewFrom;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.support.NotificationTestTimeMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 管理端查询条件测试。
 */
class NotificationAdminServiceImplTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, JdbcType.OTHER, ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-admin-test");
        TableInfoHelper.initTableInfo(assistant, NotificationDeliveryEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationTaskEntity.class);
    }

    @Test
    void shouldQueryDeliveriesWithStandaloneDataModel() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var converter = mock(NotificationAdminConverter.class);
        var gateway = mock(NotificationGateway.class);
        var page = new Page<NotificationDeliveryEntity>();
        when(deliveryMapper.selectAdminPage(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(converter.toDeliveryPage(page)).thenReturn(new Page<NotificationDeliveryAdminVO>());
        var service = new NotificationAdminServiceImpl(
                requestMapper, taskMapper, deliveryMapper, converter, gateway, NotificationTestTimeMapper.create());

        service.pageDeliveries(new PageFrom(), null);

        verify(deliveryMapper).selectAdminPage(any(), any(), any(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void shouldRejectRetryWhenTaskDoesNotExist() {
        var taskMapper = mock(NotificationTaskMapper.class);
        when(taskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        var service = new NotificationAdminServiceImpl(mock(NotificationRequestMapper.class), taskMapper,
                mock(NotificationDeliveryMapper.class), mock(NotificationAdminConverter.class),
                mock(NotificationGateway.class), NotificationTestTimeMapper.create());

        assertThrows(DataNotExistException.class, () -> service.retry(UUID.randomUUID()));

        verify(taskMapper).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void shouldRejectOverviewWindowOutsideSevenDays() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var service = new NotificationAdminServiceImpl(requestMapper, taskMapper, deliveryMapper,
                mock(NotificationAdminConverter.class), mock(NotificationGateway.class), NotificationTestTimeMapper.create());

        assertThrows(com.devops00.spectra.common.exception.DataSaveException.class,
                () -> service.overview(new NotificationOverviewFrom(169)));
        verifyNoInteractions(requestMapper, taskMapper, deliveryMapper);
    }

    @Test
    void shouldQueryRequestDetailWithoutSensitivePayload() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var entity = new com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity();
        var view = new NotificationRequestAdminVO();
        var requestId = UUID.randomUUID();
        when(requestMapper.selectById(requestId)).thenReturn(entity);
        var converter = mock(NotificationAdminConverter.class);
        when(converter.toRequestVO(entity)).thenReturn(view);
        var service = new NotificationAdminServiceImpl(requestMapper, mock(NotificationTaskMapper.class),
                mock(NotificationDeliveryMapper.class), converter, mock(NotificationGateway.class),
                NotificationTestTimeMapper.create());

        assertTrue(service.getRequest(requestId) == view);
        verify(converter).toRequestVO(entity);
    }

    @Test
    void shouldQueryTaskDetailThroughTheSanitizedConverter() {
        var taskMapper = mock(NotificationTaskMapper.class);
        var converter = mock(NotificationAdminConverter.class);
        var task = new NotificationTaskEntity();
        var view = new com.devops00.spectra.notification.javabean.vo.NotificationTaskAdminVO();
        var taskId = UUID.randomUUID();
        when(taskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(task);
        when(converter.toTaskVO(task)).thenReturn(view);
        var service = new NotificationAdminServiceImpl(mock(NotificationRequestMapper.class), taskMapper,
                mock(NotificationDeliveryMapper.class), converter, mock(NotificationGateway.class),
                NotificationTestTimeMapper.create());

        assertTrue(service.getTask(taskId) == view);
        verify(converter).toTaskVO(task);
    }

    @Test
    void shouldQueryDeliveryDetailWithTaskChannel() {
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var converter = mock(NotificationAdminConverter.class);
        var delivery = new NotificationDeliveryEntity();
        var task = new NotificationTaskEntity();
        var view = new NotificationDeliveryAdminVO();
        var deliveryId = UUID.randomUUID();
        when(deliveryMapper.selectById(deliveryId)).thenReturn(delivery);
        when(taskMapper.selectById(delivery.getNotificationTaskId())).thenReturn(task);
        when(converter.toDeliveryVO(delivery)).thenReturn(view);
        var service = new NotificationAdminServiceImpl(mock(NotificationRequestMapper.class), taskMapper,
                deliveryMapper, converter, mock(NotificationGateway.class), NotificationTestTimeMapper.create());

        assertTrue(service.getDelivery(deliveryId) == view);
        assertTrue(delivery.getChannel() == task.getChannel());
        verify(converter).toDeliveryVO(delivery);
    }
}
