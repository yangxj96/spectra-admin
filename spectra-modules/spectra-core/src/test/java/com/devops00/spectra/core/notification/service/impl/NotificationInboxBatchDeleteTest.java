/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.notification.javabean.converter.NotificationInboxConverter;
import com.devops00.spectra.core.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.core.notification.mapper.NotificationInboxMapper;
import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationInboxBatchDeleteTest {

    @BeforeAll
    static void initializeTableMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, new UUIDTypeHandler());
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), NotificationInboxEntity.class);
    }

    private final NotificationInboxMapper mapper = mock(NotificationInboxMapper.class);
    private final NotificationInboxConverter converter = mock(NotificationInboxConverter.class);
    private final TimeMapper timeMapper = mock(TimeMapper.class);

    @Test
    void batchDeleteRejectsMixedOwnershipWithoutPartialUpdate() {
        var userId = UUID.randomUUID();
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(mapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataNotExistException.class, () -> service().batchDelete(ids, userId));

        verify(mapper, never()).update(any(), any());
    }

    @Test
    void batchDeleteUpdatesAllOwnedMessages() {
        var userId = UUID.randomUUID();
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(mapper.selectCount(any())).thenReturn(2L);
        when(mapper.update(any(), any())).thenReturn(2);

        service().batchDelete(ids, userId);

        verify(mapper).update(any(), any());
    }

    @Test
    void duplicateIdsAreValidatedOnce() {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();
        when(mapper.selectCount(any())).thenReturn(1L);
        when(mapper.update(any(), any())).thenReturn(1);

        service().batchDelete(List.of(id, id), userId);

        verify(mapper).selectCount(any());
        verify(mapper).update(any(), any());
    }

    private NotificationInboxServiceImpl service() {
        return new NotificationInboxServiceImpl(mapper, converter, timeMapper);
    }
}
