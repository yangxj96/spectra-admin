/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 服务监控管理接口的权限与版本契约测试。 */
class ServiceMonitorControllerPermissionTest {

    @Test
    void overviewKeepsVersionAndRequiresMonitorReadPermission() throws NoSuchMethodException {
        var method = ServiceMonitorController.class.getMethod("getOverview");
        var mapping = method.getAnnotation(GetMapping.class);
        var authorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("1.0.0", mapping.version());
        assertEquals("hasPermission(null, 'system:monitor:read')", authorize.value());
    }
}
