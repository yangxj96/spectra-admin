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

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * 承载授权相关的不可变数据。
 *
 * @param subjectId 数据记录的唯一标识
 * @param snapshot  当前用户的授权信息快照
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record ScopedAuthorization(UUID subjectId, AuthorizationSnapshot snapshot) {

    public ScopedAuthorization {
        if (subjectId == null) {
            throw new IllegalArgumentException("subjectId 不能为空");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("authorization snapshot 不能为空");
        }
    }

    /**
     * 判断条件是否满足（{@code hasPermission}）。
     */
    public boolean hasPermission(String permission) {
        return snapshot.hasPermission(permission);
    }

    /**
     * 查询或获取目标数据（{@code allows}）。
     */
    public boolean allows(ExecutionContext context, ScopeQuery query) {
        return context != null
                && subjectId.equals(context.subjectId())
                && snapshot.canAccess(context.permission(), query);
    }

    /**
     * 查询或获取目标数据（{@code allows}）。
     */
    public boolean allows(String permission, ScopeQuery query) {
        return permission != null && snapshot.canAccess(permission, query);
    }
}
