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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.entity.RoleDataScope;
import com.devops00.spectra.core.user.javabean.entity.RoleDataScopeTarget;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.entity.UserDataScope;
import com.devops00.spectra.core.user.javabean.entity.UserDataScopeTarget;
import com.devops00.spectra.core.user.mapper.RoleDataScopeMapper;
import com.devops00.spectra.core.user.mapper.RoleDataScopeTargetMapper;
import com.devops00.spectra.core.user.mapper.UserDataScopeMapper;
import com.devops00.spectra.core.user.mapper.UserDataScopeTargetMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.RelUserRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// 数据范围解析器 — 计算用户的有效数据范围
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class DataScopeResolver implements DataScopeProvider {

    private final UserMapper userMapper;
    private final UserDataScopeMapper userDataScopeMapper;
    private final UserDataScopeTargetMapper userDataScopeTargetMapper;
    private final RoleDataScopeMapper roleDataScopeMapper;
    private final RoleDataScopeTargetMapper roleDataScopeTargetMapper;
    private final RelUserRoleService relUserRoleService;
    private final DepartmentService departmentService;

    @Override
    public EffectiveScope resolve(UUID userId) {
        if (userId == null) {
            return new EffectiveScope(DataScopeType.SELF, null, Collections.emptyList());
        }

        // 1. 获取用户实体信息
        User user = userMapper.selectById(userId);
        UUID departmentId = user != null ? user.getDepartmentId() : null;

        // 2. 查询用户自定义数据范围
        UserDataScope userDataScope = userDataScopeMapper.findByUserId(userId);
        if (userDataScope != null && userDataScope.getScopeType() != null) {
            // 用户定义了范围 → 直接使用（用户覆盖角色）
            return buildScope(userDataScope.getScopeType(), departmentId, userId);
        }

        // 3. 用户未定义范围 → 查询所有角色，取最大范围
        var roles = relUserRoleService.getRoles(userId);
        if (roles == null || roles.isEmpty()) {
            return new EffectiveScope(DataScopeType.SELF, departmentId, Collections.emptyList());
        }

        // 4. 收集所有角色的数据范围
        DataScopeType effectiveType = DataScopeType.SELF;
        List<UUID> allTargetIds = new ArrayList<>();

        for (var role : roles) {
            RoleDataScope roleScope = roleDataScopeMapper.findByRoleId(role.getId());
            // 兼容旧版 sys_role.scope：迁移期间规范表为空时仍能正确继承角色范围。
            DataScopeType roleType = roleScope != null && roleScope.getScopeType() != null ? roleScope.getScopeType() : role.getScope();
            if (roleType == null) {
                continue;
            }

            // 规则2：取最大范围
            if (comparePriority(roleType, effectiveType) > 0) {
                effectiveType = roleType;
            }

            // 收集 CUSTOM 的目标部门
            if (roleType == DataScopeType.CUSTOM) {
                var targets = roleDataScopeTargetMapper.selectList(new LambdaQueryWrapper<RoleDataScopeTarget>()
                        .eq(RoleDataScopeTarget::getRoleId, role.getId())
                        .isNull(RoleDataScopeTarget::getDeleted));
                if (targets != null) {
                    targets.stream().map(RoleDataScopeTarget::getTargetId).filter(Objects::nonNull).forEach(allTargetIds::add);
                }
            }
        }

        return buildScope(effectiveType, departmentId, allTargetIds);
    }

    /// 根据范围类型构建完整 EffectiveScope
    private EffectiveScope buildScope(DataScopeType scopeType, UUID departmentId, UUID userId) {
        List<UUID> targetIds = new ArrayList<>();

        if (scopeType == DataScopeType.DEPT) {
            if (departmentId != null) {
                targetIds.add(departmentId);
            }
        } else if (scopeType == DataScopeType.DEPT_AND_CHILDREN && departmentId != null) {
            var ids = departmentService.getSelfAndDescendantIds(departmentId);
            targetIds = ids == null ? new ArrayList<>() : new ArrayList<>(ids);
        } else if (scopeType == DataScopeType.CUSTOM) {
            // 从 UserDataScopeTarget 查询
            var targets = userDataScopeTargetMapper.findByUserId(userId);
            if (targets != null) {
                targetIds = targets.stream().map(UserDataScopeTarget::getTargetId).filter(Objects::nonNull).toList();
            }
        }

        return new EffectiveScope(scopeType, departmentId, targetIds);
    }

    /// 根据范围类型构建完整 EffectiveScope（角色合并场景）
    private EffectiveScope buildScope(DataScopeType scopeType, UUID departmentId, List<UUID> targetIds) {
        if (scopeType == DataScopeType.DEPT && departmentId != null) {
            targetIds = List.of(departmentId);
        } else if (scopeType == DataScopeType.DEPT_AND_CHILDREN && departmentId != null) {
            var children = departmentService.getSelfAndDescendantIds(departmentId);
            targetIds = children == null ? new ArrayList<>() : new ArrayList<>(children);
        }

        return new EffectiveScope(scopeType, departmentId, targetIds);
    }

    /// 比较两个范围类型的优先级（数字越小范围越大）
    ///
    /// GLOBAL(0) > DEPT_AND_CHILDREN(3) > DEPT(2) > CUSTOM(4) > SELF(1)
    private int comparePriority(DataScopeType a, DataScopeType b) {
        return Integer.compare(priority(a), priority(b));
    }

    private int priority(DataScopeType type) {
        return switch (type) {
            case GLOBAL -> 100;
            case DEPT_AND_CHILDREN -> 80;
            case DEPT -> 60;
            case CUSTOM -> 40;
            case SELF -> 20;
        };
    }
}
