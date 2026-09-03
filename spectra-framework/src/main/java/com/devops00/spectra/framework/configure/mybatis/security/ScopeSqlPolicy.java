/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.configure.mybatis.security;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 将 Permission-specific Access Boundary 编译为 SQL 谓词。
 *
 * <p>该类不接受客户端传入的 scope，也不读取 SecurityUser 中的旧全局 scope 快照。
 * 缺少 Permission、Boundary 或资源列时一律生成 {@code 1 = 0} 或抛出登记错误。</p>
 */
public final class ScopeSqlPolicy {

    private static final String CLOSURE_SCHEMA = "spectra_core";
    private static final String CLOSURE_TABLE = "sys_department_closure";

    private ScopeSqlPolicy() {
    }

    /**
     * 根据 MyBatis statement 名称选择本次 SQL 所需的 Permission。
     */
    public static String permissionFor(DataScope annotation, String mappedStatementId) {
        if (annotation == null) {
            throw new IllegalArgumentException("受保护 SQL 缺少 DataScope 策略");
        }
        String method = mappedStatementId == null ? "" : mappedStatementId.substring(mappedStatementId.lastIndexOf('.') + 1).toLowerCase();
        if (method.contains("insert") || method.contains("create") || method.contains("add")) {
            return null;
        }
        if (method.contains("export")) {
            return required(annotation.exportPermission(), "exportPermission");
        }
        if ((method.contains("delete")
                || method.contains("remove")
                || method.contains("update")
                || method.contains("modify")
                || method.contains("batch")) && !method.contains("select")) {
            return required(annotation.writePermission(), "writePermission");
        }
        return required(annotation.readPermission(), "readPermission");
    }

    /**
     * 将同一 Permission 的多个 Assignment Boundary 以 OR 合并；不同 Permission 不会进入此方法。
     */
    public static Expression build(Table table, DataScope annotation, List<PermissionBoundary> boundaries,
                                   UUID subjectId) {
        if (table == null || annotation == null || boundaries == null || boundaries.isEmpty()) {
            return falsePredicate();
        }
        Expression combined = null;
        for (PermissionBoundary boundary : boundaries) {
            Expression current = buildBoundary(table, annotation, boundary.scope(), subjectId);
            if (current == null) {
                return null;
            }
            combined = combined == null ? current : new OrExpression(combined, current);
        }
        return combined == null ? falsePredicate() : combined;
    }

    /**
     * 创建或构建目标数据（{@code buildBoundary}）。
     */
    private static Expression buildBoundary(Table table, DataScope annotation, AuthorizationScope scope, UUID subjectId) {
        if (scope.mode() == ScopeMode.NONE || scope.mode() == ScopeMode.ALL) {
            return null;
        }

        Expression structural = structuralPredicate(table, annotation, scope, subjectId);
        Expression relations = relationPredicates(table, annotation, scope, subjectId);
        return combineWithOr(structural, relations);
    }

    /**
     * 处理内部业务逻辑（{@code structuralPredicate}）。
     */
    private static Expression structuralPredicate(Table table, DataScope annotation, AuthorizationScope scope,
                                                  UUID subjectId) {
        if (scope.mode() == ScopeMode.SELF && subjectId != null && !annotation.ownerColumn().isBlank()) {
            return new EqualsTo(new Column(table, annotation.ownerColumn()), new StringValue(subjectId.toString()));
        }
        if (scope.mode() == ScopeMode.RULES && !annotation.column().isBlank()) {
            return departmentPredicate(table, annotation.column(), scope);
        }
        return null;
    }

    /**
     * 处理内部业务逻辑（{@code relationPredicates}）。
     */
    private static Expression relationPredicates(Table table, DataScope annotation, AuthorizationScope scope,
                                                 UUID subjectId) {
        Expression relations = null;
        for (DataScope.Relation relation : annotation.relations()) {
            relations = combineWithOr(relations, relationPredicate(table, relation, scope, subjectId));
        }
        return relations;
    }

    /**
     * 处理内部业务逻辑（{@code relationPredicate}）。
     */
    private static Expression relationPredicate(Table table, DataScope.Relation relation, AuthorizationScope scope,
                                                UUID subjectId) {
        Expression department = null;
        if (scope.mode() == ScopeMode.RULES && !relation.departmentColumn().isBlank()) {
            department = relationSubquery(table, relation,
                    departmentPredicate(new Table(relation.schema(), relation.table()), relation.departmentColumn(), scope));
        }
        Expression user = null;
        if (subjectId != null && !relation.userColumn().isBlank()) {
            user = relationSubquery(table, relation,
                    new EqualsTo(new Column(new Table(relation.schema(), relation.table()), relation.userColumn()),
                            new StringValue(subjectId.toString())));
        }
        return combineWithAnd(department, user);
    }

    /**
     * 处理内部业务逻辑（{@code combineWithAnd}）。
     */
    private static Expression combineWithAnd(Expression left, Expression right) {
        if (left == null) {
            return right;
        }
        return right == null ? left : new AndExpression(left, right);
    }

    /**
     * 处理内部业务逻辑（{@code combineWithOr}）。
     */
    private static Expression combineWithOr(Expression left, Expression right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return new OrExpression(left, right);
    }

    /**
     * 处理内部业务逻辑（{@code departmentPredicate}）。
     */
    private static Expression departmentPredicate(Table table, String column, AuthorizationScope scope) {
        if (scope.departmentIds().isEmpty()) {
            return falsePredicate();
        }
        Column left = new Column(table, column);
        ExpressionList<Expression> ids = new ExpressionList<>(scope.departmentIds()
                .stream()
                .map(id -> (Expression) new StringValue(id.toString()))
                .collect(Collectors.toList()));
        InExpression direct = new InExpression(left, ids);
        if (!scope.includeDescendants()) {
            return direct;
        }

        Table closure = new Table(CLOSURE_SCHEMA, CLOSURE_TABLE);
        PlainSelect select = new PlainSelect();
        select.addSelectItems(new Column(closure, "descendant_id"));
        select.setFromItem(closure);
        select.setWhere(new InExpression(new Column(closure, "ancestor_id"), ids));
        ParenthesedSelect descendants = new ParenthesedSelect();
        descendants.setSelect(select);
        return new InExpression(left, descendants);
    }

    /**
     * 处理内部业务逻辑（{@code relationSubquery}）。
     */
    private static Expression relationSubquery(Table ignored, DataScope.Relation relation, Expression where) {
        Table relationTable = new Table(relation.schema(), relation.table());
        PlainSelect select = new PlainSelect();
        select.addSelectItems(new Column(relationTable, relation.joinColumn()));
        select.setFromItem(relationTable);
        select.setWhere(where);
        ParenthesedSelect subquery = new ParenthesedSelect();
        subquery.setSelect(select);
        return new InExpression(new Column(relation.mainColumn()), subquery);
    }

    /**
     * 处理内部业务逻辑（{@code falsePredicate}）。
     */
    private static Expression falsePredicate() {
        return new EqualsTo(new LongValue(1), new LongValue(0));
    }

    /**
     * 校验并确保数据满足当前约束（{@code required}）。
     */
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("DataScope 必须显式登记 " + field);
        }
        return value;
    }
}
