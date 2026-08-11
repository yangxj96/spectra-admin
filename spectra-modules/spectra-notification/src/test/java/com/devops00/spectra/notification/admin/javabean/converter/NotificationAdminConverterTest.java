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

package com.devops00.spectra.notification.admin.javabean.converter;

import java.util.UUID;

import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** 管理端地址、错误和投递摘要脱敏测试。 */
class NotificationAdminConverterTest {

    private final NotificationAdminConverter converter = new NotificationAdminConverterImpl(new TimeMapper());

    @Test
    void shouldMaskTaskAddressAndSensitiveError() {
        var task = new NotificationTaskEntity();
        task.setRecipientAddress("encrypted-address");
        task.setLastError("code=123456 phone=13800138000 mail=user@example.com");

        var view = converter.toTaskVO(task);

        assertEquals("[已加密]", view.getRecipientAddress());
        assertFalse(view.getLastError().contains("123456"));
        assertFalse(view.getLastError().contains("13800138000"));
        assertFalse(view.getLastError().contains("user@example.com"));
    }

    @Test
    void shouldMaskDeliverySummaryWithoutDroppingStatus() {
        var delivery = new NotificationDeliveryEntity();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus("FAILED");
        delivery.setResponseSummary("token=abc123 user@example.com");

        var view = converter.toDeliveryVO(delivery);

        assertEquals("FAILED", view.getStatus());
        assertFalse(view.getResponseSummary().contains("abc123"));
        assertFalse(view.getResponseSummary().contains("user@example.com"));
    }
}
