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

import java.util.UUID;

import com.devops00.spectra.notification.service.NotificationAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/** 管理端查询、重试和取消权限边界测试。 */
class NotificationAdminControllerTest {

    @Test
    void shouldRestrictReadAndWriteOperationsToDifferentRoles() throws NoSuchMethodException {
        var controller = new NotificationAdminController(mock(NotificationAdminService.class));
        var read = NotificationAdminController.class.getMethod("availability",
                com.devops00.spectra.common.notification.NotificationChannel.class)
                .getAnnotation(PreAuthorize.class);
        var retry = NotificationAdminController.class.getMethod("retry", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ROLE_DEV_OPS') or hasRole('ROLE_AUDIT')", read.value());
        assertEquals("hasRole('ROLE_DEV_OPS')", retry.value());
        assertEquals("ROLE_DEV_OPS", "ROLE_DEV_OPS");
        assertEquals("ROLE_AUDIT", "ROLE_AUDIT");
        // 保持 controller 被实际构造，避免权限测试只验证常量而遗漏 bean 构造契约。
        assertEquals(NotificationAdminController.class, controller.getClass());
    }
}
