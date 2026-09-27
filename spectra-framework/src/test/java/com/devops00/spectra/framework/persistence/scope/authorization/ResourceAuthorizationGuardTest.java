package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.ExecutionContext;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ResourceOperation;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import com.devops00.spectra.common.security.authorization.ScopedAuthorization;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAuthorizationGuardTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID RECORD = UUID.randomUUID();
    private static final UUID BOUNDARY = UUID.randomUUID();
    private static final UUID MEMBERSHIP = UUID.randomUUID();
    private static final ExecutionContext CONTEXT = ExecutionContext.of(USER, "record", ResourceOperation.DETAIL);

    @Test
    void resourceGuardMatchesSqlMembershipIntersection() {
        var authorization = scoped(ScopeMode.RULES, Set.of(BOUNDARY), false, Set.of(BOUNDARY));

        assertDoesNotThrow(() -> ResourceAuthorizationGuard.assertAllowed(authorization, CONTEXT,
                query(RECORD, BOUNDARY, Set.of())));
        var denied = scoped(ScopeMode.RULES, Set.of(BOUNDARY), false, Set.of(MEMBERSHIP));
        assertThrows(DataScopeViolationException.class, () -> ResourceAuthorizationGuard.assertAllowed(denied,
                CONTEXT, query(RECORD, BOUNDARY, Set.of())));
    }

    @Test
    void descendantModeExpandsBoundaryAndMembershipSets() {
        var authorization = scoped(ScopeMode.RULES, Set.of(BOUNDARY), true, Set.of(MEMBERSHIP));
        var descendants = query(RECORD, UUID.randomUUID(), Set.of(BOUNDARY, MEMBERSHIP));

        assertDoesNotThrow(() -> ResourceAuthorizationGuard.assertAllowed(authorization, CONTEXT, descendants));
    }

    @Test
    void allScopeIgnoresMembership() {
        var authorization = scoped(ScopeMode.ALL, Set.of(), false, Set.of());

        assertDoesNotThrow(() -> ResourceAuthorizationGuard.assertAllowed(authorization, CONTEXT,
                query(RECORD, UUID.randomUUID(), Set.of())));
    }

    @Test
    void noneScopeDeniesResourceAccess() {
        var authorization = scoped(ScopeMode.NONE, Set.of(), false, Set.of());

        assertThrows(DataScopeViolationException.class, () -> ResourceAuthorizationGuard.assertAllowed(authorization,
                CONTEXT, query(RECORD, UUID.randomUUID(), Set.of())));
    }

    @Test
    void selfScopeUsesOwner() {
        var authorization = scoped(ScopeMode.SELF, Set.of(), false, Set.of());

        assertDoesNotThrow(() -> ResourceAuthorizationGuard.assertAllowed(authorization, CONTEXT,
                new ScopeQuery(USER, USER, null, Set.of())));
        assertThrows(DataScopeViolationException.class, () -> ResourceAuthorizationGuard.assertAllowed(authorization,
                CONTEXT, new ScopeQuery(USER, UUID.randomUUID(), null, Set.of())));
    }

    @Test
    void boundariesNeverCrossPermissions() {
        var snapshot = snapshot(ScopeMode.RULES, Set.of(BOUNDARY), false, Set.of(MEMBERSHIP));

        assertTrue(snapshot.accessBoundaries("other:read").isEmpty());
        assertTrue(!snapshot.canAccess("other:read", query(RECORD, MEMBERSHIP, Set.of())));
    }

    @Test
    void emptyMembershipDeniesRules() {
        var authorization = scoped(ScopeMode.RULES, Set.of(BOUNDARY), false, Set.of());

        assertThrows(DataScopeViolationException.class, () -> ResourceAuthorizationGuard.assertAllowed(authorization,
                CONTEXT, query(RECORD, BOUNDARY, Set.of())));
    }

    private static ScopedAuthorization scoped(ScopeMode mode, Set<UUID> departments, boolean descendants,
                                              Set<UUID> memberships) {
        return new ScopedAuthorization(USER, snapshot(mode, departments, descendants, memberships));
    }

    private static AuthorizationSnapshot snapshot(ScopeMode mode, Set<UUID> departments, boolean descendants,
                                                  Set<UUID> memberships) {
        var boundary = new PermissionBoundary("record:read", new AuthorizationScope(mode, departments, descendants));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "TEST", 1,
                Map.of("record:read", boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment), memberships);
    }

    private static ScopeQuery query(UUID subject, UUID department, Set<UUID> lineage) {
        return new ScopeQuery(subject, null, department, lineage);
    }
}
