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
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.javabean.vo.UserOnboardingVO;
import com.devops00.spectra.core.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户开通连续提交服务测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@ExtendWith(MockitoExtension.class)
class UserOnboardingServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthorizationAssignmentChangeService assignmentChangeService;

    @Mock
    private AuthorizationAssignmentQueryService assignmentQueryService;

    @InjectMocks
    private UserOnboardingServiceImpl service;

    @Test
    void submitShouldCreateUserAndApplyAuthorizationInOrder() {
        var userId = UUID.randomUUID();
        var userParams = new UserSaveFrom();
        var authorization = new AuthorizationAssignmentChangeFrom();
        authorization.setRoleId(UUID.randomUUID());
        authorization.setExpectedVersion(0L);
        authorization.setBoundaries(List.of());
        var request = request(userParams, authorization);
        when(userService.create(userParams)).thenReturn(new UserCreatedVO(userId, "测试用户"));
        when(assignmentQueryService.findByUserId(userId)).thenReturn(List.of());

        var preview = new AuthorizationChangePreviewVO();
        preview.setPreviewToken("preview-token");
        preview.setAssignmentId(null);
        when(assignmentChangeService.preview(userId, authorization)).thenReturn(preview);

        UserOnboardingVO result = service.submit(request);

        assertEquals(userId, result.getId());
        verify(userService).create(userParams);
        verify(userService, never()).modify(any());
        var applyCaptor = ArgumentCaptor.forClass(AuthorizationAssignmentApplyFrom.class);
        verify(assignmentChangeService).apply(eq(userId), applyCaptor.capture());
        assertEquals("preview-token", applyCaptor.getValue().getPreviewToken());
        assertEquals(authorization.getRoleId(), applyCaptor.getValue().getRoleId());
        assertEquals(authorization.getExpectedVersion(), applyCaptor.getValue().getExpectedVersion());
        assertEquals(authorization.getBoundaries(), applyCaptor.getValue().getBoundaries());
    }

    @Test
    void submitShouldModifyExistingUserBeforeApplyingAuthorization() {
        var userId = UUID.randomUUID();
        var userParams = new UserSaveFrom();
        userParams.setId(userId);
        userParams.setRealName("编辑用户");
        var authorization = new AuthorizationAssignmentChangeFrom();
        var request = request(userParams, authorization);
        var preview = new AuthorizationChangePreviewVO();
        preview.setPreviewToken("preview-token");
        when(assignmentQueryService.findByUserId(userId)).thenReturn(List.of());
        when(assignmentChangeService.preview(userId, authorization)).thenReturn(preview);

        UserOnboardingVO result = service.submit(request);

        assertEquals(userId, result.getId());
        assertEquals("编辑用户", result.getRealName());
        verify(userService).modify(userParams);
        verify(userService, never()).create(any());
        verify(assignmentChangeService).apply(eq(userId), any(AuthorizationAssignmentApplyFrom.class));
    }

    @Test
    void submitShouldReconcileExistingAssignmentsAndRevokeRemovedRole() {
        var userId = UUID.randomUUID();
        var keptAssignmentId = UUID.randomUUID();
        var removedAssignmentId = UUID.randomUUID();
        var keptRoleId = UUID.randomUUID();
        var removedRoleId = UUID.randomUUID();
        var userParams = new UserSaveFrom();
        userParams.setId(userId);
        userParams.setRealName("编辑用户");
        var kept = new AuthorizationAssignmentChangeFrom();
        kept.setAssignmentId(keptAssignmentId);
        kept.setRoleId(keptRoleId);
        kept.setExpectedVersion(2L);
        kept.setBoundaries(List.of());
        var removal = new AuthorizationAssignmentRemovalFrom();
        removal.setAssignmentId(removedAssignmentId);
        removal.setExpectedVersion(4L);
        var authorization = new AuthorizationAssignmentsChangeFrom();
        authorization.setAssignments(List.of(kept));
        authorization.setRemovedAssignments(List.of(removal));
        var request = new UserOnboardingFrom();
        request.setUser(userParams);
        request.setAuthorization(authorization);
        when(assignmentQueryService.findByUserId(userId)).thenReturn(List.of(
                assignmentView(keptAssignmentId, keptRoleId, "ACTIVE", 2L),
                assignmentView(removedAssignmentId, removedRoleId, "ACTIVE", 4L)));
        var preview = new AuthorizationChangePreviewVO();
        preview.setPreviewToken("preview-token");
        when(assignmentChangeService.preview(userId, kept)).thenReturn(preview);

        service.submit(request);

        verify(assignmentChangeService).revoke(eq(userId), eq(removal));
        verify(assignmentChangeService).preview(userId, kept);
        verify(assignmentChangeService).apply(eq(userId), any(AuthorizationAssignmentApplyFrom.class));
    }

    private UserOnboardingFrom request(UserSaveFrom user, AuthorizationAssignmentChangeFrom assignment) {
        var authorization = new AuthorizationAssignmentsChangeFrom();
        authorization.setAssignments(List.of(assignment));
        authorization.setRemovedAssignments(List.of());
        var request = new UserOnboardingFrom();
        request.setUser(user);
        request.setAuthorization(authorization);
        return request;
    }

    private AuthorizationAssignmentView assignmentView(UUID assignmentId, UUID roleId, String state, long version) {
        return new AuthorizationAssignmentView(assignmentId, UUID.randomUUID(), roleId, "ROLE_TEST", "BUSINESS",
                "测试角色", false, "ACTIVE", 1L, 1L, version, state, null, null, List.of(), List.of());
    }
}
