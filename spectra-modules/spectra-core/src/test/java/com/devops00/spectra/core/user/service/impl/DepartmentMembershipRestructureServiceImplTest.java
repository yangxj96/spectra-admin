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
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.entity.UserDepartmentMembership;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证部门重组时主部门和关联部门关系的迁移及去重。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
class DepartmentMembershipRestructureServiceImplTest {

    private static final UUID SOURCE_A = UUID.randomUUID();
    private static final UUID SOURCE_B = UUID.randomUUID();
    private static final UUID TARGET = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();
    private static final UUID PRIMARY_USER = UUID.randomUUID();
    private static final UUID MULTI_ASSOC_USER = UUID.randomUUID();
    private static final UUID DISABLED_USER = UUID.randomUUID();
    private static final UUID OPERATOR = UUID.randomUUID();
    private static final List<UUID> SOURCES = List.of(SOURCE_A, SOURCE_B);

    private UserMapper userMapper;
    private UserDepartmentMembershipMapper membershipMapper;
    private DepartmentMembershipRestructureServiceImpl service;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        membershipMapper = mock(UserDepartmentMembershipMapper.class);
        service = new DepartmentMembershipRestructureServiceImpl(userMapper, membershipMapper);
    }

    @Test
    void previewMergeCountsPrimaryAssociationsAndDeduplicationIncludingDisabledUsers() {
        var users = mergeUsers();
        stubMergeSnapshot(users, mergeMemberships());

        var impact = service.previewMerge(SOURCES);

        assertEquals(3, impact.affectedUserCount());
        assertEquals(1, impact.primaryDepartmentCount());
        assertEquals(4, impact.associatedDepartmentCount());
        assertEquals(2, impact.deduplicatedAssociatedCount());
        assertEquals(Set.of(PRIMARY_USER, MULTI_ASSOC_USER, DISABLED_USER), impact.affectedUserIds());
    }

    @Test
    void applyMergeMovesOneAssociationPerUserAndKeepsOtherRelationsUntouched() {
        var users = mergeUsers();
        var memberships = mergeMemberships();
        stubMergeSnapshot(users, memberships);
        when(userMapper.reassignPrimaryDepartmentsByIds(SOURCES, TARGET, OPERATOR)).thenReturn(1);
        when(membershipMapper.softDeleteActiveByUsersAndDepartments(
                List.of(PRIMARY_USER, MULTI_ASSOC_USER, DISABLED_USER), SOURCES, OPERATOR)).thenReturn(4);
        when(membershipMapper.batchInsertAssociations(anyList(), eq(OPERATOR))).thenAnswer(invocation -> {
            List<UserDepartmentMembership> rows = invocation.getArgument(0);
            assertEquals(2, rows.size());
            assertEquals(Set.of(MULTI_ASSOC_USER, DISABLED_USER),
                    rows.stream().map(UserDepartmentMembership::getUserId).collect(java.util.stream.Collectors.toSet()));
            assertEquals(Set.of(TARGET),
                    rows.stream().map(UserDepartmentMembership::getDepartmentId).collect(java.util.stream.Collectors.toSet()));
            return rows.size();
        });

        var affected = service.applyMerge(SOURCES, TARGET, OPERATOR);

        assertEquals(Set.of(PRIMARY_USER, MULTI_ASSOC_USER, DISABLED_USER), affected);
        verify(userMapper).reassignPrimaryDepartmentsByIds(SOURCES, TARGET, OPERATOR);
        verify(membershipMapper).softDeleteActiveByUsersAndDepartments(
                List.of(PRIMARY_USER, MULTI_ASSOC_USER, DISABLED_USER), SOURCES, OPERATOR);
    }

    @Test
    void applySplitPreservesPrimaryAndAssociatedRelationshipKinds() {
        var selectedIds = List.of(PRIMARY_USER, MULTI_ASSOC_USER);
        var users = List.of(user(PRIMARY_USER, SOURCE_A), user(MULTI_ASSOC_USER, OTHER));
        var memberships = List.of(membership(MULTI_ASSOC_USER, SOURCE_A), membership(MULTI_ASSOC_USER, OTHER));
        when(membershipMapper.selectUserIdsByDepartmentIds(List.of(SOURCE_A), null))
                .thenReturn(selectedIds);
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(users);
        when(membershipMapper.selectActiveByUserIds(selectedIds)).thenReturn(memberships);
        when(userMapper.reassignPrimaryDepartmentForUsers(selectedIds, SOURCE_A, TARGET, OPERATOR)).thenReturn(1);
        when(membershipMapper.softDeleteActiveByUsersAndDepartments(
                selectedIds, List.of(SOURCE_A), OPERATOR)).thenReturn(1);
        when(membershipMapper.batchInsertAssociations(anyList(), eq(OPERATOR))).thenAnswer(invocation -> {
            List<UserDepartmentMembership> rows = invocation.getArgument(0);
            assertEquals(1, rows.size());
            assertEquals(MULTI_ASSOC_USER, rows.getFirst().getUserId());
            assertEquals(TARGET, rows.getFirst().getDepartmentId());
            return rows.size();
        });

        var affected = service.applySplit(SOURCE_A, selectedIds, TARGET, OPERATOR);

        assertEquals(Set.copyOf(selectedIds), affected);
        verify(userMapper).reassignPrimaryDepartmentForUsers(selectedIds, SOURCE_A, TARGET, OPERATOR);
        verify(membershipMapper).softDeleteActiveByUsersAndDepartments(selectedIds, List.of(SOURCE_A), OPERATOR);
    }

    @Test
    void applySplitRejectsUserWhoNoLongerBelongsDirectlyToSourceDepartment() {
        var outsiderId = UUID.randomUUID();
        when(membershipMapper.selectUserIdsByDepartmentIds(List.of(SOURCE_A), null)).thenReturn(List.of());

        assertThrows(DataException.class,
                () -> service.applySplit(SOURCE_A, List.of(outsiderId), TARGET, OPERATOR));

        verify(userMapper, never()).reassignPrimaryDepartmentForUsers(anyList(), eq(SOURCE_A), eq(TARGET), eq(OPERATOR));
        verify(membershipMapper, never()).softDeleteActiveByUsersAndDepartments(anyList(), anyList(), eq(OPERATOR));
    }

    private void stubMergeSnapshot(List<User> users, List<UserDepartmentMembership> memberships) {
        when(membershipMapper.selectUserIdsByDepartmentIds(SOURCES, null))
                .thenReturn(List.of(PRIMARY_USER, MULTI_ASSOC_USER, DISABLED_USER));
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(users);
        when(membershipMapper.selectActiveByUserIds(anyList())).thenReturn(memberships);
    }

    private static List<User> mergeUsers() {
        return List.of(user(PRIMARY_USER, SOURCE_A), user(MULTI_ASSOC_USER, OTHER), user(DISABLED_USER, OTHER));
    }

    private static User user(UUID id, UUID primaryDepartmentId) {
        var user = new User();
        user.setId(id);
        user.setPrimaryDepartmentId(primaryDepartmentId);
        return user;
    }

    private static List<UserDepartmentMembership> mergeMemberships() {
        return List.of(
                membership(PRIMARY_USER, SOURCE_B),
                membership(MULTI_ASSOC_USER, SOURCE_A),
                membership(MULTI_ASSOC_USER, SOURCE_B),
                membership(DISABLED_USER, SOURCE_B));
    }

    private static UserDepartmentMembership membership(UUID userId, UUID departmentId) {
        var membership = new UserDepartmentMembership();
        membership.setUserId(userId);
        membership.setDepartmentId(departmentId);
        return membership;
    }
}
