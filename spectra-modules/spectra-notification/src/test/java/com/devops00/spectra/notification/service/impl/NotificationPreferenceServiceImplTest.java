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

import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 通知偏好首次保存和更新分支测试。
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceImplTest {

    @Mock
    private NotificationUserPreferenceMapper mapper;

    @InjectMocks
    private NotificationPreferenceServiceImpl service;

    @Test
    void shouldInsertNewPreference() {
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(NotificationUserPreferenceEntity.class))).thenReturn(1);

        service.save(UUID.randomUUID(), "SYSTEM_NOTICE", "IN_APP", true, false);

        verify(mapper).insert(any(NotificationUserPreferenceEntity.class));
        verify(mapper, never()).updateById(any(NotificationUserPreferenceEntity.class));
    }

    @Test
    void shouldUpdateExistingPreference() {
        var existing = new NotificationUserPreferenceEntity();
        existing.setId(UUID.randomUUID());
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.updateById(any(NotificationUserPreferenceEntity.class))).thenReturn(1);

        service.save(UUID.randomUUID(), "SYSTEM_NOTICE", "IN_APP", false, true);

        verify(mapper).updateById(existing);
        verify(mapper, never()).insert(any(NotificationUserPreferenceEntity.class));
    }

    @Test
    void shouldKeepMandatorySecurityPreferenceEnabledAndOutOfDoNotDisturb() {
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(NotificationUserPreferenceEntity.class))).thenReturn(1);

        service.save(UUID.randomUUID(), "security_alert", "in_app", false, true);

        verify(mapper).insert(argThat((NotificationUserPreferenceEntity entity) -> Boolean.TRUE.equals(entity.getEnabled())
                && Boolean.FALSE.equals(entity.getDoNotDisturb())
                && "SECURITY_ALERT".equals(entity.getPurpose())
                && "IN_APP".equals(entity.getChannel())));
    }

    @Test
    void shouldPersistDoNotDisturbWindow() {
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(NotificationUserPreferenceEntity.class))).thenReturn(1);
        var start = java.time.LocalDate.of(2026, 8, 13)
                .atTime(22, 0)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant();
        var end = java.time.LocalDate.of(2026, 8, 14)
                .atTime(8, 0)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant();

        service.save(UUID.randomUUID(), "SYSTEM_NOTICE", "IN_APP", true, true, start, end);

        verify(mapper).insert(argThat((NotificationUserPreferenceEntity entity) -> start.equals(entity.getDoNotDisturbStart())
                && end.equals(entity.getDoNotDisturbEnd())));
    }

    @Test
    void shouldExpandLegacyDoNotDisturbTimesUsingUserZone() {
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(NotificationUserPreferenceEntity.class))).thenReturn(1);
        var from = new com.devops00.spectra.notification.javabean.from.NotificationSettingFrom();
        from.setSystemEnabled(true);
        from.setWorkflowEnabled(true);
        from.setOaEnabled(true);
        from.setInnerMailEnabled(true);
        from.setApprovalEnabled(true);
        from.setDoNotDisturb(true);
        from.setDoNotDisturbStart("22:00:00");
        from.setDoNotDisturbEnd("08:00:00");
        var zone = ZoneId.of("Asia/Shanghai");

        service.saveLegacy(UUID.randomUUID(), from, zone);

        var captor = org.mockito.ArgumentCaptor.forClass(NotificationUserPreferenceEntity.class);
        verify(mapper, times(5)).insert(captor.capture());
        assertTrue(captor.getAllValues()
                .stream()
                .allMatch(entity -> LocalTime.of(22, 0).equals(entity.getDoNotDisturbStart().atZone(zone).toLocalTime())
                        && LocalTime.of(8, 0).equals(entity.getDoNotDisturbEnd().atZone(zone).toLocalTime())));
    }

    @Test
    void shouldProjectLegacyDoNotDisturbTimesUsingUserZone() {
        var zone = ZoneId.of("Asia/Shanghai");
        var preference = new NotificationUserPreferenceEntity();
        preference.setPurpose("SYSTEM_NOTICE");
        preference.setChannel("IN_APP");
        preference.setEnabled(true);
        preference.setDoNotDisturb(true);
        preference.setDoNotDisturbStart(java.time.LocalDate.of(2026, 8, 13).atTime(22, 0).atZone(zone).toInstant());
        preference.setDoNotDisturbEnd(java.time.LocalDate.of(2026, 8, 14).atTime(8, 0).atZone(zone).toInstant());
        when(mapper.selectList(any())).thenReturn(List.of(preference));

        var result = service.legacy(UUID.randomUUID(), zone);

        assertTrue(Boolean.TRUE.equals(result.getDoNotDisturb()));
        org.junit.jupiter.api.Assertions.assertEquals(LocalTime.of(22, 0), result.getDoNotDisturbStart());
        org.junit.jupiter.api.Assertions.assertEquals(LocalTime.of(8, 0), result.getDoNotDisturbEnd());
    }
}
