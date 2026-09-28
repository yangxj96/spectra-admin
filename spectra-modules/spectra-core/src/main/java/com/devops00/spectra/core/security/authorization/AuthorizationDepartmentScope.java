package com.devops00.spectra.core.security.authorization;

import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.system.javabean.entity.Department;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves permission-specific department scopes for system queries. */
public final class AuthorizationDepartmentScope {

    private AuthorizationDepartmentScope() {
    }

    /** Whether this permission has an ALL boundary (or the user has root ownership). */
    public static boolean isUnrestricted(AuthorizationSnapshot snapshot, String permission) {
        return snapshot != null && (snapshot.isRoot()
                || snapshot.accessBoundaries(permission).stream()
                        .anyMatch(boundary -> boundary.scope().mode() == ScopeMode.ALL));
    }

    /**
     * Resolves departments that the subject can query under the requested permission.
     * RULES boundaries are evaluated through the authorization snapshot so assignment boundaries
     * and the subject's own department memberships remain intersected.
     */
    public static Set<UUID> visibleDepartmentIds(AuthorizationSnapshot snapshot, String permission,
                                                  List<Department> departments) {
        if (snapshot == null || departments == null || departments.isEmpty()) {
            return Set.of();
        }
        Map<UUID, Department> byId = index(departments);
        Set<UUID> visible = new LinkedHashSet<>();
        for (Department department : byId.values()) {
            var query = new ScopeQuery(null, null, department.getId(), ancestors(department, byId));
            if (snapshot.canAccess(permission, query)) {
                visible.add(department.getId());
            }
        }
        return Set.copyOf(visible);
    }

    /** Adds ancestors only as structural context for the authorized department tree. */
    public static Set<UUID> treeDepartmentIds(AuthorizationSnapshot snapshot, String permission,
                                               List<Department> departments) {
        if (snapshot == null || departments == null || departments.isEmpty()) {
            return Set.of();
        }
        Map<UUID, Department> byId = index(departments);
        if (isUnrestricted(snapshot, permission)) {
            return Set.copyOf(byId.keySet());
        }
        Set<UUID> visible = new LinkedHashSet<>(visibleDepartmentIds(snapshot, permission, departments));
        for (UUID departmentId : List.copyOf(visible)) {
            Department current = byId.get(departmentId);
            Set<UUID> visited = new HashSet<>();
            while (current != null && current.getPid() != null && visited.add(current.getId())) {
                visible.add(current.getPid());
                current = byId.get(current.getPid());
            }
        }
        return Set.copyOf(visible);
    }

    /** Checks whether a user row belongs to the permission's SELF or department scope. */
    public static boolean canAccessUser(AuthorizationSnapshot snapshot, String permission,
                                        UUID viewerUserId, UUID targetUserId,
                                        Set<UUID> targetDepartmentIds, List<Department> departments) {
        if (isUnrestricted(snapshot, permission)) {
            return true;
        }
        if (allowsOwnUser(snapshot, permission, viewerUserId, targetUserId)) {
            return true;
        }
        if (snapshot == null || targetDepartmentIds == null || targetDepartmentIds.isEmpty()) {
            return false;
        }
        Set<UUID> visibleDepartments = visibleDepartmentIds(snapshot, permission, departments);
        return targetDepartmentIds.stream().anyMatch(visibleDepartments::contains);
    }

    /** Checks whether the requested department itself is in the permission scope. */
    public static boolean canAccessDepartment(AuthorizationSnapshot snapshot, String permission,
                                              UUID departmentId, List<Department> departments) {
        return departmentId != null
                && visibleDepartmentIds(snapshot, permission, departments).contains(departmentId);
    }

    /** Whether the permission's SELF or ALL boundary allows the current user's own user row. */
    public static boolean allowsOwnUser(AuthorizationSnapshot snapshot, String permission,
                                        UUID viewerUserId, UUID targetUserId) {
        return viewerUserId != null && targetUserId != null && snapshot != null
                && snapshot.canAccess(permission, new ScopeQuery(targetUserId, viewerUserId, null, Set.of()));
    }

    private static Map<UUID, Department> index(List<Department> departments) {
        Map<UUID, Department> byId = new HashMap<>();
        for (Department department : departments) {
            if (department != null && department.getId() != null) {
                byId.put(department.getId(), department);
            }
        }
        return byId;
    }

    private static Set<UUID> ancestors(Department department, Map<UUID, Department> byId) {
        Set<UUID> ancestors = new LinkedHashSet<>();
        Set<UUID> visited = new HashSet<>();
        Department current = department;
        while (current != null && current.getPid() != null && visited.add(current.getId())) {
            ancestors.add(current.getPid());
            current = byId.get(current.getPid());
        }
        return ancestors;
    }
}
