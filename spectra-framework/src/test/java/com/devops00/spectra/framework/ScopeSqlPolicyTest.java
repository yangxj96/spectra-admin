/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.framework.configure.mybatis.security.ScopeSqlPolicy;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.PermissionBoundary;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeSqlPolicyTest {

    private static final UUID SUBJECT = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");
    private static final UUID DEPARTMENT = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae4");

    @Test
    void compilesPermissionBoundaryWithAliasAndDepartmentClosure() {
        DataScope annotation = Resource.class.getAnnotation(DataScope.class);
        var boundary = new PermissionBoundary("oa:meeting:read",
                new AuthorizationScope(ScopeMode.RULES, Set.of(DEPARTMENT), true));
        String sql = ScopeSqlPolicy.build(new Table("oa_meeting"), annotation, List.of(boundary), SUBJECT).toString();

        assertTrue(sql.contains("department_id IN"));
        assertTrue(sql.contains("spectra_core.sys_department_closure"));
        assertTrue(sql.contains("oa_meeting_participant"));
    }

    @Test
    void compilesSelfBoundaryWithOwnerColumn() {
        DataScope annotation = SelfResource.class.getAnnotation(DataScope.class);
        var boundary = new PermissionBoundary("oa:calendar:read", AuthorizationScope.of(ScopeMode.SELF));
        String sql = ScopeSqlPolicy.build(new Table("oa_calendar"), annotation, List.of(boundary), SUBJECT).toString();
        assertTrue(sql.contains("owner_id"));
        assertTrue(sql.contains(SUBJECT.toString()));
    }

    @Test
    void writeAndExportRequireSeparateRegisteredPermissions() {
        DataScope annotation = Resource.class.getAnnotation(DataScope.class);
        assertEquals("oa:meeting:read", ScopeSqlPolicy.permissionFor(annotation, "MeetingMapper.selectPage"));
        assertEquals("oa:meeting:update", ScopeSqlPolicy.permissionFor(annotation, "MeetingMapper.updateById"));
        assertThrows(IllegalStateException.class, () -> ScopeSqlPolicy.permissionFor(annotation, "MeetingMapper.export"));
    }

    @DataScope(readPermission = "oa:meeting:read", writePermission = "oa:meeting:update", column = "department_id", relations = @DataScope.Relation(schema = "spectra_oa", table = "oa_meeting_participant", joinColumn = "meeting_id"))
    private static class Resource {
    }

    @DataScope(readPermission = "oa:calendar:read", writePermission = "oa:calendar:update", ownerColumn = "owner_id")
    private static class SelfResource {
    }
}
