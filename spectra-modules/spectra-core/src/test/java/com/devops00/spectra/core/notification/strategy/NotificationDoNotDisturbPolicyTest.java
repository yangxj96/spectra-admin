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

package com.devops00.spectra.core.notification.strategy;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证收件人时区缺失回退 UTC、非法值显式拒绝。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/17
 */
class NotificationDoNotDisturbPolicyTest {

    @Test
    void blankTimezoneFallsBackToUtc() {
        assertEquals(ZoneId.of("UTC"), NotificationDoNotDisturbPolicy.resolveZone(null));
        assertEquals(ZoneId.of("UTC"), NotificationDoNotDisturbPolicy.resolveZone("  "));
    }

    @Test
    void validTimezoneIsPreserved() {
        assertEquals(ZoneId.of("Asia/Shanghai"), NotificationDoNotDisturbPolicy.resolveZone("Asia/Shanghai"));
    }

    @Test
    void invalidTimezoneIsRejected() {
        assertThrows(DateTimeException.class, () -> NotificationDoNotDisturbPolicy.resolveZone("Mars/Olympus"));
    }
}
