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
package com.devops00.spectra.core.security.authorization.service;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.security.authorization.AuthorizationGrantRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrganizationChangeAuthorizationTest {

    private final AuthorizationSnapshotLoader snapshotLoader = mock(AuthorizationSnapshotLoader.class);
    private final GrantBoundaryService grantBoundaryService = mock(GrantBoundaryService.class);
    private final SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
    private final ObjectProvider<com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy> rootPolicyProvider = mock(
            ObjectProvider.class);

    @Test
    void departmentChangeRequestsUseSourceAndTargetScope() {
        var operatorId = UUID.randomUUID();
        var sourceId = UUID.randomUUID();
        var parentId = UUID.randomUUID();

        service().evaluate(operatorId, List.of("department:update"), List.of(sourceId, parentId));

        var captor = ArgumentCaptor.forClass(List.class);
        verify(grantBoundaryService).evaluate(any(), eq(operatorId), isNull(), captor.capture(), anyBoolean());
        var request = (AuthorizationGrantRequest) captor.getValue().getFirst();
        assertEquals("department:update", request.permission());
        assertEquals(ScopeMode.RULES, request.accessScope().mode());
        assertEquals(java.util.Set.of(sourceId, parentId), request.accessScope().departmentIds());
    }

    @Test
    void topLevelCreateRequiresAllScope() {
        var operatorId = UUID.randomUUID();

        service().evaluate(operatorId, List.of("department:create"), List.of());

        var captor = ArgumentCaptor.forClass(List.class);
        verify(grantBoundaryService).evaluate(any(), eq(operatorId), isNull(), captor.capture(), anyBoolean());
        var request = (AuthorizationGrantRequest) captor.getValue().getFirst();
        assertEquals(ScopeMode.ALL, request.accessScope().mode());
        assertEquals(AuthorizationScope.of(ScopeMode.ALL), request.accessScope());
    }

    private OrganizationChangeAuthorization service() {
        return new OrganizationChangeAuthorization(snapshotLoader, grantBoundaryService, securityContextAccessor,
                rootPolicyProvider);
    }
}
