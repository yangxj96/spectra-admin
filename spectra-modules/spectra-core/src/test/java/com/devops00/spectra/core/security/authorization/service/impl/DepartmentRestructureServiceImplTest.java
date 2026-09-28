/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergePreviewFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentRestructureDepartmentFrom;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceImpact;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceService;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import com.devops00.spectra.core.security.change.AuthorizationChangeToken;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.mapper.DepartmentClosureMapper;
import com.devops00.spectra.core.system.mapper.OrganizationVersionMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.entity.OrganizationVersion;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureImpact;
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

/**
 * 验证部门合并与部分拆分的 Preview/Apply 重校验及事务边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
class DepartmentRestructureServiceImplTest {

    @Test
    void previewRejectsDuplicateSourceDepartmentIdsBeforeReadingOrganization() {
        var service = service(mock(DepartmentMapper.class), mock(OrganizationVersionMapper.class),
                mock(SecurityContextAccessor.class));
        var from = new DepartmentMergePreviewFrom();
        from.setSourceDepartmentIds(List.of(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()));
        from.setExpectedOrganizationVersion(1L);
        from.setDepartment(new DepartmentRestructureDepartmentFrom());

        var duplicateId = from.getSourceDepartmentIds().getFirst();
        from.setSourceDepartmentIds(List.of(duplicateId, duplicateId));

        assertThrows(DataException.class, () -> service.previewMerge(from));
    }

    @Test
    void mergeRejectsSourcesFromDifferentParentsBeforeReadingOrganizationVersion() {
        var departmentMapper = mock(DepartmentMapper.class);
        var organizationVersionMapper = mock(OrganizationVersionMapper.class);
        var securityContextAccessor = mock(SecurityContextAccessor.class);
        var operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);

        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        var firstParentId = UUID.randomUUID();
        var secondParentId = UUID.randomUUID();
        var first = department(firstId, firstParentId);
        var second = department(secondId, secondParentId);
        var sourceIds = List.of(firstId, secondId).stream().sorted().toList();
        when(departmentMapper.selectActiveByIds(sourceIds)).thenReturn(sourceIds.stream()
                .map(id -> id.equals(firstId) ? first : second).toList());
        var from = new DepartmentMergePreviewFrom();
        from.setSourceDepartmentIds(sourceIds);
        from.setExpectedOrganizationVersion(3L);
        from.setDepartment(validDepartmentForm());

        var service = service(departmentMapper, organizationVersionMapper, securityContextAccessor);

        assertThrows(DataException.class, () -> service.previewMerge(from));
        verify(organizationVersionMapper, never()).selectSystem();
    }

    @Test
    void previewTokenBindsNormalizedRequestOperatorOperationAndOrganizationVersion() {
        var departmentMapper = mock(DepartmentMapper.class);
        var organizationVersionMapper = mock(OrganizationVersionMapper.class);
        var securityContextAccessor = mock(SecurityContextAccessor.class);
        var membershipService = mock(DepartmentMembershipRestructureService.class);
        var authorizationService = mock(DepartmentAuthorizationReferenceService.class);
        var tokenService = mock(AuthorizationChangeTokenService.class);
        var operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);

        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        var sourceIds = List.of(firstId, secondId).stream().sorted().toList();
        var sources = sourceIds.stream().map(id -> department(id, null)).toList();
        when(departmentMapper.selectActiveByIds(sourceIds)).thenReturn(sources);
        when(departmentMapper.selectActiveChildrenByParentIds(sourceIds)).thenReturn(List.of());
        var versionRow = new OrganizationVersion();
        versionRow.setOrganizationVersion(11L);
        when(organizationVersionMapper.selectSystem()).thenReturn(versionRow);
        when(membershipService.previewMerge(sourceIds)).thenReturn(
                new DepartmentMembershipRestructureImpact(Set.of(), 0, 0, 0, "membership-snapshot"));
        when(authorizationService.previewMerge(anyList(), anyList(), any(), anyString())).thenReturn(
                new DepartmentAuthorizationReferenceImpact(0, 0, 0, 0, 0, 0,
                        false, Set.of(), "authorization-snapshot"));
        when(tokenService.issue(any(AuthorizationChangeToken.class))).thenReturn("signed-preview-token");

        var service = new DepartmentRestructureServiceImpl(
                departmentMapper, mock(DepartmentClosureMapper.class), organizationVersionMapper,
                mock(DepartmentService.class), membershipService, authorizationService, mock(UserMapper.class),
                mock(UserDepartmentMembershipMapper.class), tokenService, securityContextAccessor,
                mock(SecurityChangeExecutor.class), mock(AuthorizationEpochGuard.class),
                mock(SecuritySessionRevocationPort.class), mock(ObjectProvider.class), mock(AuditService.class),
                mock(AuditRecordFactory.class), mock(TimeMapper.class));
        var from = new DepartmentMergePreviewFrom();
        from.setSourceDepartmentIds(List.of(secondId, firstId));
        from.setExpectedOrganizationVersion(11L);
        var newDepartment = validDepartmentForm();
        newDepartment.setName("全新部门");
        from.setDepartment(newDepartment);

        var preview = service.previewMerge(from);
        var tokenCaptor = ArgumentCaptor.forClass(AuthorizationChangeToken.class);
        verify(tokenService).issue(tokenCaptor.capture());
        var token = tokenCaptor.getValue();

        assertEquals("signed-preview-token", preview.getPreviewToken());
        assertEquals("全新部门", preview.getNewDepartmentName());
        assertEquals(operatorId, token.operatorId());
        assertEquals(operatorId, token.targetUserId());
        assertEquals(11L, token.expectedVersion());
        assertEquals(UUID.nameUUIDFromBytes("DEPARTMENT_RESTRUCTURE:MERGE".getBytes(StandardCharsets.UTF_8)),
                token.roleId());
        assertEquals(UUID.nameUUIDFromBytes(("MERGE:" + token.requestHash()).getBytes(StandardCharsets.UTF_8)),
                token.assignmentId());
        assertTrue(token.expiresAt().isAfter(Instant.now()));
        assertTrue(!token.expiresAt().isAfter(Instant.now().plusSeconds(300)));
    }

    @Test
    void applyRejectsPreviewTokenForAnotherOperationBeforeDatabaseReads() {
        var departmentMapper = mock(DepartmentMapper.class);
        var organizationVersionMapper = mock(OrganizationVersionMapper.class);
        var securityContextAccessor = mock(SecurityContextAccessor.class);
        var tokenService = mock(AuthorizationChangeTokenService.class);
        var operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(tokenService.verify("signed-token")).thenReturn(new AuthorizationChangeToken(
                UUID.randomUUID(), operatorId, operatorId, UUID.randomUUID(), UUID.randomUUID(), 5L,
                "request-hash", Instant.now().plusSeconds(60)));
        var service = service(departmentMapper, organizationVersionMapper, securityContextAccessor, tokenService);
        var from = new DepartmentMergeApplyFrom();
        from.setSourceDepartmentIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
        from.setExpectedOrganizationVersion(5L);
        from.setPreviewToken("signed-token");

        assertThrows(DataException.class, () -> service.applyMerge(from));
        verifyNoInteractions(departmentMapper, organizationVersionMapper);
    }

    private DepartmentRestructureServiceImpl service(DepartmentMapper departmentMapper,
                                                      OrganizationVersionMapper organizationVersionMapper,
                                                      SecurityContextAccessor securityContextAccessor) {
        return service(departmentMapper, organizationVersionMapper, securityContextAccessor,
                mock(AuthorizationChangeTokenService.class));
    }

    private DepartmentRestructureServiceImpl service(DepartmentMapper departmentMapper,
                                                      OrganizationVersionMapper organizationVersionMapper,
                                                      SecurityContextAccessor securityContextAccessor,
                                                      AuthorizationChangeTokenService tokenService) {
        return new DepartmentRestructureServiceImpl(
                departmentMapper, mock(DepartmentClosureMapper.class), organizationVersionMapper,
                mock(DepartmentService.class), mock(DepartmentMembershipRestructureService.class),
                mock(DepartmentAuthorizationReferenceService.class), mock(UserMapper.class),
                mock(UserDepartmentMembershipMapper.class), tokenService,
                securityContextAccessor, mock(SecurityChangeExecutor.class), mock(AuthorizationEpochGuard.class),
                mock(SecuritySessionRevocationPort.class), mock(ObjectProvider.class), mock(AuditService.class),
                mock(AuditRecordFactory.class), mock(TimeMapper.class));
    }

    private Department department(UUID id, UUID parentId) {
        var department = new Department();
        department.setId(id);
        department.setPid(parentId);
        department.setCode("D-" + id);
        return department;
    }

    private DepartmentRestructureDepartmentFrom validDepartmentForm() {
        var form = new DepartmentRestructureDepartmentFrom();
        form.setName("合并结果");
        form.setType((short) 1);
        form.setRegionId(UUID.randomUUID());
        return form;
    }
}
