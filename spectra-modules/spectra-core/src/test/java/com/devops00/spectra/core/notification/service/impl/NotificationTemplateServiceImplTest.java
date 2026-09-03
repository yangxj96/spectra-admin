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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.notification.javabean.converter.NotificationTemplateConverter;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.core.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.core.notification.strategy.NotificationPolicy;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知模板展示版本选择测试。
 */
class NotificationTemplateServiceImplTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(java.util.UUID.class, JdbcType.OTHER, ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-template-test");
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(assistant, NotificationTemplateEntity.class);
    }

    @Test
    void shouldPreferLatestPublishedVersionAndFallbackToHighestVersion() {
        var mapper = mock(NotificationTemplateMapper.class);
        var converter = mock(NotificationTemplateConverter.class);
        var publishedV1 = template("notice", 1, "PUBLISHED", "2026-08-01T00:00:00Z");
        var publishedV2 = template("notice", 2, "PUBLISHED", "2026-07-01T00:00:00Z");
        var draftV3 = template("notice", 3, "DRAFT", "2026-08-03T00:00:00Z");
        var archivedV4 = template("notice", 4, "ARCHIVED", "2026-08-04T00:00:00Z");
        var draftWithoutPublished = template("draft-only", 3, "DRAFT", "2026-08-03T00:00:00Z");
        var archivedWithoutPublished = template("draft-only", 4, "ARCHIVED", "2026-08-04T00:00:00Z");
        when(mapper.selectList(any())).thenReturn(List.of(
                draftV3, archivedV4, publishedV1, publishedV2, draftWithoutPublished, archivedWithoutPublished));
        var converted = new Page<NotificationTemplateVO>();
        when(converter.toPage(any())).thenReturn(converted);
        var service = new NotificationTemplateServiceImpl(
                mapper, converter, mock(NotificationTemplateRenderer.class), mock(NotificationPolicy.class),
                mock(TimeMapper.class));

        service.page(new PageFrom(), null);

        var pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(converter).toPage(pageCaptor.capture());
        @SuppressWarnings("unchecked")
        var page = (Page<NotificationTemplateEntity>) pageCaptor.getValue();
        assertEquals(2, page.getTotal());
        assertEquals(2, page.getRecords().size());
        assertSame(publishedV2, page.getRecords().get(0));
        assertSame(archivedWithoutPublished, page.getRecords().get(1));
    }

    @Test
    void shouldInitializeOptimisticVersionWhenCreatingDraft() {
        var mapper = mock(NotificationTemplateMapper.class);
        var converter = mock(NotificationTemplateConverter.class);
        var renderer = mock(NotificationTemplateRenderer.class);
        var policy = mock(NotificationPolicy.class);
        when(policy.parsePurpose("SYSTEM_NOTICE")).thenReturn(NotificationPurpose.SYSTEM_NOTICE);
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.selectList(any())).thenReturn(List.of());
        var inserted = new NotificationTemplateEntity[1];
        when(mapper.insert(org.mockito.ArgumentMatchers.<NotificationTemplateEntity>any())).thenAnswer(invocation -> {
            inserted[0] = invocation.getArgument(0, NotificationTemplateEntity.class);
            return 1;
        });
        var service = new NotificationTemplateServiceImpl(mapper, converter, renderer, policy, mock(TimeMapper.class));

        var params = new NotificationTemplateSaveFrom();
        params.setTemplateGroupCode("notice");
        params.setTemplateName("系统通知");
        params.setChannel(NotificationChannel.IN_APP);
        params.setPurpose("SYSTEM_NOTICE");
        params.setContentTemplate("系统通知：{{content}}");
        params.setParameterSchema(Map.of("properties", Map.of("content", Map.of("type", "string"))));

        service.create(params);

        verify(mapper).insert(inserted[0]);
        assertEquals(0L, inserted[0].getVersion());
    }

    private static NotificationTemplateEntity template(String groupCode, int version, String state, String updatedAt) {
        var entity = new NotificationTemplateEntity();
        entity.setTemplateGroupCode(groupCode);
        entity.setChannel("IN_APP");
        entity.setVersionNo(version);
        entity.setState(state);
        entity.setUpdatedAt(Instant.parse(updatedAt));
        return entity;
    }
}
