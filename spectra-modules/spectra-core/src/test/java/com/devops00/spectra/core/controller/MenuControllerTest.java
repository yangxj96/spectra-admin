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

package com.devops00.spectra.core.controller;

import com.devops00.spectra.core.system.controller.MenuController;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/// 菜单控制器测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/30
class MenuControllerTest {

    @Test
    void currentShouldUseAuthenticatedUserId() {
        var service = mock(MenuService.class);
        var controller = new MenuController(service);
        var user = new SecurityUser();
        var userId = UUID.randomUUID();
        user.setId(userId);

        controller.current(user);

        verify(service).current(userId);
    }

    @Test
    void treeShouldRequireMenuPermissionAfterMigration() throws NoSuchMethodException {
        var method = MenuController.class.getMethod("tree");

        assertEquals("hasPermission(null ,'MENU:*')", method.getAnnotation(PreAuthorize.class).value());
    }
}
