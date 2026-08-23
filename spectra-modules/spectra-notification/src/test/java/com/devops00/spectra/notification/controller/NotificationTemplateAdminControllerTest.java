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
import com.devops00.spectra.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.notification.service.NotificationTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * 通知模板接口权限和路径参数边界测试。
 */
class NotificationTemplateAdminControllerTest {

    @Test
    void shouldExposeFinalTemplateLifecyclePermissions() throws NoSuchMethodException {
        var controller = new NotificationTemplateAdminController(mock(NotificationTemplateService.class));
        assertNotNull(controller);
        assertEquals("hasPermission(null, 'notification:template:read')", annotation("page",
                PageFrom.class, NotificationTemplatePageFrom.class));
        assertEquals("hasPermission(null, 'notification:template:write')", annotation("create",
                NotificationTemplateSaveFrom.class));
        assertEquals("hasPermission(null, 'notification:template:write')", annotation("copy", UUID.class));
        assertEquals("hasPermission(null, 'notification:template:publish')", annotation("publish",
                UUID.class, NotificationTemplateActionFrom.class));
        assertEquals("hasPermission(null, 'notification:template:read')", annotation("preview",
                NotificationTemplatePreviewFrom.class));
    }

    private String annotation(String method, Class<?>... parameterTypes) throws NoSuchMethodException {
        return NotificationTemplateAdminController.class.getMethod(method, parameterTypes)
                .getAnnotation(PreAuthorize.class)
                .value();
    }
}
