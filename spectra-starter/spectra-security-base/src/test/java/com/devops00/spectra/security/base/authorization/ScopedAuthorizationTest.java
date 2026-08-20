/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.security.base.authorization;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopedAuthorizationTest {

    private static final UUID SUBJECT = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");
    private static final UUID DEPARTMENT = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae4");
    private static final String READ = "oa:meeting:read";
    private static final String UPDATE = "oa:meeting:update";

    @Test
    void accessBoundaryIsPermissionSpecific() {
        var read = new PermissionBoundary(READ, new AuthorizationScope(ScopeMode.RULES, Set.of(DEPARTMENT), false));
        var update = new PermissionBoundary(UPDATE, AuthorizationScope.of(ScopeMode.SELF));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "ROLE_MANAGER",
                1, Map.of(READ, read, UPDATE, update), Map.of());
        var authorization = new ScopedAuthorization(SUBJECT, AuthorizationSnapshot.of(List.of(assignment)));

        var departmentQuery = new ScopeQuery(SUBJECT, UUID.randomUUID(), DEPARTMENT, Set.of(DEPARTMENT));
        var otherDepartmentQuery = new ScopeQuery(SUBJECT, UUID.randomUUID(), UUID.randomUUID(), Set.of());
        assertTrue(authorization.allows(ExecutionContext.of(SUBJECT, "oa:meeting", ResourceOperation.LIST), departmentQuery));
        assertFalse(authorization.allows(ExecutionContext.of(SUBJECT, "oa:meeting", ResourceOperation.UPDATE), departmentQuery));
        assertFalse(authorization.allows(ExecutionContext.of(SUBJECT, "oa:meeting", ResourceOperation.LIST), otherDepartmentQuery));
    }

    @Test
    void missingPermissionFailsClosed() {
        var authorization = new ScopedAuthorization(SUBJECT, AuthorizationSnapshot.of(List.of()));
        var query = new ScopeQuery(SUBJECT, SUBJECT, null, Set.of());
        assertFalse(authorization.allows(ExecutionContext.of(SUBJECT, "oa:document", ResourceOperation.EXPORT), query));
    }
}
