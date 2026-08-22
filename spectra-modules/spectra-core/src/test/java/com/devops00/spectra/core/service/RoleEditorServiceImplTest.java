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

package com.devops00.spectra.core.service;

import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.service.RoleAuthorizationChangeService;
import com.devops00.spectra.core.user.javabean.from.RoleEditorSaveFrom;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import com.devops00.spectra.core.user.service.RoleService;
import com.devops00.spectra.core.user.service.impl.RoleEditorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色编辑器提交服务测试。
 */
@ExtendWith(MockitoExtension.class)
class RoleEditorServiceImplTest {

    @Mock
    private RoleService roleService;

    @Mock
    private RoleAuthorizationChangeService roleAuthorizationChangeService;

    @Mock
    private RelRoleMenuService relRoleMenuService;

    private RoleEditorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoleEditorServiceImpl(roleService, roleAuthorizationChangeService, relRoleMenuService);
    }

    @Test
    void saveShouldCommitRoleAuthorizationAndMenusAsOneEditorCommand() {
        var roleId = UUID.randomUUID();
        var role = new RoleVO();
        role.setId(roleId);
        role.setVersion(3L);
        var preview = new RoleAuthorizationChangePreviewVO();
        preview.setExpectedVersion(3L);
        preview.setPreviewToken("preview-token");
        var request = new RoleEditorSaveFrom();
        request.setName("业务管理员");
        request.setCode("ROLE_BUSINESS_ADMIN");
        request.setRemark("业务角色");
        request.setExpectedVersion(3L);
        request.setAuthorityLevel(2);
        request.setPermissionCodes(Set.of("role:read"));
        request.setGrantablePermissionCodes(Set.of());
        request.setMenuIds(List.of());

        when(roleService.save(any())).thenReturn(role);
        when(roleAuthorizationChangeService.preview(eq(roleId), any())).thenReturn(preview);
        when(roleService.detail(roleId)).thenReturn(role);

        var result = service.save(request);

        assertEquals(roleId, result.getId());
        verify(roleAuthorizationChangeService).apply(eq(roleId), any());
        ArgumentCaptor<RoleMenuFrom> menuCaptor = ArgumentCaptor.forClass(RoleMenuFrom.class);
        verify(relRoleMenuService).grant(eq(roleId), menuCaptor.capture());
        assertEquals(List.of(), menuCaptor.getValue().getMenuIds());
    }
}
