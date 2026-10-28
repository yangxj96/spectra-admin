/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.DevOpsRecipientQuery;
import com.devops00.spectra.core.user.service.UserDirectoryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DEV_OPS 告警收件人查询实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Component
@RequiredArgsConstructor
public class DevOpsRecipientQueryImpl implements DevOpsRecipientQuery {

    private final SecurityRoleMapper securityRoleMapper;

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final UserDirectoryReadService userDirectoryReadService;

    @Override
    public List<UUID> findActiveUserIds() {
        var role = securityRoleMapper.selectOne(new LambdaQueryWrapper<SecurityRole>()
                .eq(SecurityRole::getCode, RootAuthorizationPolicy.ROOT_ROLE)
                .eq(SecurityRole::getState, SecurityAuthorizationState.ACTIVE.name())
                .isNull(SecurityRole::getDeleted));
        if (role == null) {
            return List.of();
        }
        var now = Instant.now();
        var userIds = roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getRoleId, role.getId())
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name())
                .isNull(RoleAssignment::getDeleted)
                .and(wrapper -> wrapper.isNull(RoleAssignment::getValidFrom)
                        .or()
                        .le(RoleAssignment::getValidFrom, now))
                .and(wrapper -> wrapper.isNull(RoleAssignment::getValidUntil)
                        .or()
                        .gt(RoleAssignment::getValidUntil, now)))
                .stream()
                .map(RoleAssignment::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userDirectoryReadService.findByIds(userIds)
                .stream()
                .filter(user -> "ACTIVE".equals(user.status()))
                .map(UserDirectoryReadService.UserDirectoryRow::id)
                .toList();
    }
}
