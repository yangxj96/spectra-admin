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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.notification.NotificationAudience;
import com.devops00.spectra.common.notification.NotificationAudienceDirectory;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Core 用户、部门和角色到通知受众的展开适配器。
 *
 * <p>这里只展开候选用户 ID，不返回用户资料或地址；数据范围和渠道地址由通知收件人目录继续校验。</p>
 */
@Service
@RequiredArgsConstructor
public class CoreNotificationAudienceDirectory implements NotificationAudienceDirectory {

    private final UserService userService;

    private final DepartmentService departmentService;

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final SecurityRoleMapper securityRoleMapper;

    @Override
    public List<UUID> resolve(NotificationAudience audience) {
        if (audience == null) {
            return List.of();
        }
        var userIds = new LinkedHashSet<UUID>();
        addNonNull(userIds, audience.userIds());
        addDepartmentUsers(userIds, audience.departmentIds());
        addRoleUsers(userIds, audience.roleIds());
        return userIds.stream().toList();
    }

    private void addDepartmentUsers(Set<UUID> userIds, List<UUID> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return;
        }
        var expandedDepartments = new LinkedHashSet<UUID>();
        for (var departmentId : departmentIds) {
            if (departmentId == null) {
                continue;
            }
            Collection<UUID> descendants = departmentService.getSelfAndDescendantIds(departmentId);
            if (descendants != null) {
                addNonNull(expandedDepartments, descendants);
            }
        }
        if (expandedDepartments.isEmpty()) {
            return;
        }
        userService.list(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .eq(User::getStatus, UserStatus.ACTIVE)
                .in(User::getDepartmentId, expandedDepartments))
                .stream()
                .map(User::getId)
                .forEach(userIds::add);
    }

    private void addRoleUsers(Set<UUID> userIds, List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        var requestedRoleIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        if (requestedRoleIds.isEmpty()) {
            return;
        }
        var activeRoleIds = securityRoleMapper.selectList(new LambdaQueryWrapper<SecurityRole>()
                .select(SecurityRole::getId)
                .in(SecurityRole::getId, requestedRoleIds)
                .eq(SecurityRole::getState, "ACTIVE"))
                .stream()
                .map(SecurityRole::getId)
                .toList();
        if (activeRoleIds.isEmpty()) {
            return;
        }
        var now = Instant.now();
        roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .select(RoleAssignment::getUserId, RoleAssignment::getValidFrom, RoleAssignment::getValidUntil)
                .in(RoleAssignment::getRoleId, activeRoleIds)
                .eq(RoleAssignment::getState, "ACTIVE"))
                .stream()
                .filter(assignment -> isValid(assignment, now))
                .map(RoleAssignment::getUserId)
                .filter(Objects::nonNull)
                .forEach(userIds::add);
    }

    private boolean isValid(RoleAssignment assignment, Instant now) {
        return (assignment.getValidFrom() == null || !now.isBefore(assignment.getValidFrom()))
                && (assignment.getValidUntil() == null || now.isBefore(assignment.getValidUntil()));
    }

    private void addNonNull(Set<UUID> target, Collection<UUID> values) {
        if (values != null) {
            values.stream().filter(Objects::nonNull).forEach(target::add);
        }
    }
}
