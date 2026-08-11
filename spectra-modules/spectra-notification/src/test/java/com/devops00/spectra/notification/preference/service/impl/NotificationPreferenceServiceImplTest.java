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

package com.devops00.spectra.notification.preference.service.impl;

import java.util.UUID;

import com.devops00.spectra.notification.preference.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.preference.mapper.NotificationUserPreferenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通知偏好首次保存和更新分支测试。 */
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

        service.save(UUID.randomUUID(), UUID.randomUUID(), "SYSTEM_NOTICE", "IN_APP", true, false);

        verify(mapper).insert(any(NotificationUserPreferenceEntity.class));
        verify(mapper, never()).updateById(any(NotificationUserPreferenceEntity.class));
    }

    @Test
    void shouldUpdateExistingPreference() {
        var existing = new NotificationUserPreferenceEntity();
        existing.setId(UUID.randomUUID());
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.updateById(any(NotificationUserPreferenceEntity.class))).thenReturn(1);

        service.save(UUID.randomUUID(), UUID.randomUUID(), "SYSTEM_NOTICE", "IN_APP", false, true);

        verify(mapper).updateById(existing);
        verify(mapper, never()).insert(any(NotificationUserPreferenceEntity.class));
    }
}
