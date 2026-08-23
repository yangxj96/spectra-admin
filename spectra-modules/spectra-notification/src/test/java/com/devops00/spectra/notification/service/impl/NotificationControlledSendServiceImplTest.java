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
import com.devops00.spectra.common.notification.NotificationAudienceDirectory;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.notification.javabean.entity.NotificationSendPreviewEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.javabean.from.NotificationAudienceFrom;
import com.devops00.spectra.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.notification.javabean.from.NotificationControlledSendFrom;
import com.devops00.spectra.notification.mapper.NotificationSendPreviewMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 受控发送 Preview/Apply、一次性消费和 Gateway 串联测试。
 */
class NotificationControlledSendServiceImplTest {

    private static final UUID OPERATOR_ID = UUID.randomUUID();

    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    private static final UUID TEMPLATE_ID = UUID.randomUUID();

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, JdbcType.OTHER, ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-controlled-send-test");
        TableInfoHelper.initTableInfo(assistant, NotificationSendPreviewEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationTaskEntity.class);
    }

    @Test
    void shouldPreviewThenApplyExactlyOnceThroughGateway() {
        var previewMapper = mock(NotificationSendPreviewMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var gateway = mock(NotificationGateway.class);
        var audienceDirectory = mock(NotificationAudienceDirectory.class);
        var recipientDirectory = mock(NotificationRecipientDirectory.class);
        var security = mock(SecurityContextAccessor.class);
        var template = template();
        var request = request();
        var preview = new NotificationSendPreviewEntity();
        preview.setId(UUID.randomUUID());
        when(security.currentUserId()).thenReturn(OPERATOR_ID);
        when(templateMapper.selectById(TEMPLATE_ID)).thenReturn(template);
        when(audienceDirectory.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(recipientDirectory.resolve(List.of(RECIPIENT_ID)))
                .thenReturn(List.of(new NotificationRecipient(RECIPIENT_ID, "13800138000", null, true, true, "Asia/Shanghai")));
        when(preferenceMapper.selectList(any())).thenReturn(List.of(preference()));
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, true, "AVAILABLE"));
        when(previewMapper.insert(any(NotificationSendPreviewEntity.class))).thenReturn(1);
        when(previewMapper.update(any(), any())).thenReturn(1);
        when(previewMapper.updateById(any(NotificationSendPreviewEntity.class))).thenReturn(1);
        when(gateway.enqueue(any(), any())).thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        when(taskMapper.selectCount(any())).thenReturn(1L);

        var service = service(previewMapper, taskMapper, templateMapper, preferenceMapper, gateway,
                audienceDirectory, recipientDirectory, security);
        var result = service.preview(request);
        when(previewMapper.selectById(result.previewId())).thenReturn(preview);
        preview.setId(result.previewId());
        preview.setOperatorUserId(OPERATOR_ID);
        preview.setRequestHash(result.requestHash());
        preview.setPreviewTokenHash(hash(result.previewToken()));
        preview.setResolutionHash(hash(List.of(RECIPIENT_ID + ":SMS:null")));
        preview.setRequestSnapshot(snapshot(request));
        preview.setExpiresAt(result.expiresAt());
        preview.setStatus("PREVIEWED");

        var apply = new NotificationControlledSendApplyFrom();
        apply.setPreviewId(result.previewId());
        apply.setPreviewToken(result.previewToken());
        apply.setRequestHash(result.requestHash());
        var applied = service.apply(apply);
        var replay = service.apply(apply);

        assertEquals("ACCEPTED", applied.status());
        assertEquals(1, applied.taskCount());
        assertTrue(replay.idempotentReplay());
        verify(gateway, times(1)).enqueue(any(), any());
    }

    private NotificationControlledSendServiceImpl service(NotificationSendPreviewMapper previewMapper,
                                                          NotificationTaskMapper taskMapper,
                                                          NotificationTemplateMapper templateMapper,
                                                          NotificationUserPreferenceMapper preferenceMapper,
                                                          NotificationGateway gateway,
                                                          NotificationAudienceDirectory audienceDirectory,
                                                          NotificationRecipientDirectory recipientDirectory,
                                                          SecurityContextAccessor security) {
        return new NotificationControlledSendServiceImpl(previewMapper, taskMapper, templateMapper, preferenceMapper,
                gateway, audienceDirectory, recipientDirectory, new com.devops00.spectra.notification.strategy.NotificationPolicy(),
                new NotificationTemplateRenderer(), security, new ObjectMapper());
    }

    private NotificationControlledSendFrom request() {
        var audience = new NotificationAudienceFrom();
        audience.setUserIds(List.of(RECIPIENT_ID));
        var request = new NotificationControlledSendFrom();
        request.setIdempotencyKey("controlled:one");
        request.setPurpose(NotificationPurpose.SYSTEM_NOTICE);
        request.setChannels(List.of(NotificationChannel.SMS));
        request.setTemplateVersionIds(Map.of(NotificationChannel.SMS, TEMPLATE_ID));
        request.setAudience(audience);
        request.setParameters(Map.of("title", "系统通知", "content", "请处理待办"));
        return request;
    }

    private NotificationTemplateEntity template() {
        var template = new NotificationTemplateEntity();
        template.setId(TEMPLATE_ID);
        template.setTemplateGroupCode("system-notice");
        template.setChannel(NotificationChannel.SMS.name());
        template.setPurpose(NotificationPurpose.SYSTEM_NOTICE.name());
        template.setVersionNo(1);
        template.setVersionDigest("digest");
        template.setTitleTemplate("{{title}}");
        template.setContentTemplate("{{content}}");
        template.setState("PUBLISHED");
        return template;
    }

    private NotificationUserPreferenceEntity preference() {
        var preference = new NotificationUserPreferenceEntity();
        preference.setUserId(RECIPIENT_ID);
        preference.setPurpose(NotificationPurpose.SYSTEM_NOTICE.name());
        preference.setChannel(NotificationChannel.SMS.name());
        preference.setEnabled(true);
        preference.setDoNotDisturb(false);
        return preference;
    }

    private Map<String, Object> snapshot(NotificationControlledSendFrom request) {
        return new java.util.LinkedHashMap<>(Map.of(
                "idempotencyKey", request.getIdempotencyKey(),
                "purpose", request.getPurpose().name(),
                "channels", List.of(NotificationChannel.SMS.name()),
                "templateVersionIds", Map.of(NotificationChannel.SMS.name(), TEMPLATE_ID.toString()),
                "audience", Map.of("userIds", List.of(RECIPIENT_ID.toString()), "departmentIds", List.of(), "roleIds", List.of()),
                "parameters", request.getParameters()));
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of()
                    .formatHex(java.security.MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String hash(List<String> values) {
        return hash(String.join("|", values));
    }
}
