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
import com.devops00.spectra.notification.javabean.from.NotificationBatchDeleteFrom;
import com.devops00.spectra.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.service.NotificationInboxService;
import com.devops00.spectra.notification.service.NotificationPreferenceService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * 消息中心和偏好 Self API 的认证边界测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/13
 */
class NotificationSelfControllerSecurityTest {

    @Test
    void shouldRequireNotificationPermissionsForSelfOperations() throws NoSuchMethodException {
        var controller = new NotificationController(mock(NotificationInboxService.class), mock(SecurityContextAccessor.class));
        var query = NotificationController.class.getMethod("list", PageFrom.class, NotificationQueryFrom.class);
        var detail = NotificationController.class.getMethod("detail", UUID.class);
        var unread = NotificationController.class.getMethod("unreadCount");
        var markRead = NotificationController.class.getMethod("markAsRead", UUID.class);
        var markAll = NotificationController.class.getMethod("markAllAsRead");
        var delete = NotificationController.class.getMethod("deleteById", UUID.class);
        var batchDelete = NotificationController.class.getMethod("batchDelete", NotificationBatchDeleteFrom.class);

        assertEquals("hasPermission(null ,'notification:read')", query.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:read')", detail.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:read')", unread.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:update')", markRead.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:update')", markAll.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:delete')", delete.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasPermission(null ,'notification:delete')", batchDelete.getAnnotation(PreAuthorize.class).value());
        assertNotNull(controller);
    }

    @Test
    void shouldExposeOnlyAuthenticatedPreferenceOperations() throws NoSuchMethodException {
        var controller = new NotificationPreferenceController(mock(NotificationPreferenceService.class), mock(SecurityContextAccessor.class));
        var list = NotificationPreferenceController.class.getMethod("list");
        var save = NotificationPreferenceController.class.getMethod("save", String.class, String.class,
                boolean.class, boolean.class);

        assertEquals("isAuthenticated()", list.getAnnotation(PreAuthorize.class).value());
        assertEquals("isAuthenticated()", save.getAnnotation(PreAuthorize.class).value());
        assertNotNull(controller);
    }
}
