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

import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfileAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfileBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationScope;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证部门合并只重写活动授权引用并保留历史与 JSONB 无关字段。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
class DepartmentAuthorizationReferenceServiceImplTest {

    private static final UUID SOURCE_A = UUID.randomUUID();
    private static final UUID SOURCE_B = UUID.randomUUID();
    private static final UUID TARGET = UUID.randomUUID();
    private static final UUID ACTIVE_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID REVOKED_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID ACCESS_SCOPE_ID = UUID.randomUUID();
    private static final UUID GRANT_SCOPE_ID = UUID.randomUUID();
    private static final UUID REVOKED_SCOPE_ID = UUID.randomUUID();
    private static final UUID ACTIVE_PROFILE_ID = UUID.randomUUID();
    private static final UUID DISABLED_PROFILE_ID = UUID.randomUUID();
    private static final UUID DELETED_PROFILE_ID = UUID.randomUUID();
    private static final UUID PROFILE_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID DISABLED_PROFILE_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID DELETED_PROFILE_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID OPERATOR_ID = UUID.randomUUID();

    private RoleAssignmentMapper roleAssignmentMapper;
    private AssignmentPermissionBoundaryMapper permissionBoundaryMapper;
    private AssignmentGrantBoundaryMapper grantBoundaryMapper;
    private AuthorizationScopeMapper scopeMapper;
    private ScopeRuleMapper scopeRuleMapper;
    private AuthorizationProfileMapper profileMapper;
    private AuthorizationProfileAssignmentMapper profileAssignmentMapper;
    private AuthorizationProfileBoundaryMapper profileBoundaryMapper;
    private DepartmentAuthorizationReferenceServiceImpl service;
    private List<ScopeRule> scopeRules;
    private List<AuthorizationProfileBoundary> profileBoundaries;
    private AuthorizationProfile activeProfile;
    private AuthorizationProfile disabledProfile;
    private AuthorizationProfile deletedProfile;

    @BeforeEach
    void setUp() {
        roleAssignmentMapper = mock(RoleAssignmentMapper.class);
        permissionBoundaryMapper = mock(AssignmentPermissionBoundaryMapper.class);
        grantBoundaryMapper = mock(AssignmentGrantBoundaryMapper.class);
        scopeMapper = mock(AuthorizationScopeMapper.class);
        scopeRuleMapper = mock(ScopeRuleMapper.class);
        profileMapper = mock(AuthorizationProfileMapper.class);
        profileAssignmentMapper = mock(AuthorizationProfileAssignmentMapper.class);
        profileBoundaryMapper = mock(AuthorizationProfileBoundaryMapper.class);
        service = new DepartmentAuthorizationReferenceServiceImpl(roleAssignmentMapper,
                permissionBoundaryMapper, grantBoundaryMapper, scopeMapper, scopeRuleMapper,
                profileMapper, profileAssignmentMapper, profileBoundaryMapper);

        var activeAssignment = assignment(ACTIVE_ASSIGNMENT_ID, "ACTIVE");
        var revokedAssignment = assignment(REVOKED_ASSIGNMENT_ID, "REVOKED");
        when(roleAssignmentMapper.selectList(any())).thenReturn(List.of(activeAssignment, revokedAssignment));

        var activeAccess = accessBoundary(ACTIVE_ASSIGNMENT_ID, ACCESS_SCOPE_ID);
        var revokedAccess = accessBoundary(REVOKED_ASSIGNMENT_ID, REVOKED_SCOPE_ID);
        when(permissionBoundaryMapper.selectList(any())).thenReturn(List.of(activeAccess, revokedAccess));
        var activeGrant = grantBoundary(ACTIVE_ASSIGNMENT_ID, GRANT_SCOPE_ID);
        when(grantBoundaryMapper.selectList(any())).thenReturn(List.of(activeGrant));

        var accessScope = scope(ACCESS_SCOPE_ID, "RULES");
        var grantScope = scope(GRANT_SCOPE_ID, "RULES");
        var revokedScope = scope(REVOKED_SCOPE_ID, "RULES");
        when(scopeMapper.selectBatchIds(any())).thenReturn(List.of(accessScope, grantScope, revokedScope));

        scopeRules = new ArrayList<>(List.of(
                rule(ACCESS_SCOPE_ID, SOURCE_A, true),
                rule(GRANT_SCOPE_ID, SOURCE_A, false),
                rule(GRANT_SCOPE_ID, SOURCE_B, false),
                rule(REVOKED_SCOPE_ID, SOURCE_A, true)));
        when(scopeRuleMapper.selectList(any())).thenReturn(scopeRules);
        when(scopeRuleMapper.update(any(), any())).thenReturn(1);
        when(scopeRuleMapper.deleteDuplicateDepartmentRule(any(), any())).thenReturn(1);

        activeProfile = profile(ACTIVE_PROFILE_ID, "ACTIVE", 4L, null);
        disabledProfile = profile(DISABLED_PROFILE_ID, "DISABLED", 2L, null);
        deletedProfile = profile(DELETED_PROFILE_ID, "DISABLED", 7L, java.time.Instant.now());
        when(profileMapper.selectList(any())).thenReturn(List.of(activeProfile, disabledProfile, deletedProfile));
        var activeProfileAssignment = profileAssignment(PROFILE_ASSIGNMENT_ID, ACTIVE_PROFILE_ID);
        var disabledProfileAssignment = profileAssignment(DISABLED_PROFILE_ASSIGNMENT_ID, DISABLED_PROFILE_ID);
        var deletedProfileAssignment = profileAssignment(DELETED_PROFILE_ASSIGNMENT_ID, DELETED_PROFILE_ID);
        when(profileAssignmentMapper.selectList(any())).thenReturn(
                List.of(activeProfileAssignment, disabledProfileAssignment, deletedProfileAssignment));

        var activeBoundary = profileBoundary(PROFILE_ASSIGNMENT_ID, scopeMap(
                "RULES", List.of("dept-a", "dept-a", "dept-other"), true, "active-extra"),
                scopeMap("RULES", List.of("dept-a", "dept-b"), false, "grant-extra"));
        var disabledBoundary = profileBoundary(DISABLED_PROFILE_ASSIGNMENT_ID, scopeMap(
                "RULES", List.of("dept-b", "dept-a"), true, "disabled-extra"), null);
        var deletedBoundary = profileBoundary(DELETED_PROFILE_ASSIGNMENT_ID, scopeMap(
                "RULES", List.of("dept-a"), true, "deleted-extra"), null);
        when(profileBoundaryMapper.selectList(any())).thenReturn(List.of(activeBoundary, disabledBoundary, deletedBoundary));
        when(profileBoundaryMapper.updateById(any(AuthorizationProfileBoundary.class))).thenReturn(1);
        when(profileMapper.update(any(), any())).thenReturn(1);
        profileBoundaries = new ArrayList<>(List.of(activeBoundary, disabledBoundary, deletedBoundary));
    }

    @Test
    void previewCountsActiveAssignmentsAndAllNonDeletedProfilesAndSignalsExpansion() {
        var impact = service.previewMerge(List.of(SOURCE_A, SOURCE_B),
                List.of("dept-a", "dept-b"), TARGET, "dept-target");

        assertEquals(1, impact.assignmentCount());
        assertEquals(2, impact.profileCount());
        assertEquals(1, impact.accessRuleCount());
        assertEquals(2, impact.grantRuleCount());
        assertEquals(2, impact.profileAccessScopeCount());
        assertEquals(1, impact.profileGrantScopeCount());
        assertTrue(impact.expandsEffectiveAuthority());
    }

    @Test
    void previewDoesNotFlagExpansionWhenEverySourceDepartmentIsAlreadyCovered() {
        scopeRules.getFirst().setIncludeDescendants(false);
        profileBoundaries.getFirst()
                .setAccessScope(scopeMap(
                        "RULES", List.of("dept-a", "dept-b"), false, "active-extra"));

        var impact = service.previewMerge(List.of(SOURCE_A, SOURCE_B),
                List.of("dept-a", "dept-b"), TARGET, "dept-target");

        assertFalse(impact.expandsEffectiveAuthority());
    }

    @Test
    void applyRewritesAndDeduplicatesStructuredRulesButLeavesRevokedHistoryAlone() {
        var impact = service.applyMerge(List.of(SOURCE_A, SOURCE_B),
                List.of("dept-a", "dept-b"), TARGET, "dept-target", OPERATOR_ID);

        assertEquals(1, impact.assignmentCount());
        assertEquals(1, scopeRules.stream()
                .filter(rule -> ACCESS_SCOPE_ID.equals(rule.getScopeId())
                        && TARGET.equals(rule.getDepartmentId()))
                .count());
        assertEquals(1, scopeRules.stream()
                .filter(rule -> GRANT_SCOPE_ID.equals(rule.getScopeId())
                        && TARGET.equals(rule.getDepartmentId()))
                .count());
        assertEquals(1, scopeRules.stream()
                .filter(rule -> REVOKED_SCOPE_ID.equals(rule.getScopeId())
                        && SOURCE_A.equals(rule.getDepartmentId()))
                .count());
        verify(scopeRuleMapper, times(2)).update(any(), any());
        verify(scopeRuleMapper).deleteDuplicateDepartmentRule(scopeRules.get(2).getId(), 0L);
    }

    @Test
    void applyPreservesUnrelatedJsonFieldsAndUpdatesDisabledProfileOnce() {
        service.applyMerge(List.of(SOURCE_A, SOURCE_B), List.of("dept-a", "dept-b"), TARGET,
                "dept-target", OPERATOR_ID);

        var active = profileBoundaries.getFirst();
        assertEquals(List.of("dept-target", "dept-other"), active.getAccessScope().get("department_codes"));
        assertEquals("active-extra", active.getAccessScope().get("extra"));
        assertEquals(List.of("dept-target"), active.getGrantScope().get("department_codes"));
        var disabled = profileBoundaries.get(1);
        assertEquals(List.of("dept-target"), disabled.getAccessScope().get("department_codes"));
        assertEquals("disabled-extra", disabled.getAccessScope().get("extra"));
        verify(profileBoundaryMapper).updateById(active);
        verify(profileBoundaryMapper).updateById(disabled);
        verify(profileBoundaryMapper, times(2)).updateById(any(AuthorizationProfileBoundary.class));
        verify(profileMapper, times(2)).update(org.mockito.ArgumentMatchers.isNull(), any());
        assertEquals(5L, activeProfile.getVersion());
        assertEquals(3L, disabledProfile.getVersion());
        assertEquals(List.of("dept-a"), profileBoundaries.get(2).getAccessScope().get("department_codes"));
    }

    private static RoleAssignment assignment(UUID id, String state) {
        var assignment = new RoleAssignment();
        assignment.setId(id);
        assignment.setState(state);
        return assignment;
    }

    private static AssignmentPermissionBoundary accessBoundary(UUID assignmentId, UUID scopeId) {
        var boundary = new AssignmentPermissionBoundary();
        boundary.setAssignmentId(assignmentId);
        boundary.setScopeId(scopeId);
        return boundary;
    }

    private static AssignmentGrantBoundary grantBoundary(UUID assignmentId, UUID scopeId) {
        var boundary = new AssignmentGrantBoundary();
        boundary.setAssignmentId(assignmentId);
        boundary.setScopeId(scopeId);
        return boundary;
    }

    private static AuthorizationScope scope(UUID id, String mode) {
        var scope = new AuthorizationScope();
        scope.setId(id);
        scope.setScopeMode(mode);
        return scope;
    }

    private static ScopeRule rule(UUID scopeId, UUID departmentId, boolean includeDescendants) {
        var rule = new ScopeRule();
        rule.setId(UUID.randomUUID());
        rule.setScopeId(scopeId);
        rule.setRuleType("DEPARTMENT");
        rule.setDepartmentId(departmentId);
        rule.setIncludeDescendants(includeDescendants);
        rule.setVersion(0L);
        rule.setRulePayload(Map.of("retain", "value"));
        return rule;
    }

    private static AuthorizationProfile profile(UUID id, String state, long version, java.time.Instant deleted) {
        var profile = new AuthorizationProfile();
        profile.setId(id);
        profile.setState(state);
        profile.setVersion(version);
        profile.setDeleted(deleted);
        return profile;
    }

    private static AuthorizationProfileAssignment profileAssignment(UUID id, UUID profileId) {
        var assignment = new AuthorizationProfileAssignment();
        assignment.setId(id);
        assignment.setProfileId(profileId);
        return assignment;
    }

    private static AuthorizationProfileBoundary profileBoundary(UUID assignmentId,
                                                                Map<String, Object> access,
                                                                Map<String, Object> grant) {
        var boundary = new AuthorizationProfileBoundary();
        boundary.setProfileAssignmentId(assignmentId);
        boundary.setAccessScope(access);
        boundary.setGrantScope(grant);
        return boundary;
    }

    private static Map<String, Object> scopeMap(String mode, List<String> codes,
                                                boolean includeDescendants, String extra) {
        var map = new LinkedHashMap<String, Object>();
        map.put("mode", mode);
        map.put("department_codes", new ArrayList<>(codes));
        map.put("include_descendants", includeDescendants);
        map.put("extra", extra);
        return map;
    }
}
