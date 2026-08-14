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
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.framework.configure.mybatis.DataScopeEntityRegistry;
import com.devops00.spectra.framework.configure.mybatis.security.ScopeSqlPolicy;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * MP执行的单表SQL拦截处理 — 自动注入数据范围 WHERE 条件
 *
 * <h3>二维过滤</h3>
 * <ul>
 * <li><b>结构维度</b>：基于 department_id / created_by 等归属字段，根据 Permission-specific Boundary 过滤</li>
 * <li><b>关系维度</b>：基于 {@link DataScope#relations()} 声明的多对多关联表</li>
 * </ul>
 *
 * <h3>跳过规则</h3>
 * <ul>
 * <li>业务实体设置 {@link DataScope#ignore()} = true 会 fail-closed，不能绕过登记</li>
 * <li>ALL/NONE 等特殊 Boundary 由统一的 ScopeSqlPolicy 解释</li>
 * <li>缺少登录上下文时拒绝执行（fail-closed）</li>
 * </ul>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@Slf4j
public class DataScopeInnerInterceptor implements MultiDataPermissionHandler {

    private final ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider;

    private final DataScopeEntityRegistry dataScopeEntityRegistry;

    private final @Nullable SecurityContextAccessor securityContextAccessor;

    /**
     * 保留给框架单测和没有新授权模块的独立使用场景；正式应用使用三参数构造器。
     */
    public DataScopeInnerInterceptor(ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider,
                                     DataScopeEntityRegistry dataScopeEntityRegistry) {
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
        this.dataScopeEntityRegistry = dataScopeEntityRegistry;
        this.securityContextAccessor = null;
    }

    @Autowired
    public DataScopeInnerInterceptor(ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider,
                                     DataScopeEntityRegistry dataScopeEntityRegistry,
                                     SecurityContextAccessor securityContextAccessor) {
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
        this.dataScopeEntityRegistry = dataScopeEntityRegistry;
        this.securityContextAccessor = securityContextAccessor;
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
        UUID userId = securityContextAccessor == null ? null : securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new DataScopeViolationException("数据权限 SQL 缺少当前用户上下文");
        }

        AuthorizationSnapshotProvider snapshotProvider = authorizationSnapshotProvider == null
                ? null : authorizationSnapshotProvider.getIfAvailable();
        if (snapshotProvider == null) {
            throw new DataScopeViolationException("授权快照提供者未配置，已拒绝访问");
        }
        String permission = ScopeSqlPolicy.permissionFor(annotation, mappedStatementId);
        if (permission == null) {
            return where;
        }
        AuthorizationSnapshot snapshot = snapshotProvider.load(userId);
        Expression scopeExpression = ScopeSqlPolicy.build(table, annotation,
                snapshot.accessBoundaries(permission), userId);
        if (scopeExpression == null) {
            return where;
        }
        if (where == null) {
            return scopeExpression;
        }
        return new AndExpression(where, scopeExpression);
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
