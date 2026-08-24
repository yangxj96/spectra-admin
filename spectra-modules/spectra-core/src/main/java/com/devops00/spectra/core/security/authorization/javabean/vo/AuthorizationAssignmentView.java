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

package com.devops00.spectra.core.security.authorization.javabean.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * RoleAssignment 只读视图，保留每个 Assignment 内的 Access/Grant Boundary 绑定。
 */
public record AuthorizationAssignmentView(UUID assignmentId,
                                          UUID userId,
                                          UUID roleId,
                                          String roleCode,
                                          String roleKind,
                                          String roleName,
                                          Boolean roleSystemManaged,
                                          String roleState,
                                          Long roleVersion,
                                          Long rolePermissionCount,
                                          Long version,
                                          String state,
                                          LocalDateTime validFrom,
                                          LocalDateTime validUntil,
                                          List<AuthorizationBoundaryView> accessBoundaries,
                                          List<AuthorizationBoundaryView> grantBoundaries) {

    public AuthorizationAssignmentView {
        accessBoundaries = immutableList(accessBoundaries);
        grantBoundaries = immutableList(grantBoundaries);
    }

    @Override
    public List<AuthorizationBoundaryView> accessBoundaries() {
        return immutableList(accessBoundaries);
    }

    @Override
    public List<AuthorizationBoundaryView> grantBoundaries() {
        return immutableList(grantBoundaries);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableList}）。
     */
    private static <T> List<T> immutableList(List<T> source) {
        return source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
    }
}
