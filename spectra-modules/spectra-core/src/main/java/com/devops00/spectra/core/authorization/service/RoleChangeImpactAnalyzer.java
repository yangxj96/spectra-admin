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

package com.devops00.spectra.core.authorization.service;

import com.devops00.spectra.core.authorization.domain.RoleAuthorizationState;
import com.devops00.spectra.core.authorization.domain.RoleChangeImpact;

import java.util.HashSet;

/**
 * Role 授权状态的纯影响分析器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public class RoleChangeImpactAnalyzer {

    /**
     * 计算权限、可授予权限和 authorityLevel 的变化。
     *
     * @param before                  变更前状态
     * @param after                   变更后状态
     * @param affectedAssignmentCount 受影响 Assignment 数量
     * @param affectedUserCount       受影响用户数量
     * @return 影响结果
     */
    public RoleChangeImpact analyze(RoleAuthorizationState before,
                                    RoleAuthorizationState after,
                                    int affectedAssignmentCount,
                                    int affectedUserCount) {
        if (before == null || after == null) {
            throw new IllegalArgumentException("Role 变更前后状态不能为空");
        }
        var addedPermissions = difference(after.permissions(), before.permissions());
        var removedPermissions = difference(before.permissions(), after.permissions());
        var addedGrantable = difference(after.grantablePermissions(), before.grantablePermissions());
        var removedGrantable = difference(before.grantablePermissions(), after.grantablePermissions());
        boolean levelChanged = before.authorityLevel() != after.authorityLevel();
        boolean expands = !addedPermissions.isEmpty() || !addedGrantable.isEmpty()
                || after.authorityLevel() > before.authorityLevel();
        return new RoleChangeImpact(addedPermissions, removedPermissions, addedGrantable, removedGrantable,
                levelChanged, expands, affectedAssignmentCount, affectedUserCount);
    }

    private static <T> HashSet<T> difference(java.util.Set<T> left, java.util.Set<T> right) {
        var result = new HashSet<>(left);
        result.removeAll(right);
        return result;
    }
}
