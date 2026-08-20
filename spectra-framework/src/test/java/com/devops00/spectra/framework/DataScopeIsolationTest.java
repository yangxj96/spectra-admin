/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.framework.configure.mybatis.security.ScopeSqlPolicy;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.PermissionBoundary;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据隔离核心回归测试：绕过上下文、Permission-specific SQL、空边界 fail-closed、关系 schema。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
class DataScopeIsolationTest {

    private static final UUID USER_ID = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");
    private static final String READ_PERMISSION = "oa:meeting:read";

    @AfterEach
    void clearContext() {
        DataScopeContextHolder.endRequest();
    }

    @Test
    void requestContextOnlyTracksScopedBypass() {
        DataScopeContextHolder.beginRequest();
        assertFalse(DataScopeContextHolder.isBypassed());

        DataScopeContextHolder.withBypass(() -> assertTrue(DataScopeContextHolder.isBypassed()));
        assertFalse(DataScopeContextHolder.isBypassed());

        DataScopeContextHolder.endRequest();
        assertFalse(DataScopeContextHolder.isBypassed());
    }

    @Test
    void selfScopeUsesAnnotationOwnerColumnAndKeepsAlias() {
        var table = new Table("oa_meeting");
        table.setAlias(new net.sf.jsqlparser.expression.Alias("m"));
        Expression expression = build(table, AuthorizationScope.of(ScopeMode.SELF));

        assertNotNull(expression);
        assertTrue(expression.toString().contains("m.owner_id"));
        assertTrue(expression.toString().contains(USER_ID.toString()));
    }

    @Test
    void missingPermissionBoundaryProducesFalsePredicate() {
        Expression expression = ScopeSqlPolicy.build(new Table("oa_meeting"), resource(), List.of(), USER_ID);

        assertEquals("1 = 0", expression.toString());
    }

    @Test
    void allBoundaryDoesNotAddPredicate() {
        assertNull(build(new Table("oa_meeting"), AuthorizationScope.of(ScopeMode.ALL)));
    }

    @Test
    void relationUsesExplicitSchema() {
        Expression expression = build(new Table("oa_meeting"), AuthorizationScope.of(ScopeMode.SELF));

        assertNotNull(expression);
        assertTrue(expression.toString().contains("spectra_oa.oa_meeting_participant"));
    }

    private Expression build(Table table, AuthorizationScope scope) {
        return ScopeSqlPolicy.build(table, resource(),
                List.of(new PermissionBoundary(READ_PERMISSION, scope)), USER_ID);
    }

    private DataScope resource() {
        return TestResource.class.getAnnotation(DataScope.class);
    }

    @DataScope(readPermission = READ_PERMISSION, column = "department_id", ownerColumn = "owner_id", relations = @DataScope.Relation(schema = "spectra_oa", table = "oa_meeting_participant", joinColumn = "meeting_id"))
    private static class TestResource {
    }
}
