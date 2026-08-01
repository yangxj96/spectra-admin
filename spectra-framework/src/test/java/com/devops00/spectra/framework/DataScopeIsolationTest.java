/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.framework.configure.mybatis.interceptor.DataScopeInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// 数据隔离核心回归测试：上下文作用域、字段别名、空范围 fail-closed、关系 schema。
class DataScopeIsolationTest {

    private static final UUID USER_ID = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");

    @AfterEach
    void clearContext() {
        DataScopeContextHolder.endRequest();
    }

    @Test
    void requestContextCachesOnlyInsideRequestAndBypassIsScoped() {
        var scope = new DataScopeProvider.EffectiveScope(DataScopeType.DEPT, USER_ID, List.of(USER_ID));

        DataScopeContextHolder.beginRequest();
        DataScopeContextHolder.setScope(USER_ID, scope);
        assertEquals(scope, DataScopeContextHolder.getScope(USER_ID));
        assertFalse(DataScopeContextHolder.isBypassed());

        DataScopeContextHolder.withBypass(() -> assertTrue(DataScopeContextHolder.isBypassed()));
        assertFalse(DataScopeContextHolder.isBypassed());

        DataScopeContextHolder.endRequest();
        assertEquals(null, DataScopeContextHolder.getScope(USER_ID));
    }

    @Test
    void selfScopeUsesAnnotationOwnerColumnAndKeepsAlias() throws Exception {
        var table = new Table("oa_meeting");
        table.setAlias(new net.sf.jsqlparser.expression.Alias("m"));
        Expression expression = invokeStructural(table, TestResource.class.getAnnotation(DataScope.class),
                new DataScopeProvider.EffectiveScope(DataScopeType.SELF, null, List.of()));

        assertNotNull(expression);
        assertTrue(expression.toString().contains("m.owner_id"));
        assertTrue(expression.toString().contains(USER_ID.toString()));
    }

    @Test
    void emptyCustomScopeProducesFalsePredicate() throws Exception {
        Expression expression = invokeStructural(new Table("oa_meeting"),
                TestResource.class.getAnnotation(DataScope.class),
                new DataScopeProvider.EffectiveScope(DataScopeType.CUSTOM, null, List.of()));

        assertEquals("1 = 0", expression.toString());
    }

    @Test
    void relationUsesExplicitSchema() throws Exception {
        Method method = DataScopeInnerInterceptor.class.getDeclaredMethod(
                "buildRelationalExpression", Table.class, DataScope.class, UUID.class);
        method.setAccessible(true);
        Expression expression = (Expression) method.invoke(new DataScopeInnerInterceptor(null, null),
                new Table("oa_meeting"), TestResource.class.getAnnotation(DataScope.class), USER_ID);

        assertNotNull(expression);
        assertTrue(expression.toString().contains("spectra_oa.oa_meeting_participant"));
    }

    private Expression invokeStructural(Table table, DataScope annotation,
                                         DataScopeProvider.EffectiveScope scope) throws Exception {
        Method method = DataScopeInnerInterceptor.class.getDeclaredMethod(
                "buildStructuralExpression", Table.class, DataScope.class, String.class,
                DataScopeProvider.EffectiveScope.class, UUID.class);
        method.setAccessible(true);
        return (Expression) method.invoke(new DataScopeInnerInterceptor(null, null), table, annotation,
                annotation.column(), scope, USER_ID);
    }

    @DataScope(
            column = "department_id",
            ownerColumn = "owner_id",
            relations = @DataScope.Relation(
                    schema = "spectra_oa",
                    table = "oa_meeting_participant",
                    joinColumn = "meeting_id"
            )
    )
    private static class TestResource {
    }
}
