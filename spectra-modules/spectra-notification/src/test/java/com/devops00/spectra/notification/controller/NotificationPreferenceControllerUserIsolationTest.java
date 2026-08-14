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

package com.devops00.spectra.notification.controller;

import com.devops00.spectra.notification.service.NotificationPreferenceService;
import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知偏好 Self API 的用户绑定回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/13
 */
class NotificationPreferenceControllerUserIsolationTest {

    private static final UUID USER_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    private static final UUID USER_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private SecHolderStrategy security;

    @BeforeEach
    void setUpSecurityHolder() {
        security = mock(SecHolderStrategy.class);
        SecUtil.setHolder(security);
    }

    @AfterEach
    void clearSecurityHolder() {
        SecUtil.setHolder(null);
    }

    @Test
    void shouldBindPreferenceReadsAndWritesToTheActiveUser() {
        var service = mock(NotificationPreferenceService.class);
        var controller = new NotificationPreferenceController(service);
        when(security.getCurrentUserId()).thenReturn(USER_A, USER_A, USER_B);

        controller.list();
        controller.save("SYSTEM_NOTICE", "IN_APP", false, true);
        controller.save("OA_NOTICE", "IN_APP", false, false);

        verify(service).list(USER_A);
        verify(service).save(USER_A, "SYSTEM_NOTICE", "IN_APP", false, true);
        verify(service).save(USER_B, "OA_NOTICE", "IN_APP", false, false);
    }

    @Test
    void shouldRejectPreferenceAccessWithoutAnAuthenticatedUser() {
        var service = mock(NotificationPreferenceService.class);
        var controller = new NotificationPreferenceController(service);
        when(security.getCurrentUserId()).thenReturn(null);

        assertThrows(IllegalStateException.class, controller::list);
    }
}
