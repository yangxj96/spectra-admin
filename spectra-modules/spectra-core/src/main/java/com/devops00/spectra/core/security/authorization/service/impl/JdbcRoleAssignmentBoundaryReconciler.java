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

package com.devops00.spectra.core.security.authorization.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * 在角色权限变更事务中清除不再属于角色的 Assignment Boundary 及其孤立 Scope。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@Component
public final class JdbcRoleAssignmentBoundaryReconciler {

    private static final String DELETE_ACCESS = """
            DELETE FROM sec_assignment_permission_boundary AS boundary
            USING sec_role_assignment AS assignment
            WHERE boundary.assignment_id = assignment.id
              AND assignment.role_id = ?
              AND assignment.deleted IS NULL
              AND boundary.deleted IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM sec_role_permission AS declared
                  WHERE declared.role_id = assignment.role_id
                    AND declared.permission_id = boundary.permission_id
                    AND declared.deleted IS NULL)
            RETURNING boundary.scope_id
            """;

    private static final String DELETE_GRANT = """
            DELETE FROM sec_assignment_grant_boundary AS boundary
            USING sec_role_assignment AS assignment
            WHERE boundary.assignment_id = assignment.id
              AND assignment.role_id = ?
              AND assignment.deleted IS NULL
              AND boundary.deleted IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM sec_role_grantable_permission AS declared
                  WHERE declared.role_id = assignment.role_id
                    AND declared.permission_id = boundary.permission_id
                    AND declared.deleted IS NULL)
            RETURNING boundary.scope_id
            """;

    private static final String SCOPE_REFERENCED = """
            SELECT EXISTS (SELECT 1 FROM sec_assignment_permission_boundary WHERE scope_id = ?)
                OR EXISTS (SELECT 1 FROM sec_assignment_grant_boundary WHERE scope_id = ?)
            """;

    private static final String DELETE_SCOPE_RULES = "DELETE FROM sec_scope_rule WHERE scope_id = ?";

    private static final String DELETE_SCOPE = "DELETE FROM sec_authorization_scope WHERE id = ?";

    private final JdbcTemplate jdbc;

    private final String schema;

    @Autowired
    public JdbcRoleAssignmentBoundaryReconciler(JdbcTemplate jdbc) {
        this(jdbc, "spectra_security");
    }

    JdbcRoleAssignmentBoundaryReconciler(JdbcTemplate jdbc, String schema) {
        this.jdbc = Objects.requireNonNull(jdbc);
        if (schema == null || !schema.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException("授权数据库 schema 名称无效");
        }
        this.schema = schema;
    }

    /** 在 Role 行已被更新锁定后执行；数据库异常使外层事务回滚。 */
    void reconcile(UUID roleId) {
        Objects.requireNonNull(roleId, "roleId");
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Role Boundary 清理必须处于角色变更事务中");
        }
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            String previousSchema = connection.getSchema();
            connection.setSchema(schema);
            try {
                var removedScopeIds = new HashSet<UUID>();
                deleteObsolete(connection, DELETE_ACCESS, roleId, removedScopeIds);
                deleteObsolete(connection, DELETE_GRANT, roleId, removedScopeIds);
                for (var scopeId : removedScopeIds) {
                    if (!scopeIsReferenced(connection, scopeId)) {
                        deleteByScope(connection, DELETE_SCOPE_RULES, scopeId);
                        deleteByScope(connection, DELETE_SCOPE, scopeId);
                    }
                }
            } finally {
                connection.setSchema(previousSchema);
            }
            return null;
        });
    }

    private void deleteObsolete(Connection connection, String sql, UUID roleId, HashSet<UUID> scopes) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, roleId);
            try (var rows = statement.executeQuery()) {
                while (rows.next()) {
                    scopes.add(rows.getObject(1, UUID.class));
                }
            }
        }
    }

    private boolean scopeIsReferenced(Connection connection, UUID scopeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SCOPE_REFERENCED)) {
            statement.setObject(1, scopeId);
            statement.setObject(2, scopeId);
            try (var rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new SQLException("无法确认授权范围引用状态");
                }
                return rows.getBoolean(1);
            }
        }
    }

    private void deleteByScope(Connection connection, String sql, UUID scopeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, scopeId);
            statement.executeUpdate();
        }
    }
}
