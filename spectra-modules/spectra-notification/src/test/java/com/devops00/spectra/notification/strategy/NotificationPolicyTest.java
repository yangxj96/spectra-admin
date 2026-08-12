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

package com.devops00.spectra.notification.strategy;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知用途与渠道策略测试。
 */
class NotificationPolicyTest {

    private final NotificationPolicy policy = new NotificationPolicy();

    @Test
    void shouldDefaultOptionalPurposeToInApp() {
        assertEquals(List.of(NotificationChannel.IN_APP),
                policy.resolve(NotificationPurpose.SYSTEM_NOTICE, List.of()));
        assertFalse(policy.mandatory(NotificationPurpose.SYSTEM_NOTICE));
    }

    @Test
    void shouldKeepSecurityAlertInApp() {
        assertThrows(DataSaveException.class,
                () -> policy.resolve(NotificationPurpose.SECURITY_ALERT, List.of(NotificationChannel.EMAIL)));
        assertTrue(policy.mandatory(NotificationPurpose.SECURITY_ALERT));
    }

    @Test
    void shouldRequireExternalChannelForVerificationCode() {
        assertThrows(DataSaveException.class,
                () -> policy.resolve(NotificationPurpose.LOGIN_CODE, List.of(NotificationChannel.IN_APP)));
        assertEquals(List.of(NotificationChannel.SMS),
                policy.resolve(NotificationPurpose.LOGIN_CODE, List.of(NotificationChannel.SMS)));
    }
}
