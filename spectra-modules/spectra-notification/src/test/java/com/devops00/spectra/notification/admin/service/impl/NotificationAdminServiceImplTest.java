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

package com.devops00.spectra.notification.admin.service.impl;

import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.notification.admin.javabean.converter.NotificationAdminConverter;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationTaskAdminVO;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.request.mapper.NotificationRequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 管理端租户边界和查询条件测试。 */
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
    void shouldAddSystemTenantFilterToDeliveryQuery() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var deliveryMapper = mock(NotificationDeliveryMapper.class);
        var converter = mock(NotificationAdminConverter.class);
        var gateway = mock(NotificationGateway.class);
        var page = new Page<NotificationDeliveryEntity>();
        when(deliveryMapper.selectPage(any(), any())).thenReturn(page);
        when(converter.toDeliveryPage(page)).thenReturn(new Page<NotificationDeliveryAdminVO>());
        var service = new NotificationAdminServiceImpl(requestMapper, taskMapper, deliveryMapper, converter, gateway);

        service.pageDeliveries(new PageFrom(), null);

        var wrapperCaptor = org.mockito.ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(deliveryMapper).selectPage(any(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("tenant_id"));
    }

    @Test
    void shouldRejectRetryWhenTaskIsOutsideNotificationTenant() {
        var taskMapper = mock(NotificationTaskMapper.class);
        when(taskMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        var service = new NotificationAdminServiceImpl(mock(NotificationRequestMapper.class), taskMapper,
                mock(NotificationDeliveryMapper.class), mock(NotificationAdminConverter.class),
                mock(NotificationGateway.class));

        assertThrows(DataNotExistException.class, () -> service.retry(UUID.randomUUID()));

        var wrapperCaptor = org.mockito.ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(taskMapper).selectOne(wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getSqlSegment().contains("tenant_id"));
    }
}
