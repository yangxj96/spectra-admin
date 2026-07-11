/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.configure.mybatis.interceptor;


import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/// MP执行的单表SQL拦截处理 — 自动注入数据范围 WHERE 条件
///
/// <h3>二维过滤</h3>
/// <ul>
///   <li><b>结构维度</b>：基于 department_id / created_by 等归属字段，根据用户数据范围类型过滤</li>
///   <li><b>关系维度</b>：基于 {@link DataScope#relations()} 声明的多对多关联表</li>
/// </ul>
///
/// <h3>跳过规则</h3>
/// <ul>
///   <li>{@link DataScope#ignore()} = true 的表不过滤</li>
///   <li>GLOBAL 范围的用户不过滤</li>
///   <li>未登录用户不过滤</li>
/// </ul>
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
public class DataScopeInnerInterceptor implements MultiDataPermissionHandler {

    private final DataScopeProvider dataScopeProvider;

    public DataScopeInnerInterceptor(DataScopeProvider dataScopeProvider) {
        this.dataScopeProvider = dataScopeProvider;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        UUID userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }

        // 解析表名与实体类
        String tableName = table.getName();
        if (tableName == null) {
            return null;
        }

        // 尝试获取实体类上的 @DataScope 注解
        Class<?> entityClass = resolveEntityClass(mappedStatementId);
        if (entityClass == null) {
            return null;
        }

        DataScope annotation = entityClass.getAnnotation(DataScope.class);
        if (annotation == null || annotation.ignore()) {
            return null;
        }

        // 解析用户有效数据范围
        DataScopeProvider.EffectiveScope scope = dataScopeProvider.resolve(userId);
        if (scope.getScopeType() == DataScopeType.GLOBAL) {
            return null;
        }

        // 构建 SQL WHERE 条件
        Expression scopeExpression = buildScopeExpression(table, annotation, scope);
        if (scopeExpression == null) {
            return null;
        }

        if (where == null) {
            return scopeExpression;
        }
        return new AndExpression(where, scopeExpression);
    }

    /// 构建数据范围 SQL 表达式
    private Expression buildScopeExpression(Table table, DataScope annotation, DataScopeProvider.EffectiveScope scope) {
        String columnName = annotation != null ? annotation.column() : "department_id";
        UUID currentUserId = SecUtil.getCurrentUserId();

        Expression structuralExpr = buildStructuralExpression(table, columnName, scope, currentUserId);

        // 关系维度
        Expression relationalExpr = buildRelationalExpression(table, annotation, currentUserId);

        if (structuralExpr == null && relationalExpr == null) {
            return null;
        }
        if (structuralExpr == null) {
            return relationalExpr;
        }
        if (relationalExpr == null) {
            return structuralExpr;
        }

        return new OrExpression(structuralExpr, relationalExpr);
    }

    /// 构建结构维度条件（department_id / created_by）
    private Expression buildStructuralExpression(Table table, String columnName, DataScopeProvider.EffectiveScope scope, UUID currentUserId) {
        return switch (scope.getScopeType()) {
            case SELF -> {
                // created_by = currentUserId
                EqualsTo eq = new EqualsTo();
                eq.setLeftExpression(new Column(new Table(table.getName()), "created_by"));
                eq.setRightExpression(new StringValue(currentUserId.toString()));
                yield eq;
            }
            case DEPT, DEPT_AND_CHILDREN, CUSTOM -> {
                var targetIds = scope.getTargetIds();
                if (targetIds == null || targetIds.isEmpty()) {
                    yield null;
                }
                InExpression in = new InExpression();
                in.setLeftExpression(new Column(new Table(table.getName()), columnName));
                if (targetIds.size() == 1) {
                    // 单个值用 EqualsTo 更高效
                    EqualsTo eq = new EqualsTo();
                    eq.setLeftExpression(new Column(new Table(table.getName()), columnName));
                    eq.setRightExpression(new StringValue(targetIds.get(0).toString()));
                    yield eq;
                }
                ExpressionList<Expression> exprList = new ExpressionList<>(
                        targetIds.stream()
                                .map(id -> new StringValue(id.toString()))
                                .collect(Collectors.toList())
                );
                in.setRightExpression(exprList);
                yield in;
            }
            case GLOBAL -> null;
        };
    }

    /// 构建关系维度条件（关联表子查询）
    private Expression buildRelationalExpression(Table table, DataScope annotation, UUID currentUserId) {
        if (annotation == null || annotation.relations().length == 0) {
            return null;
        }

        if (currentUserId == null) {
            return null;
        }

        Expression combined = null;
        for (DataScope.Relation relation : annotation.relations()) {
            // id IN (SELECT joinColumn FROM relationTable WHERE userColumn = ?)
            InExpression in = new InExpression();
            in.setLeftExpression(new Column(table, relation.mainColumn()));

            // 构建子查询
            var plainSelect = new net.sf.jsqlparser.statement.select.PlainSelect();

            // SELECT joinColumn FROM relationTable
            plainSelect.addSelectItems(new Column(new Table(relation.table()), relation.joinColumn()));

            Table relTable = new Table(relation.table());
            plainSelect.setFromItem(relTable);

            // WHERE userColumn = ?
            EqualsTo userCond = new EqualsTo();
            userCond.setLeftExpression(new Column(relTable, relation.userColumn()));
            userCond.setRightExpression(new StringValue(currentUserId.toString()));
            plainSelect.setWhere(userCond);

            var subSelect = new ParenthesedSelect();
            subSelect.setSelect(plainSelect);
            in.setRightExpression(subSelect);

            if (combined == null) {
                combined = in;
            } else {
                combined = new OrExpression(combined, in);
            }
        }

        return combined;
    }

    /// 从 MyBatis mappedStatementId 反推实体类
    ///
    /// mappedStatementId 格式: com.devops00.spectra.core.user.mapper.UserMapper.selectById
    /// 尝试从 Mapper 包路径推导对应的 Entity 包路径
    private Class<?> resolveEntityClass(String mappedStatementId) {
        if (mappedStatementId == null) {
            return null;
        }
        try {
            // 格式: mapperPackage.MapperName.methodName
            int lastDot = mappedStatementId.lastIndexOf('.');
            if (lastDot < 0) return null;
            String classPath = mappedStatementId.substring(0, lastDot);

            // 尝试将 mapper 替换为 javabean/entity 查找实体类
            // 例如: core.user.mapper.UserMapper → core.user.javabean.entity.User
            String entityPath = classPath
                    .replace(".mapper.", ".javabean.entity.")
                    .replace("Mapper", "");

            return Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
