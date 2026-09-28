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
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentsChangeFrom;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.UserOnboardingFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserCreatedVO;
import com.devops00.spectra.core.user.service.UserDepartmentMembershipService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

/**
 * 验证用户资料、部门关系和授权配置在同一 onboarding 流程中提交。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
class UserOnboardingServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OPERATOR_ID = UUID.randomUUID();
    private static final UUID OLD_PRIMARY_ID = UUID.randomUUID();
    private static final UUID NEW_PRIMARY_ID = UUID.randomUUID();
    private static final UUID OLD_ASSOCIATED_ID = UUID.randomUUID();
    private static final UUID NEW_ASSOCIATED_ID = UUID.randomUUID();

    private UserService userService;
    private AuthorizationAssignmentChangeService assignmentChangeService;
    private AuthorizationAssignmentQueryService assignmentQueryService;
    private UserDepartmentMembershipService membershipService;
    private AuditService auditService;
    private SecurityContextAccessor securityContextAccessor;
    private AuthorizationSnapshotProvider authorizationSnapshotProvider;
    private DepartmentMapper departmentMapper;
    private UserOnboardingServiceImpl onboardingService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        assignmentChangeService = mock(AuthorizationAssignmentChangeService.class);
        assignmentQueryService = mock(AuthorizationAssignmentQueryService.class);
        membershipService = mock(UserDepartmentMembershipService.class);
        auditService = mock(AuditService.class);
        securityContextAccessor = mock(SecurityContextAccessor.class);
        authorizationSnapshotProvider = mock(AuthorizationSnapshotProvider.class);
        departmentMapper = mock(DepartmentMapper.class);
        AuditSanitizer sanitizer = snapshot -> new LinkedHashMap<>(snapshot);
        onboardingService = new UserOnboardingServiceImpl(userService, assignmentChangeService,
                assignmentQueryService, membershipService, new AuditRecordFactory(sanitizer), auditService,
                securityContextAccessor, authorizationSnapshotProvider, departmentMapper);
        when(securityContextAccessor.currentUserId()).thenReturn(OPERATOR_ID);
        when(authorizationSnapshotProvider.load(OPERATOR_ID)).thenReturn(rootSnapshot());
        when(departmentMapper.selectList(any())).thenReturn(List.of(
                department(OLD_PRIMARY_ID), department(NEW_PRIMARY_ID),
                department(OLD_ASSOCIATED_ID), department(NEW_ASSOCIATED_ID)));
    }

    @Test
    void updatesPrimaryAndAssociatedDepartmentsTogetherAndAuditsBeforeAndAfter() {
        var existing = new User();
        existing.setId(USER_ID);
        existing.setPrimaryDepartmentId(OLD_PRIMARY_ID);
        when(userService.getById(USER_ID)).thenReturn(existing);
        when(membershipService.findAssociatedDepartmentIds(USER_ID)).thenReturn(List.of(OLD_ASSOCIATED_ID));
        when(assignmentQueryService.findByUserId(USER_ID)).thenReturn(List.of());

        onboardingService.submit(request(USER_ID, NEW_PRIMARY_ID, List.of(NEW_ASSOCIATED_ID)));

        verify(userService).modify(any(UserSaveFrom.class));
        verify(membershipService).replace(USER_ID, NEW_PRIMARY_ID, List.of(NEW_ASSOCIATED_ID), OPERATOR_ID);
        var audit = ArgumentCaptor.forClass(AuditRecord.class);
        verify(auditService).record(audit.capture());
        assertEquals("USER_DEPARTMENT_MEMBERSHIP_CHANGED", audit.getValue().eventType());
        assertEquals(OLD_PRIMARY_ID, audit.getValue().before().get("primaryDepartmentId"));
        assertEquals(List.of(OLD_ASSOCIATED_ID), audit.getValue().before().get("associatedDepartmentIds"));
        assertEquals(NEW_PRIMARY_ID, audit.getValue().after().get("primaryDepartmentId"));
        assertEquals(List.of(NEW_ASSOCIATED_ID), audit.getValue().after().get("associatedDepartmentIds"));
    }

    @Test
    void membershipFailureRollsBackOnboardingBeforeAuthorizationChanges() throws NoSuchMethodException {
        when(userService.create(any(UserSaveFrom.class))).thenReturn(new UserCreatedVO(USER_ID, "测试用户"));
        when(membershipService.findAssociatedDepartmentIds(USER_ID)).thenReturn(List.of());
        doThrow(new DataException("关联部门写入失败"))
                .when(membershipService)
                .replace(USER_ID, NEW_PRIMARY_ID, List.of(), OPERATOR_ID);

        assertThrows(DataException.class,
                () -> onboardingService.submit(request(null, NEW_PRIMARY_ID, List.of())));

        verify(assignmentQueryService, never()).findByUserId(USER_ID);
        verify(assignmentChangeService, never()).preview(any(), any());
        verify(auditService, never()).record(any());
        assertTrue(UserOnboardingServiceImpl.class.getMethod("submit", UserOnboardingFrom.class)
                .isAnnotationPresent(Transactional.class));
    }

    @Test
    void rejectsNewUserInDepartmentOutsideUserCreateBoundary() {
        var boundary = new PermissionBoundary("user:create",
                new AuthorizationScope(ScopeMode.RULES, Set.of(OLD_PRIMARY_ID), false));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "ROLE_ADMIN_SYSTEM", 2,
                Map.of("user:create", boundary), Map.of());
        when(authorizationSnapshotProvider.load(OPERATOR_ID))
                .thenReturn(AuthorizationSnapshot.of(List.of(assignment), Set.of(OLD_PRIMARY_ID)));

        assertThrows(com.devops00.spectra.common.exception.DataNotExistException.class,
                () -> onboardingService.submit(request(null, NEW_PRIMARY_ID, List.of())));

        verify(userService, never()).create(any(UserSaveFrom.class));
        verify(membershipService, never()).replace(any(), any(), any(), any());
    }

    private static UserOnboardingFrom request(UUID userId, UUID primaryDepartmentId,
                                              List<UUID> associatedDepartmentIds) {
        var user = new UserSaveFrom();
        user.setId(userId);
        user.setPrimaryDepartmentId(primaryDepartmentId);
        user.setAssociatedDepartmentIds(associatedDepartmentIds);
        user.setRealName("测试用户");
        var authorization = new AuthorizationAssignmentsChangeFrom();
        authorization.setAssignments(List.of());
        authorization.setRemovedAssignments(List.of());
        var request = new UserOnboardingFrom();
        request.setUser(user);
        request.setAuthorization(authorization);
        return request;
    }

    private static Department department(UUID id) {
        var department = new Department();
        department.setId(id);
        return department;
    }

    private static AuthorizationSnapshot rootSnapshot() {
        var root = new AuthorizationAssignment(UUID.randomUUID(), "ROLE_DEV_OPS", 100,
                Map.of(), Map.of());
        return AuthorizationSnapshot.of(List.of(root));
    }
}
