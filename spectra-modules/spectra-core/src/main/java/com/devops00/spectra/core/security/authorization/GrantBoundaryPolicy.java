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

package com.devops00.spectra.core.security.authorization;

import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeContains;

import java.util.List;
import java.util.UUID;

/**
 * Assignment-preserving Grant Boundary 校验器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class GrantBoundaryPolicy {

    private GrantBoundaryPolicy() {
    }

    /**
     * 校验操作者是否能执行一组授权变更。
     *
     * @param operatorSnapshot 操作者授权快照
     * @param operatorId       操作者 ID
     * @param targetUserId     目标用户 ID
     * @param requests         按 Permission 拆分的变更请求
     * @param root             是否通过统一 RootPolicy 判定
     */
    public static void assertAllowed(AuthorizationSnapshot operatorSnapshot,
                                     UUID operatorId,
                                     UUID targetUserId,
                                     List<AuthorizationGrantRequest> requests,
                                     boolean root) {
        if (operatorSnapshot == null) {
            throw new GrantBoundaryViolationException("操作者授权上下文缺失，拒绝授权变更");
        }
        if (operatorId != null && operatorId.equals(targetUserId) && !root) {
            throw new GrantBoundaryViolationException("普通管理员不能修改自己的授权");
        }
        if (root) {
            return;
        }
        if (requests == null || requests.isEmpty()) {
            throw new GrantBoundaryViolationException("授权变更不能为空");
        }
        for (var request : requests) {
            var candidates = operatorSnapshot.assignments()
                    .stream()
                    .filter(assignment -> assignment.authorityLevel() > request.targetAuthorityLevel())
                    .filter(assignment -> assignment.grantBoundaries().containsKey(request.permission()))
                    .filter(assignment -> request.accessScope() == null
                            || contains(assignment.grantBoundaries().get(request.permission()), request.accessScope()))
                    .filter(assignment -> request.grantScope() == null
                            || contains(assignment.grantBoundaries().get(request.permission()), request.grantScope()))
                    .toList();
            if (candidates.isEmpty()) {
                throw new GrantBoundaryViolationException("Permission " + request.permission()
                        + " 超出操作者的 Grant Boundary 或 authorityLevel");
            }
        }
    }

    /**
     * 判断条件是否满足（{@code contains}）。
     */
    private static boolean contains(PermissionBoundary container, AuthorizationScope requested) {
        return requested != null && ScopeContains.contains(container.scope(), requested);
    }
}
