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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.core.authorization.entity.Permission;
import com.devops00.spectra.core.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.authorization.entity.RolePermission;
import com.devops00.spectra.core.authorization.LegacyAuthorizationWriteGuard;
import com.devops00.spectra.core.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityVO;
import com.devops00.spectra.core.user.service.RelRoleAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 关联服务-用户和权限
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Service
public class RelRoleAuthorityServiceImpl implements RelRoleAuthorityService {

    private final PermissionMapper permissionMapper;

    private final RolePermissionMapper rolePermissionMapper;

    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    public RelRoleAuthorityServiceImpl(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper,
                                       RoleGrantablePermissionMapper roleGrantablePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleGrantablePermissionMapper = roleGrantablePermissionMapper;
    }

    @Override
    @Transactional
    public void grant(UUID roleId, RoleAuthorityFrom from) {
        LegacyAuthorizationWriteGuard.reject("旧角色权限关联写入口");
    }

    @Override
    @Transactional
    public void revoke(UUID roleId) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        roleGrantablePermissionMapper.delete(new LambdaQueryWrapper<RoleGrantablePermission>()
                .eq(RoleGrantablePermission::getRoleId, roleId));
    }

    @Override
    public List<AuthorityVO> get(UUID roleId) {
        var permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)).stream()
                .map(RolePermission::getPermissionId).toList();
        return toVO(permissionIds);
    }

    @Override
    public List<AuthorityVO> get(List<UUID> ids) {
        if (CollUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        var permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleId, ids)).stream()
                .map(RolePermission::getPermissionId).distinct().toList();
        return toVO(permissionIds);
    }

    private List<AuthorityVO> toVO(List<UUID> permissionIds) {
        if (CollUtils.isEmpty(permissionIds)) {
            return new ArrayList<>();
        }
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .in(Permission::getId, permissionIds)
                        .eq(Permission::getState, "ACTIVE")
                        .orderByAsc(Permission::getCode)).stream()
                .map(permission -> new AuthorityVO(permission.getId(), null, permission.getName(), permission.getCode()))
                .toList();
    }
}
