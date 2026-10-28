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
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.security.authorization.AuthorizationGrantRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 组织变更操作的最终授权边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@Component
@RequiredArgsConstructor
public class OrganizationChangeAuthorization {

    private final AuthorizationSnapshotLoader snapshotLoader;

    private final GrantBoundaryService grantBoundaryService;

    private final SecurityContextAccessor securityContextAccessor;

    private final ObjectProvider<RootAuthorizationPolicy> rootPolicyProvider;

    public void evaluate(UUID operatorId, List<String> permissions, Collection<UUID> departmentIds) {
        var rootPolicy = rootPolicyProvider.getIfAvailable();
        boolean root = rootPolicy != null && rootPolicy.isRoot(securityContextAccessor.currentUser());
        var scope = scope(departmentIds);
        var requests = permissions.stream()
                .map(permission -> new AuthorizationGrantRequest(permission, scope, scope, 1))
                .toList();
        grantBoundaryService.evaluate(snapshotLoader.load(operatorId), operatorId, null, requests, root);
    }

    private static AuthorizationScope scope(Collection<UUID> departmentIds) {
        var ids = departmentIds == null
                ? new LinkedHashSet<UUID>()
                : departmentIds.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return ids.isEmpty()
                ? AuthorizationScope.of(ScopeMode.ALL)
                : new AuthorizationScope(ScopeMode.RULES, ids, false);
    }
}
