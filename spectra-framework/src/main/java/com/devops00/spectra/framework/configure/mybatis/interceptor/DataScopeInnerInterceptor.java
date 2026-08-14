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
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.framework.configure.mybatis.DataScopeEntityRegistry;
import com.devops00.spectra.framework.configure.mybatis.security.ScopeSqlPolicy;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MP执行的单表SQL拦截处理 — 自动注入数据范围 WHERE 条件
 *
 * <h3>二维过滤</h3>
 * <ul>
 * <li><b>结构维度</b>：基于 department_id / created_by 等归属字段，根据用户数据范围类型过滤</li>
 * <li><b>关系维度</b>：基于 {@link DataScope#relations()} 声明的多对多关联表</li>
 * </ul>
 *
 * <h3>跳过规则</h3>
 * <ul>
 * <li>业务实体设置 {@link DataScope#ignore()} = true 会 fail-closed，不能绕过登记</li>
 * <li>GLOBAL 范围的用户不过滤</li>
 * <li>缺少登录上下文时拒绝执行（fail-closed）</li>
 * </ul>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@Slf4j
public class DataScopeInnerInterceptor implements MultiDataPermissionHandler {

    private final ObjectProvider<DataScopeProvider> dataScopeProvider;

    private final ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider;

    private final DataScopeEntityRegistry dataScopeEntityRegistry;

    /**
     * 保留给框架单测和没有新授权模块的独立使用场景；正式应用使用三参数构造器。
     */
    public DataScopeInnerInterceptor(ObjectProvider<DataScopeProvider> dataScopeProvider, DataScopeEntityRegistry dataScopeEntityRegistry) {
        this(dataScopeProvider, null, dataScopeEntityRegistry);
    }

    @Autowired
    public DataScopeInnerInterceptor(ObjectProvider<DataScopeProvider> dataScopeProvider,
                                     ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider,
                                     DataScopeEntityRegistry dataScopeEntityRegistry) {
        this.dataScopeProvider = dataScopeProvider;
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
        this.dataScopeEntityRegistry = dataScopeEntityRegistry;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (DataScopeContextHolder.isBypassed()) {
            return null;
        }
        // 解析表名与实体类
        String tableName = table.getName();
        if (tableName == null) {
            return null;
        }

        // 尝试获取实体类上的 @DataScope 注解
        Class<?> entityClass = resolveEntityClass(mappedStatementId);
        DataScope annotation = entityClass != null ? entityClass.getAnnotation(DataScope.class) : null;
        // XML / 自动分页语句等场景可能无法从 mappedStatementId 推导实体，
        // 此时必须继续使用按表名注册的元数据，不能直接放弃隔离。
        if (annotation == null) {
            annotation = dataScopeEntityRegistry.find(tableName);
        }
        if (annotation == null) {
            return null;
        }
        if (annotation.ignore()) {
            throw new DataScopeViolationException("受保护资源不得使用未登记的 ignore=true 数据范围策略");
        }

        // 只有明确标注了 @DataScope 的业务表才需要登录上下文。
        // 认证流程会在登录前查询 sys_account/sys_user 等基础表，
        // 这些表不属于数据隔离范围，不能因为当前尚未建立用户上下文而失败。
        UUID userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            throw new DataScopeViolationException("数据权限 SQL 缺少当前用户上下文");
        }

        AuthorizationSnapshotProvider snapshotProvider = authorizationSnapshotProvider == null
                ? null : authorizationSnapshotProvider.getIfAvailable();
        if (snapshotProvider != null) {
            String permission = ScopeSqlPolicy.permissionFor(annotation, mappedStatementId);
            if (permission == null) {
                return where;
            }
            AuthorizationSnapshot snapshot = snapshotProvider.load(userId);
            Expression scopeExpression = ScopeSqlPolicy.build(table, annotation,
                    snapshot.accessBoundaries(permission), userId);
            if (where == null) {
                return scopeExpression;
            }
            return new AndExpression(where, scopeExpression);
        }

        // 解析用户有效数据范围
        DataScopeProvider.EffectiveScope scope = DataScopeContextHolder.getScope(userId);
        if (scope == null) {
            scope = dataScopeProvider.getObject().resolve(userId);
            if (scope != null) {
                DataScopeContextHolder.setScope(userId, scope);
            }
        }
        if (scope == null || scope.getScopeType() == null) {
            throw new DataScopeViolationException("无法解析当前用户的数据范围，已拒绝访问");
        }
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

    /**
     * 构建数据范围 SQL 表达式
     */
    private Expression buildScopeExpression(Table table, DataScope annotation, DataScopeProvider.EffectiveScope scope) {
        String columnName = annotation != null ? annotation.column() : "department_id";
        UUID currentUserId = SecUtil.getCurrentUserId();

        Expression structuralExpr = buildStructuralExpression(table, annotation, columnName, scope, currentUserId);

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

    /**
     * 构建结构维度条件（department_id / created_by）
     */
    private Expression buildStructuralExpression(Table table, DataScope annotation, String columnName, DataScopeProvider.EffectiveScope scope,
                                                 UUID currentUserId) {
        return switch (scope.getScopeType()) {
            case SELF -> {
                // created_by = currentUserId
                EqualsTo eq = new EqualsTo();
                eq.setLeftExpression(new Column(table, annotation.ownerColumn()));
                eq.setRightExpression(new StringValue(currentUserId.toString()));
                yield eq;
            }
            case DEPT, DEPT_AND_CHILDREN, CUSTOM -> {
                var targetIds = scope.getTargetIds();
                if (targetIds == null || targetIds.isEmpty()) {
                    // 空范围必须拒绝全部数据，不能返回 null 形成 fail-open。
                    yield new EqualsTo(new LongValue(1), new LongValue(0));
                }
                InExpression in = new InExpression();
                in.setLeftExpression(new Column(table, columnName));
                if (targetIds.size() == 1) {
                    // 单个值用 EqualsTo 更高效
                    EqualsTo eq = new EqualsTo();
                    eq.setLeftExpression(new Column(table, columnName));
                    eq.setRightExpression(new StringValue(targetIds.get(0).toString()));
                    yield eq;
                }
                ExpressionList<Expression> exprList = new ExpressionList<>(
                        targetIds.stream().map(id -> new StringValue(id.toString())).collect(Collectors.toList()));
                in.setRightExpression(exprList);
                yield in;
            }
            case GLOBAL -> null;
        };
    }

    /**
     * 构建关系维度条件（关联表子查询）
     */
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
            Table relTable = new Table(relation.schema(), relation.table());
            plainSelect.addSelectItems(new Column(relTable, relation.joinColumn()));

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

    /**
     * 从 MyBatis mappedStatementId 反推实体类
     *
     * <p>mappedStatementId 格式:</p>
     * <p>com.devops00.spectra.core.user.mapper.UserMapper.selectById 尝试从 Mapper</p>
     * <p>包路径推导对应的 Entity 包路径</p>
     */
    private Class<?> resolveEntityClass(String mappedStatementId) {
        if (mappedStatementId == null) {
            return null;
        }
        try {
            // 格式: mapperPackage.MapperName.methodName
            int lastDot = mappedStatementId.lastIndexOf('.');
            if (lastDot < 0)
                return null;
            String classPath = mappedStatementId.substring(0, lastDot);

            // 尝试将 mapper 替换为 javabean/entity 查找实体类
            // 例如: core.user.mapper.UserMapper → core.user.javabean.entity.User
            String entityPath = classPath.replace(".mapper.", ".javabean.entity.").replace("Mapper", "");

            return Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
