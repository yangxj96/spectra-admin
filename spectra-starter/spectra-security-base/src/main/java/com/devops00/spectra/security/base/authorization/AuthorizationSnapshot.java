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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assignment-preserving 授权快照。
 * <p>
 * 相同 Permission 的 Scope 可以合并；不同 Permission 永远只能读取自己的 Assignment Boundary，
 * 不能先把所有 Permission 和 Scope 全局 UNION 后重新组合。
 */
public final class AuthorizationSnapshot {

    private final List<AuthorizationAssignment> assignments;

    private final Map<String, List<PermissionBoundary>> accessBoundaries;

    private final Map<String, List<PermissionBoundary>> grantBoundaries;

    private AuthorizationSnapshot(List<AuthorizationAssignment> assignments) {
        this.assignments = List.copyOf(assignments == null ? List.of() : assignments);
        this.accessBoundaries = index(this.assignments, true);
        this.grantBoundaries = index(this.assignments, false);
    }

    public static AuthorizationSnapshot of(List<AuthorizationAssignment> assignments) {
        return new AuthorizationSnapshot(assignments);
    }

    public List<AuthorizationAssignment> assignments() {
        return assignments;
    }

    public Set<String> permissions() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(accessBoundaries.keySet()));
    }

    public Set<String> grantablePermissions() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(grantBoundaries.keySet()));
    }

    public boolean hasPermission(String permission) {
        return permission != null && accessBoundaries.containsKey(permission);
    }

    public boolean canAccess(String permission, ScopeQuery query) {
        return allows(accessBoundaries.get(permission), query);
    }

    public boolean canGrant(String permission, ScopeQuery query) {
        return allows(grantBoundaries.get(permission), query);
    }

    public List<PermissionBoundary> accessBoundaries(String permission) {
        return accessBoundaries.getOrDefault(permission, List.of());
    }

    public List<PermissionBoundary> grantBoundaries(String permission) {
        return grantBoundaries.getOrDefault(permission, List.of());
    }

    private static boolean allows(List<PermissionBoundary> boundaries, ScopeQuery query) {
        return boundaries != null && boundaries.stream().anyMatch(boundary -> boundary.scope().allows(query));
    }

    private static Map<String, List<PermissionBoundary>> index(List<AuthorizationAssignment> assignments, boolean access) {
        var result = new LinkedHashMap<String, List<PermissionBoundary>>();
        for (AuthorizationAssignment assignment : assignments) {
            var boundaries = access ? assignment.accessBoundaries() : assignment.grantBoundaries();
            boundaries.forEach((permission, boundary) -> result.computeIfAbsent(permission, ignored -> new ArrayList<>()).add(boundary));
        }
        result.replaceAll((permission, boundaries) -> List.copyOf(boundaries));
        return Collections.unmodifiableMap(result);
    }
}
