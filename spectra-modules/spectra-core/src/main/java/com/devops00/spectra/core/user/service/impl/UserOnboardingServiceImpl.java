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

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.core.security.authorization.AuthorizationDepartmentScope;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.constant.SecurityRoleCodes;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentsChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.javabean.vo.UserOnboardingVO;
import com.devops00.spectra.core.user.service.UserDepartmentMembershipService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.service.UserOnboardingService;
import com.devops00.spectra.core.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户资料和多角色授权连续提交服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Service
@RequiredArgsConstructor
public class UserOnboardingServiceImpl implements UserOnboardingService {

    private final UserService userService;

    private final AuthorizationAssignmentChangeService assignmentChangeService;

    private final AuthorizationAssignmentQueryService assignmentQueryService;

    private final UserDepartmentMembershipService membershipService;

    private final AuditRecordFactory auditRecordFactory;

    private final AuditService auditService;

    private final SecurityContextAccessor securityContextAccessor;

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public UserOnboardingVO submit(UserOnboardingFrom params) {
        var userParams = params.getUser();
        User previous = userParams.getId() == null ? null : userService.getById(userParams.getId());
        var previousAssociatedIds = previous == null
                ? List.<UUID>of()
                : membershipService.findAssociatedDepartmentIds(previous.getId());
        var previousPrimaryDepartmentId = previous == null ? null : previous.getPrimaryDepartmentId();

        assertUserAndDepartmentScope(userParams, previous, previousAssociatedIds);

        UserOnboardingVO user = submitUser(userParams);
        UUID operatorId = securityContextAccessor.currentUserId();
        membershipService.replace(user.getId(), userParams.getPrimaryDepartmentId(),
                userParams.getAssociatedDepartmentIds(), operatorId);
        recordDepartmentMembershipChange(new DepartmentMembershipChangeInput(user.getId(), operatorId,
                previousPrimaryDepartmentId, previousAssociatedIds, userParams.getPrimaryDepartmentId(),
                userParams.getAssociatedDepartmentIds()));
        submitAuthorization(user.getId(), params.getAuthorization());
        return user;
    }

    private void assertUserAndDepartmentScope(UserSaveFrom requested, User previous,
                                              List<UUID> previousAssociatedDepartmentIds) {
        UUID viewerId = securityContextAccessor.currentUserId();
        AuthorizationSnapshot authorization = viewerId == null
                ? null
                : authorizationSnapshotProvider.load(viewerId);
        var departments = departmentMapper.selectList(new QueryWrapper<Department>().select("id", "pid"));

        String permission = previous == null ? "user:create" : "user:update";
        if (previous != null) {
            Set<UUID> previousDepartmentIds = new HashSet<>(previousAssociatedDepartmentIds);
            if (previous.getPrimaryDepartmentId() != null) {
                previousDepartmentIds.add(previous.getPrimaryDepartmentId());
            }
            if (!AuthorizationDepartmentScope.canAccessUser(new AuthorizationDepartmentScope.UserAccessQuery(
                    authorization, permission, viewerId, previous.getId(), previousDepartmentIds, departments))) {
                throw new com.devops00.spectra.common.exception.DataNotExistException("用户不存在");
            }
        }

        Set<UUID> requestedDepartmentIds = new HashSet<>();
        if (requested.getPrimaryDepartmentId() != null) {
            requestedDepartmentIds.add(requested.getPrimaryDepartmentId());
        }
        if (requested.getAssociatedDepartmentIds() != null) {
            requestedDepartmentIds.addAll(requested.getAssociatedDepartmentIds());
        }
        var allowedDepartmentIds = AuthorizationDepartmentScope.isUnrestricted(authorization, permission)
                ? departments.stream().map(Department::getId).collect(Collectors.toSet())
                : AuthorizationDepartmentScope.visibleDepartmentIds(authorization, permission, departments);
        if (!allowedDepartmentIds.containsAll(requestedDepartmentIds)) {
            throw new com.devops00.spectra.common.exception.DataNotExistException("部门不存在或无权访问");
        }
    }

    /**
     * 处理内部业务逻辑（{@code submitUser}）。
     */
    private UserOnboardingVO submitUser(UserSaveFrom params) {
        if (params.getId() == null) {
            UserCreatedVO created = userService.create(params);
            return new UserOnboardingVO(created.getId(), created.getRealName());
        }
        userService.modify(params);
        return new UserOnboardingVO(params.getId(), params.getRealName());
    }

    /**
     * 记录主部门和关联部门列表的变更前后快照。
     */
    private void recordDepartmentMembershipChange(DepartmentMembershipChangeInput input) {
        Map<String, Object> before = departmentSnapshot(input.previousPrimaryDepartmentId(), input.previousAssociatedIds());
        Map<String, Object> after = departmentSnapshot(input.primaryDepartmentId(), input.associatedDepartmentIds());
        if (before.equals(after)) {
            return;
        }
        var event = auditRecordFactory.create(null, "USER_DEPARTMENT_MEMBERSHIP_CHANGED", input.operatorId(), input.userId(),
                null, null, null, before, after, "更新用户部门成员关系", null, AuditRecord.Result.SUCCEEDED,
                RequestCorrelationContext.current().correlationId());
        auditService.record(event);
    }

    private record DepartmentMembershipChangeInput(UUID userId, UUID operatorId, UUID previousPrimaryDepartmentId,
                                                   List<UUID> previousAssociatedIds, UUID primaryDepartmentId,
                                                   List<UUID> associatedDepartmentIds) {
    }

    /**
     * 规范化部门快照，避免列表输入顺序影响审计比较。
     */
    private Map<String, Object> departmentSnapshot(UUID primaryDepartmentId, List<UUID> associatedDepartmentIds) {
        var snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("primaryDepartmentId", primaryDepartmentId);
        var normalizedAssociatedIds = associatedDepartmentIds == null
                ? List.<UUID>of()
                : associatedDepartmentIds.stream().sorted().toList();
        snapshot.put("associatedDepartmentIds", normalizedAssociatedIds);
        return snapshot;
    }

    /**
     * 处理内部业务逻辑（{@code submitAuthorization}）。
     */
    private void submitAuthorization(UUID userId, AuthorizationAssignmentsChangeFrom params) {
        validateAssignments(userId, params);
        for (var removal : params.getRemovedAssignments() == null
                ? List.<AuthorizationAssignmentRemovalFrom>of()
                : params.getRemovedAssignments()) {
            assignmentChangeService.revoke(userId, removal);
        }
        var assignments = params.getAssignments() == null
                ? List.<AuthorizationAssignmentChangeFrom>of()
                : params.getAssignments();
        for (var assignment : assignments) {
            applyAssignment(userId, assignment);
        }
    }

    /**
     * 更新或推进目标状态（{@code applyAssignment}）。
     */
    private void applyAssignment(UUID userId, AuthorizationAssignmentChangeFrom params) {
        AuthorizationChangePreviewVO preview = assignmentChangeService.preview(userId, params);
        var apply = new AuthorizationAssignmentApplyFrom();
        apply.setAssignmentId(preview.getAssignmentId());
        apply.setRoleId(params.getRoleId());
        apply.setExpectedVersion(params.getExpectedVersion());
        apply.setBoundaries(params.getBoundaries());
        apply.setPreviewToken(preview.getPreviewToken());
        assignmentChangeService.apply(userId, apply);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateAssignments}）。
     */
    private void validateAssignments(UUID userId, AuthorizationAssignmentsChangeFrom params) {
        if (params == null) {
            throw new com.devops00.spectra.common.exception.DataException("角色授权参数不能为空");
        }
        var activeAssignments = assignmentQueryService.findByUserId(userId)
                .stream()
                .filter(assignment -> SecurityAuthorizationState.ACTIVE.name().equals(assignment.state()))
                .filter(assignment -> !SecurityRoleCodes.DEFAULT_USER.equals(assignment.roleCode()))
                .toList();
        var activeAssignmentIds = activeAssignments.stream()
                .map(assignment -> assignment.assignmentId())
                .collect(Collectors.toSet());
        var requestedAssignmentIds = new HashSet<UUID>();
        var requestedRoleIds = new HashSet<UUID>();
        var requestedAssignments = params.getAssignments() == null
                ? List.<AuthorizationAssignmentChangeFrom>of()
                : params.getAssignments();
        validateRequestedAssignments(requestedAssignments, requestedAssignmentIds, requestedRoleIds);
        var removedIds = new HashSet<UUID>();
        validateRemovedAssignments(params.getRemovedAssignments(), activeAssignmentIds, removedIds);
        validateReconciledAssignments(activeAssignmentIds, requestedAssignmentIds, removedIds);
    }

    private void validateRequestedAssignments(List<AuthorizationAssignmentChangeFrom> assignments,
                                              Set<UUID> requestedAssignmentIds, Set<UUID> requestedRoleIds) {
        for (var assignment : assignments) {
            if (assignment.getAssignmentId() != null && !requestedAssignmentIds.add(assignment.getAssignmentId())) {
                throw new com.devops00.spectra.common.exception.DataException("同一角色授权不能重复提交");
            }
            if (!requestedRoleIds.add(assignment.getRoleId())) {
                throw new com.devops00.spectra.common.exception.DataException("同一角色不能重复授权");
            }
        }
    }

    private void validateRemovedAssignments(List<AuthorizationAssignmentRemovalFrom> removals,
                                            Set<UUID> activeAssignmentIds, Set<UUID> removedIds) {
        if (removals == null) {
            return;
        }
        for (var removal : removals) {
            if (!removedIds.add(removal.getAssignmentId())) {
                throw new com.devops00.spectra.common.exception.DataException("同一角色授权不能重复移除");
            }
            if (!activeAssignmentIds.contains(removal.getAssignmentId())) {
                throw new com.devops00.spectra.common.exception.DataException("待移除的角色授权已不存在或已失效");
            }
        }
    }

    private void validateReconciledAssignments(Set<UUID> activeAssignmentIds, Set<UUID> requestedAssignmentIds,
                                               Set<UUID> removedIds) {
        if (!Collections.disjoint(requestedAssignmentIds, removedIds)) {
            throw new com.devops00.spectra.common.exception.DataException("角色授权不能同时保留和移除");
        }
        var reconciledIds = new HashSet<>(requestedAssignmentIds);
        reconciledIds.addAll(removedIds);
        if (!reconciledIds.equals(activeAssignmentIds)) {
            throw new com.devops00.spectra.common.exception.DataException("当前用户角色授权已发生变化，请刷新后重试");
        }
    }
}
