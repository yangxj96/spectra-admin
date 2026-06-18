/*
 *  Copyright 2018-2025 yangxj96
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
import com.devops00.spectra.core.user.javabean.entity.RelUserRole;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.mapper.RelUserRoleMapper;
import com.devops00.spectra.core.user.mapper.RoleMapper;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// 关联服务-用户和角色
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Service
public class RelUserRoleServiceImpl implements RelUserRoleService {

    @Resource
    private RelUserRoleMapper relUserRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    @Transactional
    public void grant(UUID userId, List<UUID> roleIds) {
        var coll = new ArrayList<RelUserRole>();
        for (UUID roleId : roleIds) {
            coll.add(RelUserRole.builder().userId(userId).roleId(roleId).build());
        }
        relUserRoleMapper.insert(coll);
    }

    @Override
    @Transactional
    public void revoke(UUID userId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, userId);
        relUserRoleMapper.delete(wrapper);
    }

    @Override
    public void revoke(UUID userId, List<UUID> roleIds) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, userId)
                .in(RelUserRole::getRoleId, roleIds);
        relUserRoleMapper.delete(wrapper);
    }

    @Override
    public List<RelUserRole> getRelByRoleId(UUID roleId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getRoleId, roleId);
        return relUserRoleMapper.selectList(wrapper);
    }

    @Override
    public List<Role> getRoles(UUID userId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>();
        wrapper.eq(RelUserRole::getUserId, userId);
        List<RelUserRole> userRoles = relUserRoleMapper.selectList(wrapper);
        return roleMapper.selectByIds(userRoles.stream().map(RelUserRole::getRoleId).toList());
    }


}
