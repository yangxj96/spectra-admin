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

package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.security.authorization.service.RoleAuthorizationChangeService;
import com.devops00.spectra.core.user.javabean.from.RoleEditorSaveFrom;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import com.devops00.spectra.core.user.service.RoleEditorService;
import com.devops00.spectra.core.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色编辑器提交服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Service
@RequiredArgsConstructor
public class RoleEditorServiceImpl implements RoleEditorService {

    private final RoleService roleService;

    private final RoleAuthorizationChangeService roleAuthorizationChangeService;

    private final RelRoleMenuService relRoleMenuService;

    @Override
    @Transactional
    public RoleVO save(RoleEditorSaveFrom params) {
        var role = roleService.save(new RoleFrom(params.getId(), params.getName(), params.getCode(), params.getRemark()));
        if (role.getId() == null) {
            throw new DataException("保存角色失败，未生成角色标识");
        }

        var authorization = new RoleAuthorizationChangeFrom();
        authorization.setExpectedVersion(params.getExpectedVersion() == null
                ? role.getVersion()
                : params.getExpectedVersion());
        authorization.setAuthorityLevel(params.getAuthorityLevel());
        authorization.setPermissionCodes(params.getPermissionCodes());
        authorization.setGrantablePermissionCodes(params.getGrantablePermissionCodes());

        var preview = roleAuthorizationChangeService.preview(role.getId(), authorization);
        var apply = new RoleAuthorizationApplyFrom();
        apply.setExpectedVersion(preview.getExpectedVersion());
        apply.setAuthorityLevel(authorization.getAuthorityLevel());
        apply.setPermissionCodes(authorization.getPermissionCodes());
        apply.setGrantablePermissionCodes(authorization.getGrantablePermissionCodes());
        apply.setPreviewToken(preview.getPreviewToken());
        roleAuthorizationChangeService.apply(role.getId(), apply);

        relRoleMenuService.grant(role.getId(), new RoleMenuFrom(role.getId(), params.getMenuIds()));
        return roleService.detail(role.getId());
    }
}
