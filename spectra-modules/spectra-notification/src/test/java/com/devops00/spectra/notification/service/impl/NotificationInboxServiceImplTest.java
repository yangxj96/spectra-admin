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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.javabean.converter.NotificationInboxConverter;
import com.devops00.spectra.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.mapper.NotificationInboxMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * 消息中心 Self API 服务的收件人所有权测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/13
 */
@ExtendWith(MockitoExtension.class)
class NotificationInboxServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final UUID MESSAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock
    private NotificationInboxMapper mapper;

    @Mock
    private NotificationInboxConverter converter;

    @Mock
    private TimeMapper timeMapper;

    @InjectMocks
    private NotificationInboxServiceImpl service;

    @BeforeAll
    static void initializeMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-inbox-test");
        TableInfoHelper.initTableInfo(assistant, NotificationInboxEntity.class);
    }

    @Test
    void shouldScopePageAndUnreadCountToReceiver() {
        var page = new Page<NotificationInboxEntity>();
        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(converter.toVOPage(page)).thenReturn(new Page<>());
        when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        service.page(new PageFrom(), USER_ID, new NotificationQueryFrom());
        var pageCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), pageCaptor.capture());
        var pageQuery = (LambdaQueryWrapper<NotificationInboxEntity>) pageCaptor.getValue();
        assertScoped(pageQuery);

        assertEquals(2L, service.unreadCount(USER_ID));
        var unreadCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper).selectCount(unreadCaptor.capture());
        var unreadQuery = (LambdaQueryWrapper<NotificationInboxEntity>) unreadCaptor.getValue();
        assertScoped(unreadQuery);
    }

    @Test
    void shouldHideForeignMessageAsNotFound() {
        when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.detail(MESSAGE_ID, USER_ID));

        var query = captureQuery();
        assertScoped(query);
        assertTrue(query.getParamNameValuePairs().containsValue(MESSAGE_ID));
    }

    @Test
    void shouldProtectSingleReadAndDeleteWithOwnershipConditions() {
        when(mapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);
        when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThrows(DataNotExistException.class, () -> service.markAsRead(MESSAGE_ID, USER_ID));
        var readUpdate = captureUpdate();
        assertScoped(readUpdate);

        assertThrows(DataNotExistException.class, () -> service.deleteById(MESSAGE_ID, USER_ID));
        var deleteUpdate = captureUpdate();
        assertScoped(deleteUpdate);
    }

    @Test
    void shouldRestrictMarkAllAndBatchDeleteToCurrentReceiver() {
        when(mapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        var otherMessageId = UUID.randomUUID();

        service.markAllAsRead(USER_ID);
        var markAllUpdate = captureUpdate();
        assertScoped(markAllUpdate);

        service.batchDelete(List.of(MESSAGE_ID, otherMessageId), USER_ID);
        var batchUpdate = captureUpdate();
        assertScoped(batchUpdate);
        assertTrue(batchUpdate.getParamNameValuePairs().containsValue(MESSAGE_ID));
        assertTrue(batchUpdate.getParamNameValuePairs().containsValue(otherMessageId));
    }

    private LambdaQueryWrapper<NotificationInboxEntity> captureQuery() {
        var captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(mapper, org.mockito.Mockito.atLeastOnce()).selectOne(captor.capture());
        return captor.getValue();
    }

    private LambdaUpdateWrapper<NotificationInboxEntity> captureUpdate() {
        var captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper, org.mockito.Mockito.atLeastOnce()).update(isNull(), captor.capture());
        return captor.getValue();
    }

    private void assertScoped(com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?> wrapper) {
        assertTrue(wrapper.getSqlSegment().contains("receiver_user_id"),
                () -> wrapper.getSqlSegment() + " / " + wrapper.getParamNameValuePairs());
    }
}
