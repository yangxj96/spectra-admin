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

import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentsChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.javabean.vo.UserOnboardingVO;
import com.devops00.spectra.core.user.service.UserOnboardingService;
import com.devops00.spectra.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;
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

    @Override
    @Transactional
    public UserOnboardingVO submit(UserOnboardingFrom params) {
        var userParams = params.getUser();
        UserOnboardingVO user = submitUser(userParams);
        submitAuthorization(user.getId(), params.getAuthorization());
        return user;
    }

    private UserOnboardingVO submitUser(UserSaveFrom params) {
        if (params.getId() == null) {
            UserCreatedVO created = userService.create(params);
            return new UserOnboardingVO(created.getId(), created.getRealName());
        }
        userService.modify(params);
        return new UserOnboardingVO(params.getId(), params.getRealName());
    }

    private void submitAuthorization(UUID userId, AuthorizationAssignmentsChangeFrom params) {
        validateAssignments(userId, params);
        for (var removal : params.getRemovedAssignments() == null
                ? List.<AuthorizationAssignmentRemovalFrom>of()
                : params.getRemovedAssignments()) {
            assignmentChangeService.revoke(userId, removal);
        }
        for (var assignment : params.getAssignments()) {
            applyAssignment(userId, assignment);
        }
    }

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

    private void validateAssignments(UUID userId, AuthorizationAssignmentsChangeFrom params) {
        if (params == null || params.getAssignments() == null || params.getAssignments().isEmpty()) {
            throw new com.devops00.spectra.common.exception.DataException("至少保留一个角色授权");
        }
        var activeAssignments = assignmentQueryService.findByUserId(userId)
                .stream()
                .filter(assignment -> "ACTIVE".equals(assignment.state()))
                .toList();
        var activeAssignmentIds = activeAssignments.stream()
                .map(assignment -> assignment.assignmentId())
                .collect(Collectors.toSet());
        var requestedAssignmentIds = new HashSet<UUID>();
        var requestedRoleIds = new HashSet<UUID>();
        for (var assignment : params.getAssignments()) {
            if (assignment.getAssignmentId() != null && !requestedAssignmentIds.add(assignment.getAssignmentId())) {
                throw new com.devops00.spectra.common.exception.DataException("同一角色授权不能重复提交");
            }
            if (!requestedRoleIds.add(assignment.getRoleId())) {
                throw new com.devops00.spectra.common.exception.DataException("同一角色不能重复授权");
            }
        }
        var removedIds = new HashSet<UUID>();
        if (params.getRemovedAssignments() != null) {
            for (var removal : params.getRemovedAssignments()) {
                if (!removedIds.add(removal.getAssignmentId())) {
                    throw new com.devops00.spectra.common.exception.DataException("同一角色授权不能重复移除");
                }
                if (!activeAssignmentIds.contains(removal.getAssignmentId())) {
                    throw new com.devops00.spectra.common.exception.DataException("待移除的角色授权已不存在或已失效");
                }
            }
        }
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
