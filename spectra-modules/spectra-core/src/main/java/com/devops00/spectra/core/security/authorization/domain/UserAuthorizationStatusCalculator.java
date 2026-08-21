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

package com.devops00.spectra.core.security.authorization.domain;

import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;

import java.time.Instant;
import java.util.List;

/**
 * 根据授权实例视图计算用户授权状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
public final class UserAuthorizationStatusCalculator {

    private UserAuthorizationStatusCalculator() {
    }

    /**
     * 计算当前时刻的用户授权状态。
     *
     * @param assignments 用户的全部授权实例
     * @return 授权状态
     */
    public static UserAuthorizationStatus calculate(List<AuthorizationAssignmentView> assignments) {
        return calculate(assignments, Instant.now());
    }

    /**
     * 使用指定时刻计算用户授权状态，便于边界时间测试。
     *
     * @param assignments 用户的全部授权实例
     * @param now 当前时刻
     * @return 授权状态
     */
    static UserAuthorizationStatus calculate(List<AuthorizationAssignmentView> assignments, Instant now) {
        if (assignments == null || assignments.isEmpty()) {
            return UserAuthorizationStatus.UNCONFIGURED;
        }

        var effectiveCount = 0;
        var completeCount = 0;
        var incompleteCount = 0;
        var invalidCount = 0;
        for (var assignment : assignments) {
            var assignmentEffective = "ACTIVE".equals(assignment.state())
                    && (assignment.validFrom() == null || !assignment.validFrom().isAfter(now))
                    && (assignment.validUntil() == null || assignment.validUntil().isAfter(now));
            var roleEffective = "ACTIVE".equals(assignment.roleState());
            if (!assignmentEffective || !roleEffective) {
                invalidCount++;
                continue;
            }

            effectiveCount++;
            var requiredPermissionCount = assignment.rolePermissionCount() == null
                    ? 0L
                    : assignment.rolePermissionCount();
            var configuredBoundaryCount = assignment.accessBoundaries() == null
                    ? 0
                    : assignment.accessBoundaries().size();
            if (requiredPermissionCount == 0 || configuredBoundaryCount >= requiredPermissionCount) {
                completeCount++;
            } else {
                incompleteCount++;
            }
        }

        if (effectiveCount == 0) {
            return UserAuthorizationStatus.PARTIAL;
        }
        if (invalidCount > 0 || (completeCount > 0 && incompleteCount > 0)) {
            return UserAuthorizationStatus.PARTIAL;
        }
        if (incompleteCount > 0) {
            return UserAuthorizationStatus.INCOMPLETE;
        }
        return UserAuthorizationStatus.ACTIVE;
    }
}
