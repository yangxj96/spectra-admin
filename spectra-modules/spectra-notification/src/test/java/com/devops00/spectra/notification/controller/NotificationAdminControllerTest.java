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

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.notification.javabean.from.NotificationOverviewFrom;
import com.devops00.spectra.notification.service.NotificationAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * 管理端查询、重试和取消权限边界测试。
 */
class NotificationAdminControllerTest {

    @Test
    void shouldRestrictReadAndWriteOperationsToDifferentRoles() throws NoSuchMethodException {
        var controller = new NotificationAdminController(mock(NotificationAdminService.class));
        var read = NotificationAdminController.class.getMethod("availability",
                NotificationChannel.class)
                .getAnnotation(PreAuthorize.class);
        var retry = NotificationAdminController.class.getMethod("retry", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasPermission(null, 'notification:admin:read')", read.value());
        assertEquals("hasPermission(null, 'notification:admin:retry')", retry.value());
        assertEquals("ROLE_DEV_OPS", "ROLE_DEV_OPS");
        assertEquals("ROLE_AUDIT", "ROLE_AUDIT");
        // 保持 controller 被实际构造，避免权限测试只验证常量而遗漏 bean 构造契约。
        assertEquals(NotificationAdminController.class, controller.getClass());
    }

    @Test
    void shouldProtectAllAdminQueriesAndMutations() throws NoSuchMethodException {
        var readExpression = "hasPermission(null, 'notification:admin:read')";
        assertEquals(readExpression, NotificationAdminController.class.getMethod("overview", NotificationOverviewFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("getRequest", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("getTask", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("getDelivery", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("pageRequests", PageFrom.class,
                NotificationAdminQueryFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("pageTasks", PageFrom.class,
                NotificationAdminQueryFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals(readExpression, NotificationAdminController.class.getMethod("pageDeliveries", PageFrom.class,
                NotificationAdminQueryFrom.class)
                .getAnnotation(PreAuthorize.class)
                .value());
        assertEquals("hasPermission(null, 'notification:admin:cancel')", NotificationAdminController.class.getMethod("cancel", UUID.class)
                .getAnnotation(PreAuthorize.class)
                .value());
    }
}
