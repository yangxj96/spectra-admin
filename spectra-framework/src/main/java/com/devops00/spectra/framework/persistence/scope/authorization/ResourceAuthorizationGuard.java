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

package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.security.authorization.ResourceOperation;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.ExecutionContext;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import com.devops00.spectra.common.security.authorization.ScopedAuthorization;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * 资源级 IDOR、批量和导出门禁。
 *
 * <p>服务层在读取详情、更新、删除、批量操作或导出前调用本门面。它只接受服务端解析出的
 * {@link ScopeQuery}，绝不接受客户端直接声明的部门范围。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class ResourceAuthorizationGuard {

    private final ObjectProvider<AuthorizationSnapshotProvider> provider;

    public ResourceAuthorizationGuard(ObjectProvider<AuthorizationSnapshotProvider> provider) {
        this.provider = Objects.requireNonNull(provider, "authorizationSnapshotProvider 不能为空");
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertAllowed}）。
     *
     * @param context 资源授权上下文，包含操作者、目标资源和权限范围。
     * @param query   待校验的单条资源范围查询。
     */
    public void assertAllowed(ExecutionContext context, ScopeQuery query) {
        requireContextAndQuery(context, query);
        assertAllowed(loadAuthorization(context), context, query);
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertBatchAllowed}）。
     *
     * @param context 资源授权上下文，包含操作者、目标资源和权限范围。
     * @param queries 待批量校验的资源范围查询集合。
     */
    public void assertBatchAllowed(ExecutionContext context, Collection<ScopeQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new DataScopeViolationException("批量资源集合不能为空");
        }
        requireContext(context);
        queries.forEach(ResourceAuthorizationGuard::requireQuery);
        var authorization = loadAuthorization(context);
        for (ScopeQuery query : queries) {
            assertAllowed(authorization, context, query);
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertExportAllowed}）。
     *
     * @param context 资源授权上下文，包含操作者、目标资源和权限范围。
     * @param queries 待批量校验的资源范围查询集合。
     */
    public void assertExportAllowed(ExecutionContext context, Collection<ScopeQuery> queries) {
        if (context == null || context.operation() != ResourceOperation.EXPORT) {
            throw new DataScopeViolationException("导出操作必须使用 EXPORT Permission");
        }
        assertBatchAllowed(context, queries);
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertAllowed}）。
     *
     * @param authorization 待校验的资源授权声明。
     * @param context       资源授权上下文，包含操作者、目标资源和权限范围。
     * @param query         待校验的单条资源范围查询。
     */
    public static void assertAllowed(ScopedAuthorization authorization, ExecutionContext context, ScopeQuery query) {
        if (!authorization.allows(context, query)) {
            throw new DataScopeViolationException("资源不在当前 Permission 的 Access Boundary 内");
        }
    }

    /**
     * 查询授权。
     */
    private ScopedAuthorization loadAuthorization(ExecutionContext context) {
        AuthorizationSnapshotProvider snapshotProvider = provider.getIfAvailable();
        if (snapshotProvider == null) {
            throw new DataScopeViolationException("授权快照读取器不可用，拒绝资源访问");
        }
        AuthorizationSnapshot snapshot = snapshotProvider.load(context.subjectId());
        return new ScopedAuthorization(context.subjectId(), snapshot);
    }

    /**
     * 校验上下文查询。
     */
    private static void requireContextAndQuery(ExecutionContext context, ScopeQuery query) {
        requireContext(context);
        requireQuery(query);
    }

    /**
     * 校验上下文。
     */
    private static void requireContext(ExecutionContext context) {
        if (context == null) {
            throw new DataScopeViolationException("资源授权上下文不完整");
        }
    }

    /**
     * 校验查询。
     */
    private static void requireQuery(ScopeQuery query) {
        if (query == null) {
            throw new DataScopeViolationException("资源授权查询不完整");
        }
    }
}
