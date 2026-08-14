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

package com.devops00.spectra.security.base.authorization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户获得的一个 RoleAssignment 及其 Access/Grant 边界。
 */
public record AuthorizationAssignment(UUID assignmentId,
                                      String roleCode,
                                      Map<String, PermissionBoundary> accessBoundaries,
                                      Map<String, PermissionBoundary> grantBoundaries) {

    public AuthorizationAssignment {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId 不能为空");
        }
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("roleCode 不能为空");
        }
        accessBoundaries = immutableByPermission(accessBoundaries);
        grantBoundaries = immutableByPermission(grantBoundaries);
    }

    private static Map<String, PermissionBoundary> immutableByPermission(Map<String, PermissionBoundary> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        var copy = new LinkedHashMap<String, PermissionBoundary>();
        source.forEach((permission, boundary) -> {
            if (boundary == null || !permission.equals(boundary.permission())) {
                throw new IllegalArgumentException("Permission 与 Boundary 绑定不一致");
            }
            copy.put(permission, boundary);
        });
        return Collections.unmodifiableMap(copy);
    }
}
