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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.notification.javabean.from.NotificationProviderTestFrom;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import com.devops00.spectra.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.notification.service.NotificationProviderTestService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Provider 配置接口权限边界测试。
 */
class NotificationProviderAdminControllerTest {

    @Test
    void shouldSeparateProviderReadAndConfigurePermissions() throws NoSuchMethodException {
        var controller = new NotificationProviderAdminController(mock(NotificationProviderAdminService.class),
                mock(NotificationProviderRuntime.class), mock(NotificationProviderTestService.class));

        assertEquals("hasPermission(null, 'notification:provider:read')",
                NotificationProviderAdminController.class.getMethod("list")
                        .getAnnotation(PreAuthorize.class)
                        .value());
        assertEquals("hasPermission(null, 'notification:provider:configure')",
                NotificationProviderAdminController.class.getMethod("health", NotificationChannel.class)
                        .getAnnotation(PreAuthorize.class)
                        .value());
        assertEquals("hasPermission(null, 'notification:provider:configure')",
                NotificationProviderAdminController.class.getMethod("modify", NotificationChannel.class,
                        NotificationProviderSaveFrom.class)
                        .getAnnotation(PreAuthorize.class)
                        .value());
        assertEquals("hasPermission(null, 'notification:provider:configure')",
                NotificationProviderAdminController.class.getMethod("test", NotificationChannel.class,
                        NotificationProviderTestFrom.class)
                        .getAnnotation(PreAuthorize.class)
                        .value());
        assertEquals(NotificationProviderAdminController.class, controller.getClass());
    }
}
