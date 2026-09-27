package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeSqlPolicyTest {

    private static final DataScope DATA_SCOPE = ScopedRecord.class.getAnnotation(DataScope.class);
    private static final UUID BOUNDARY = UUID.randomUUID();
    private static final UUID MEMBERSHIP = UUID.randomUUID();

    @Test
    void rulesRequireBothBoundaryAndMembershipDepartment() {
        var expression = ScopeSqlPolicy.build(new Table("record"), DATA_SCOPE,
                List.of(boundary(Set.of(BOUNDARY), false)), null, Set.of(MEMBERSHIP));

        assertNotNull(expression);
        var sql = expression.toString();
        assertTrue(sql.contains(BOUNDARY.toString()));
        assertTrue(sql.contains(MEMBERSHIP.toString()));
        assertTrue(sql.contains(" AND "));
    }

    @Test
    void descendantModeExpandsBothDepartmentSets() {
        var expression = ScopeSqlPolicy.build(new Table("record"), DATA_SCOPE,
                List.of(boundary(Set.of(BOUNDARY), true)), null, Set.of(MEMBERSHIP));

        assertNotNull(expression);
        var sql = expression.toString();
        assertTrue(sql.contains(BOUNDARY.toString()));
        assertTrue(sql.contains(MEMBERSHIP.toString()));
        assertEquals(2, occurrences(sql, "descendant_id"));
    }

    @Test
    void emptyMembershipDeniesStructuralPredicate() {
        var expression = ScopeSqlPolicy.build(new Table("record"), DATA_SCOPE,
                List.of(boundary(Set.of(BOUNDARY), false)), null, Set.of());

        assertNotNull(expression);
        assertTrue(expression.toString().contains("1 = 0"));
    }

    @Test
    void relationshipDepartmentBranchRetainsExistingScopeWithoutMembershipIntersection() {
        var expression = ScopeSqlPolicy.build(new Table("record"), RELATION_DATA_SCOPE,
                List.of(boundary(Set.of(BOUNDARY), false)), UUID.randomUUID(), Set.of(MEMBERSHIP));

        assertNotNull(expression);
        var sql = expression.toString();
        assertTrue(sql.contains("oa_meeting_participant"));
        assertEquals(1, occurrences(sql, MEMBERSHIP.toString()));
        assertTrue(occurrences(sql, BOUNDARY.toString()) >= 2);
    }

    @Test
    void allScopeIgnoresMembership() {
        var expression = ScopeSqlPolicy.build(new Table("record"), DATA_SCOPE,
                List.of(boundary(Set.of(), false, ScopeMode.ALL)), null, Set.of());

        assertNull(expression);
    }

    @Test
    void noneScopeDeniesAllRows() {
        var expression = ScopeSqlPolicy.build(new Table("record"), DATA_SCOPE,
                List.of(boundary(Set.of(), false, ScopeMode.NONE)), null, Set.of(MEMBERSHIP));

        assertNotNull(expression);
        assertEquals("1 = 0", expression.toString());
    }

    private static PermissionBoundary boundary(Set<UUID> departments, boolean descendants) {
        return boundary(departments, descendants, ScopeMode.RULES);
    }

    private static PermissionBoundary boundary(Set<UUID> departments, boolean descendants, ScopeMode mode) {
        return new PermissionBoundary("record:read", new AuthorizationScope(mode, departments, descendants));
    }

    private static int occurrences(String source, String target) {
        var count = 0;
        var from = 0;
        while ((from = source.indexOf(target, from)) >= 0) {
            count++;
            from += target.length();
        }
        return count;
    }

    @DataScope(readPermission = "record:read", column = "department_id")
    private static class ScopedRecord {
    }

    @DataScope(readPermission = "record:read", column = "department_id", relations = {
            @DataScope.Relation(schema = "spectra_oa", table = "oa_meeting_participant", joinColumn = "meeting_id", departmentColumn = "department_id")})
    private static class RelationScopedRecord {
    }

    private static final DataScope RELATION_DATA_SCOPE = RelationScopedRecord.class.getAnnotation(DataScope.class);
}
