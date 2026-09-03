/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.configure.mybatis.security;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.security.authorization.ResourceOperation;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.ExecutionContext;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import com.devops00.spectra.common.security.authorization.ScopedAuthorization;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 资源级 IDOR、批量和导出门禁。
 *
 * <p>服务层在读取详情、更新、删除、批量操作或导出前调用本门面。它只接受服务端解析出的
 * {@link ScopeQuery}，绝不接受客户端直接声明的部门范围。</p>
 */
@Component
public class ResourceAuthorizationGuard {

    private final ObjectProvider<AuthorizationSnapshotProvider> provider;

    @Autowired
    public ResourceAuthorizationGuard(ObjectProvider<AuthorizationSnapshotProvider> provider) {
        this.provider = provider;
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertAllowed}）。
     */
    public void assertAllowed(ExecutionContext context, ScopeQuery query) {
        if (context == null || query == null) {
            throw new DataScopeViolationException("资源授权上下文不完整");
        }
        AuthorizationSnapshotProvider snapshotProvider = provider.getIfAvailable();
        if (snapshotProvider == null) {
            throw new DataScopeViolationException("授权快照读取器不可用，拒绝资源访问");
        }
        AuthorizationSnapshot snapshot = snapshotProvider.load(context.subjectId());
        assertAllowed(new ScopedAuthorization(context.subjectId(), snapshot), context, query);
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertBatchAllowed}）。
     */
    public void assertBatchAllowed(ExecutionContext context, Collection<ScopeQuery> queries) {
        if (queries == null || queries.isEmpty()) {
            throw new DataScopeViolationException("批量资源集合不能为空");
        }
        for (ScopeQuery query : queries) {
            assertAllowed(context, query);
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertExportAllowed}）。
     */
    public void assertExportAllowed(ExecutionContext context, Collection<ScopeQuery> queries) {
        if (context == null || context.operation() != ResourceOperation.EXPORT) {
            throw new DataScopeViolationException("导出操作必须使用 EXPORT Permission");
        }
        assertBatchAllowed(context, queries);
    }

    /**
     * 校验并确保数据满足当前约束（{@code assertAllowed}）。
     */
    public static void assertAllowed(ScopedAuthorization authorization, ExecutionContext context, ScopeQuery query) {
        if (!authorization.allows(context, query)) {
            throw new DataScopeViolationException("资源不在当前 Permission 的 Access Boundary 内");
        }
    }
}
