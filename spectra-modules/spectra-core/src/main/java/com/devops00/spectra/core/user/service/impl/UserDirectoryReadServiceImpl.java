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
package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.UserDirectoryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户目录只读实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Service
@RequiredArgsConstructor
public class UserDirectoryReadServiceImpl implements UserDirectoryReadService {

    private final UserMapper userMapper;

    private final UserDepartmentMembershipMapper membershipMapper;

    @Override
    public List<UserDirectoryRow> listAll() {
        return toRows(userMapper.selectList(null));
    }

    @Override
    public List<UserDirectoryRow> findByIds(Collection<UUID> userIds) {
        var distinctIds = distinctIds(userIds);
        if (distinctIds.isEmpty()) {
            return List.of();
        }
        return toRows(userMapper.selectByIds(distinctIds));
    }

    @Override
    public List<UserDirectoryRow> searchSecurityCandidates(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        var normalized = keyword.trim();
        UUID userId = parseUuid(normalized);
        int boundedLimit = Math.min(Math.max(limit, 1), 20);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .isNull(User::getDeleted)
                .and(condition -> {
                    condition.like(User::getUsername, normalized)
                            .or()
                            .like(User::getRealName, normalized)
                            .or()
                            .like(User::getEmployeeNo, normalized);
                    if (userId != null) {
                        condition.or().eq(User::getId, userId);
                    }
                })
                .orderByAsc(User::getRealName)
                .orderByAsc(User::getUsername)
                .last("LIMIT " + boundedLimit);
        return toRows(userMapper.selectList(wrapper));
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public List<UUID> findUserIdsByDepartmentId(UUID departmentId) {
        if (departmentId == null) {
            return List.of();
        }
        return membershipMapper.selectUserIdsByDepartmentIds(List.of(departmentId), null);
    }

    @Override
    public Map<UUID, Set<UUID>> findDepartmentIdsByUserIds(Collection<UUID> userIds) {
        var distinctIds = distinctIds(userIds);
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        var result = new java.util.HashMap<UUID, Set<UUID>>();
        toRows(userMapper.selectByIds(distinctIds)).forEach(user -> {
            if (user.id() != null && user.primaryDepartmentId() != null) {
                result.computeIfAbsent(user.id(), ignored -> new java.util.LinkedHashSet<>())
                        .add(user.primaryDepartmentId());
            }
        });
        membershipMapper.selectActiveByUserIds(distinctIds)
                .stream()
                .filter(membership -> membership.getUserId() != null && membership.getDepartmentId() != null)
                .forEach(membership -> result.computeIfAbsent(membership.getUserId(),
                        ignored -> new java.util.LinkedHashSet<>()).add(membership.getDepartmentId()));
        return result.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
    }

    private static List<UserDirectoryRow> toRows(List<User> users) {
        return users == null
                ? List.of()
                : users.stream()
                        .filter(user -> user != null)
                        .map(user -> new UserDirectoryRow(
                                user.getId(),
                                user.getEmployeeNo(),
                                user.getRealName(),
                                user.getUsername(),
                                user.getAvatar(),
                                user.getStatus() == null ? null : user.getStatus().name(),
                                user.getPrimaryDepartmentId()))
                        .toList();
    }

    private static List<UUID> distinctIds(Collection<UUID> ids) {
        return ids == null
                ? List.of()
                : ids.stream().filter(id -> id != null).distinct().toList();
    }
}
