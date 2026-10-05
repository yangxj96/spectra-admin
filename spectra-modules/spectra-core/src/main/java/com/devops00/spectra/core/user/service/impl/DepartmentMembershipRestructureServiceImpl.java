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
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureImpact;
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 部门重组的用户主部门及关联部门关系处理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Service
@RequiredArgsConstructor
public class DepartmentMembershipRestructureServiceImpl implements DepartmentMembershipRestructureService {

    private final UserMapper userMapper;
    private final UserDepartmentMembershipMapper membershipMapper;

    @Override
    public DepartmentMembershipRestructureImpact previewMerge(Collection<UUID> sourceDepartmentIds) {
        return toImpact(loadMergeSnapshot(normalizeDepartments(sourceDepartmentIds)));
    }

    @Override
    public Set<UUID> applyMerge(Collection<UUID> sourceDepartmentIds, UUID targetDepartmentId, UUID operatorId) {
        var departments = normalizeDepartments(sourceDepartmentIds);
        requireTargetAndOperator(targetDepartmentId, operatorId);
        var sourceDepartmentSet = Set.copyOf(departments);
        var snapshot = loadMergeSnapshot(departments);
        if (snapshot.users().isEmpty()) {
            return Set.of();
        }

        var impact = toImpact(snapshot);
        if (impact.primaryDepartmentCount() > 0) {
            int updated = userMapper.reassignPrimaryDepartmentsByIds(departments, targetDepartmentId, operatorId);
            requireAffectedRows(updated, impact.primaryDepartmentCount(), "替换主部门关系失败");
        }

        var sourceMemberships = sourceMemberships(snapshot, sourceDepartmentSet);
        if (sourceMemberships.isEmpty()) {
            return impact.affectedUserIds();
        }
        var membershipUserIds = sourceMemberships.stream()
                .map(UserDepartmentMembership::getUserId)
                .distinct()
                .toList();
        int deleted = membershipMapper.softDeleteActiveByUsersAndDepartments(
                membershipUserIds, departments, operatorId);
        requireAffectedRows(deleted, sourceMemberships.size(), "替换关联部门关系失败");

        var primaryDepartmentUserIds = primaryDepartmentUserIds(snapshot.users(), sourceDepartmentSet);
        var targetAssociations = targetAssociations(snapshot.users(), sourceMemberships,
                primaryDepartmentUserIds, targetDepartmentId);
        insertAssociations(targetAssociations, operatorId);
        return impact.affectedUserIds();
    }

    @Override
    public DepartmentMembershipRestructureImpact previewSplit(UUID sourceDepartmentId, Collection<UUID> userIds) {
        if (sourceDepartmentId == null) {
            throw new DataException("源部门不能为空");
        }
        var selectedIds = normalizeUsers(userIds);
        var directUserIds = new HashSet<>(membershipMapper.selectUserIdsByDepartmentIds(
                List.of(sourceDepartmentId), null));
        if (!directUserIds.containsAll(selectedIds)) {
            throw new DataException("拆分成员已不再直接属于源部门，请重新预览");
        }
        var snapshot = loadSplitSnapshot(sourceDepartmentId, selectedIds);
        validateSelectedUsers(selectedIds, snapshot.users());
        return toImpact(snapshot);
    }

    @Override
    public Set<UUID> applySplit(UUID sourceDepartmentId, Collection<UUID> userIds,
                                UUID targetDepartmentId, UUID operatorId) {
        if (sourceDepartmentId == null
                || targetDepartmentId == null
                || operatorId == null
                || sourceDepartmentId.equals(targetDepartmentId)) {
            throw new DataException("部门重组参数无效");
        }
        var selectedIds = normalizeUsers(userIds);
        var directUserIds = new HashSet<>(membershipMapper.selectUserIdsByDepartmentIds(
                List.of(sourceDepartmentId), null));
        if (!directUserIds.containsAll(selectedIds)) {
            throw new DataException("拆分成员已不再直接属于源部门，请重新预览");
        }
        var snapshot = loadSplitSnapshot(sourceDepartmentId, selectedIds);
        validateSelectedUsers(selectedIds, snapshot.users());
        var impact = toImpact(snapshot);

        if (impact.primaryDepartmentCount() > 0) {
            int updated = userMapper.reassignPrimaryDepartmentForUsers(
                    selectedIds, sourceDepartmentId, targetDepartmentId, operatorId);
            requireAffectedRows(updated, impact.primaryDepartmentCount(), "替换拆分成员主部门失败");
        }

        var sourceMemberships = sourceMemberships(snapshot, Set.of(sourceDepartmentId));
        if (!sourceMemberships.isEmpty()) {
            int deleted = membershipMapper.softDeleteActiveByUsersAndDepartments(
                    selectedIds, List.of(sourceDepartmentId), operatorId);
            requireAffectedRows(deleted, sourceMemberships.size(), "替换拆分成员关联部门失败");
        }

        var primaryDepartmentUserIds = primaryDepartmentUserIds(snapshot.users(), Set.of(sourceDepartmentId));
        var targetAssociations = targetAssociations(snapshot.users(), sourceMemberships,
                primaryDepartmentUserIds, targetDepartmentId);
        insertAssociations(targetAssociations, operatorId);
        return Set.copyOf(selectedIds);
    }

    private MembershipSnapshot loadMergeSnapshot(List<UUID> sourceDepartmentIds) {
        var userIds = membershipMapper.selectUserIdsByDepartmentIds(sourceDepartmentIds, null);
        return loadSnapshot(userIds, Set.copyOf(sourceDepartmentIds));
    }

    private MembershipSnapshot loadSplitSnapshot(UUID sourceDepartmentId, List<UUID> userIds) {
        if (sourceDepartmentId == null) {
            throw new DataException("源部门不能为空");
        }
        return loadSnapshot(userIds, Set.of(sourceDepartmentId));
    }

    private MembershipSnapshot loadSnapshot(Collection<UUID> requestedUserIds, Set<UUID> sourceDepartmentIds) {
        if (requestedUserIds == null || requestedUserIds.isEmpty()) {
            return new MembershipSnapshot(List.of(), List.of(), sourceDepartmentIds);
        }
        var distinctIds = List.copyOf(new LinkedHashSet<>(requestedUserIds));
        var users = userMapper.selectBatchIds(distinctIds);
        if (users == null || users.isEmpty()) {
            return new MembershipSnapshot(List.of(), List.of(), sourceDepartmentIds);
        }
        var activeUserIds = users.stream().map(User::getId).toList();
        var memberships = membershipMapper.selectActiveByUserIds(activeUserIds);
        return new MembershipSnapshot(List.copyOf(users),
                memberships == null ? List.of() : List.copyOf(memberships), sourceDepartmentIds);
    }

    private DepartmentMembershipRestructureImpact toImpact(MembershipSnapshot snapshot) {
        var sourceUsers = new HashSet<UUID>();
        var associatedByUser = new HashSet<UUID>();
        int associatedCount = 0;
        for (var membership : snapshot.memberships()) {
            if (snapshot.sourceDepartmentIds().contains(membership.getDepartmentId())) {
                sourceUsers.add(membership.getUserId());
                associatedByUser.add(membership.getUserId());
                associatedCount++;
            }
        }

        int primaryCount = 0;
        var primaryDepartmentUserIds = new HashSet<UUID>();
        var affectedUserIds = new LinkedHashSet<UUID>();
        for (var user : snapshot.users()) {
            if (snapshot.sourceDepartmentIds().contains(user.getPrimaryDepartmentId())) {
                primaryCount++;
                primaryDepartmentUserIds.add(user.getId());
                affectedUserIds.add(user.getId());
            }
            if (sourceUsers.contains(user.getId())) {
                affectedUserIds.add(user.getId());
            }
        }

        int insertedAssociations = (int) associatedByUser.stream()
                .filter(userId -> !primaryDepartmentUserIds.contains(userId))
                .count();
        var fingerprintParts = new ArrayList<String>();
        snapshot.users()
                .stream()
                .sorted(java.util.Comparator.comparing(User::getId))
                .forEach(user -> fingerprintParts.add("P:" + user.getId() + ":" + user.getPrimaryDepartmentId()));
        snapshot.memberships()
                .stream()
                .filter(item -> snapshot.sourceDepartmentIds().contains(item.getDepartmentId()))
                .sorted(java.util.Comparator.comparing(UserDepartmentMembership::getUserId)
                        .thenComparing(UserDepartmentMembership::getDepartmentId))
                .forEach(item -> fingerprintParts.add("A:" + item.getUserId() + ":" + item.getDepartmentId()));
        return new DepartmentMembershipRestructureImpact(affectedUserIds, primaryCount,
                associatedCount, associatedCount - insertedAssociations, fingerprint(fingerprintParts));
    }

    private String fingerprint(List<String> parts) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(
                            String.join("|", parts).getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算成员关系预览摘要失败", exception);
        }
    }

    private List<UserDepartmentMembership> sourceMemberships(MembershipSnapshot snapshot,
                                                             Set<UUID> sourceDepartmentIds) {
        return snapshot.memberships()
                .stream()
                .filter(item -> sourceDepartmentIds.contains(item.getDepartmentId()))
                .toList();
    }

    private Set<UUID> primaryDepartmentUserIds(List<User> users, Set<UUID> sourceDepartmentIds) {
        return users.stream()
                .filter(user -> sourceDepartmentIds.contains(user.getPrimaryDepartmentId()))
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    private List<UserDepartmentMembership> targetAssociations(List<User> users,
                                                              List<UserDepartmentMembership> sourceMemberships,
                                                              Set<UUID> primaryDepartmentUserIds,
                                                              UUID targetDepartmentId) {
        var userIdsWithSourceAssociation = new LinkedHashSet<UUID>();
        for (var membership : sourceMemberships) {
            if (!primaryDepartmentUserIds.contains(membership.getUserId())) {
                userIdsWithSourceAssociation.add(membership.getUserId());
            }
        }
        var usersById = new HashMap<UUID, User>();
        for (var user : users) {
            usersById.put(user.getId(), user);
        }
        var result = new ArrayList<UserDepartmentMembership>();
        for (var userId : userIdsWithSourceAssociation) {
            if (!usersById.containsKey(userId)) {
                throw new DataException("关联成员在重组过程中发生变化");
            }
            var membership = new UserDepartmentMembership();
            membership.setUserId(userId);
            membership.setDepartmentId(targetDepartmentId);
            result.add(membership);
        }
        return result;
    }

    private void insertAssociations(List<UserDepartmentMembership> memberships, UUID operatorId) {
        if (memberships.isEmpty()) {
            return;
        }
        int inserted = membershipMapper.batchInsertAssociations(memberships, operatorId);
        requireAffectedRows(inserted, memberships.size(), "写入重组后的关联部门关系失败");
    }

    private List<UUID> normalizeDepartments(Collection<UUID> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            throw new DataException("源部门不能为空");
        }
        var result = new LinkedHashSet<UUID>();
        for (var departmentId : departmentIds) {
            if (departmentId == null || !result.add(departmentId)) {
                throw new DataException("源部门不能为空且不能重复");
            }
        }
        return List.copyOf(result);
    }

    private List<UUID> normalizeUsers(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new DataException("请至少选择一名直属成员");
        }
        var result = new LinkedHashSet<UUID>();
        for (var userId : userIds) {
            if (userId == null || !result.add(userId)) {
                throw new DataException("拆分成员不能为空且不能重复");
            }
        }
        return List.copyOf(result);
    }

    private void validateSelectedUsers(List<UUID> selectedIds, List<User> users) {
        var actualIds = users.stream().map(User::getId).collect(Collectors.toSet());
        if (users.size() != selectedIds.size() || !actualIds.containsAll(selectedIds)) {
            throw new DataException("拆分成员不存在或已不再直接属于源部门，请重新预览");
        }
    }

    private void requireTargetAndOperator(UUID targetDepartmentId, UUID operatorId) {
        if (targetDepartmentId == null || operatorId == null) {
            throw new DataException("部门重组参数无效");
        }
    }

    private void requireAffectedRows(int actual, int expected, String message) {
        if (actual != expected) {
            throw new DataException(message);
        }
    }

    private record MembershipSnapshot(List<User> users, List<UserDepartmentMembership> memberships,
                                      Set<UUID> sourceDepartmentIds) {
    }
}
