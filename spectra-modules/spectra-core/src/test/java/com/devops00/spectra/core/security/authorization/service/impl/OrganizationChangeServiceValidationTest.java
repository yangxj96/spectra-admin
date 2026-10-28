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

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.domain.OrganizationChangeImpact;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.service.OrganizationChangeAuthorization;
import com.devops00.spectra.core.security.authorization.service.OrganizationImpactAnalyzer;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import com.devops00.spectra.core.security.change.HighRiskApprovalGate;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.core.system.service.DepartmentOrganizationService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.service.UserSecurityStateService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationChangeServiceValidationTest {

    private final DepartmentOrganizationService departmentOrganizationService = mock(
            DepartmentOrganizationService.class);
    private final RoleAssignmentMapper roleAssignmentMapper = mock(RoleAssignmentMapper.class);
    private final UserSecurityStateService userSecurityStateService = mock(UserSecurityStateService.class);
    private final DepartmentService departmentService = mock(DepartmentService.class);
    private final OrganizationChangeAuthorization organizationChangeAuthorization = mock(
            OrganizationChangeAuthorization.class);
    private final OrganizationImpactAnalyzer impactAnalyzer = mock(OrganizationImpactAnalyzer.class);
    private final AuthorizationChangeTokenService tokenService = mock(AuthorizationChangeTokenService.class);
    private final AuthorizationEpochGuard epochGuard = mock(AuthorizationEpochGuard.class);
    private final SecuritySessionRevocationPort sessionRevocationPort = mock(SecuritySessionRevocationPort.class);
    private final SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
    private final SecurityChangeExecutor securityChangeExecutor = mock(SecurityChangeExecutor.class);
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider = mock(ObjectProvider.class);
    private final AuditService auditService = mock(AuditService.class);
    private final AuditRecordFactory auditRecordFactory = mock(AuditRecordFactory.class);
    private final TimeMapper timeMapper = mock(TimeMapper.class);

    private final UUID operatorId = UUID.randomUUID();

    @Test
    void createTopLevelDepartmentDoesNotTreatNullIdentifiersAsSelfMove() {
        var from = changeFrom(null);

        assertDoesNotThrow(() -> service().previewCreate(from));
    }

    @Test
    void updateStillRejectsMovingDepartmentToItself() {
        var departmentId = UUID.randomUUID();
        when(departmentOrganizationService.find(departmentId)).thenReturn(snapshot(departmentId));
        var from = changeFrom(departmentId);

        assertThrows(DataException.class, () -> service().preview(departmentId, from));
    }

    @Test
    void updateStillRejectsMovingDepartmentIntoItsOwnDescendant() {
        var departmentId = UUID.randomUUID();
        var parentId = UUID.randomUUID();
        when(departmentOrganizationService.find(departmentId)).thenReturn(snapshot(departmentId));
        when(departmentOrganizationService.find(parentId)).thenReturn(snapshot(parentId));
        when(departmentService.getSelfAndDescendantIds(departmentId)).thenReturn(Set.of(departmentId, parentId));
        var from = changeFrom(parentId);

        assertThrows(DataException.class, () -> service().preview(departmentId, from));
    }

    private OrganizationChangeServiceImpl service() {
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(impactAnalyzer.analyze(anyLong(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(new OrganizationChangeImpact(0L, 1L, false, 0, 0));
        when(tokenService.issue(any())).thenReturn("preview-token");
        when(roleAssignmentMapper.selectList(any())).thenReturn(List.of());
        return new OrganizationChangeServiceImpl(departmentOrganizationService, roleAssignmentMapper,
                userSecurityStateService, departmentService, organizationChangeAuthorization, impactAnalyzer,
                tokenService, epochGuard, sessionRevocationPort, securityContextAccessor, securityChangeExecutor,
                approvalGateProvider, auditService, auditRecordFactory, timeMapper);
    }

    private static DepartmentOrganizationService.DepartmentSnapshot snapshot(UUID id) {
        return new DepartmentOrganizationService.DepartmentSnapshot(id, null, null, "测试部门", "1", UUID.randomUUID(),
                null, null);
    }

    private static OrganizationChangeFrom changeFrom(UUID parentId) {
        var from = new OrganizationChangeFrom();
        from.setExpectedOrganizationVersion(0L);
        from.setNewParentId(parentId);
        from.setName("测试部门");
        from.setType((short) 1);
        from.setRegionId(UUID.randomUUID());
        return from;
    }
}
