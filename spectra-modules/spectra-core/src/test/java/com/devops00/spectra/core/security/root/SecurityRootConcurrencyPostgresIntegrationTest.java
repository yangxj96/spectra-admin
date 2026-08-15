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

package com.devops00.spectra.core.security.root;

import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.root.RootGovernanceException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 真实 PostgreSQL 上验证 Root singleton 行锁覆盖并发新增和撤销。
 * <p>
 * 该测试默认禁用，只接受专用、可丢弃的 Flyway 测试数据库连接。
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_FLYWAY_POSTGRES_TEST", matches = "true")
class SecurityRootConcurrencyPostgresIntegrationTest {

    private static final String MIGRATION_LOCATION = "classpath:db/migration";

    @Test
    void concurrentRootChangesMustRespectMinAndMaxLimits() throws Exception {
        DatabaseConfig database = DatabaseConfig.from("SPECTRA_SECURITY_FLYWAY_DB_");
        Flyway.configure()
                .dataSource(database.url(), database.username(), database.password())
                .locations(MIGRATION_LOCATION)
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();

        var dataSource = new DriverManagerDataSource(database.url(), database.username(), database.password());
        var jdbc = new JdbcTemplate(dataSource);
        var roleId = UUID.randomUUID();
        var users = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var assignments = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        try {
            insertFixtures(jdbc, roleId, users);
            insertAssignment(jdbc, assignments.get(0), users.get(0), roleId);
            insertAssignment(jdbc, assignments.get(1), users.get(1), roleId);

            var guard = new JdbcLastEffectiveDevOpsGuard(new JdbcRootPolicyRepository(jdbc), new AvailableAuditWriter());
            var transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

            int successfulRemovals = runConcurrent(transaction, List.of(
                    () -> {
                        guard.assertCanRemoveDevOps();
                        revokeAssignment(jdbc, assignments.get(0));
                    },
                    () -> {
                        guard.assertCanRemoveDevOps();
                        revokeAssignment(jdbc, assignments.get(1));
                    }));
            assertEquals(1, successfulRemovals);
            assertEquals(1, effectiveDevOpsCount(jdbc));

            reactivateAssignment(jdbc, assignments.get(0));
            reactivateAssignment(jdbc, assignments.get(1));
            assertEquals(2, effectiveDevOpsCount(jdbc));

            int successfulAdditions = runConcurrent(transaction, List.of(
                    () -> {
                        guard.assertCanAddDevOps();
                        insertAssignment(jdbc, assignments.get(2), users.get(2), roleId);
                    },
                    () -> {
                        guard.assertCanAddDevOps();
                        insertAssignment(jdbc, assignments.get(3), users.get(3), roleId);
                    }));
            assertEquals(1, successfulAdditions);
            assertEquals(3, effectiveDevOpsCount(jdbc));
        } finally {
            for (UUID assignment : assignments) {
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id = ?", assignment);
            }
            for (UUID user : users) {
                jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", user);
            }
            for (UUID user : users) {
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", user);
            }
            jdbc.update("DELETE FROM spectra_security.sec_role WHERE id = ?", roleId);
        }
    }

    private static void insertFixtures(JdbcTemplate jdbc, UUID roleId, List<UUID> users) {
        jdbc.update("""
                INSERT INTO spectra_security.sec_role (id, code, name, authority_level, state, role_kind)
                VALUES (?, 'ROLE_DEV_OPS', 'Concurrency Test Root', 100, 'ACTIVE', 'DEV_OPS')
                """, roleId);
        for (UUID user : users) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, status) VALUES (?, ?, 'ACTIVE')", user,
                    "root-concurrency-" + user);
            jdbc.update("""
                    INSERT INTO spectra_security.sec_authentication_identity
                        (id, user_id, method_code, provider_code, identifier_hash, state)
                    VALUES (?, ?, 'PASSWORD', 'LOCAL', ?, 'ACTIVE')
                    """, UUID.randomUUID(), user, "hash-" + user);
        }
    }

    private static void insertAssignment(JdbcTemplate jdbc, UUID assignmentId, UUID userId, UUID roleId) {
        jdbc.update("""
                INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id, state)
                VALUES (?, ?, ?, 'ACTIVE')
                """, assignmentId, userId, roleId);
    }

    private static void revokeAssignment(JdbcTemplate jdbc, UUID assignmentId) {
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET state = 'REVOKED' WHERE id = ?", assignmentId);
    }

    private static void reactivateAssignment(JdbcTemplate jdbc, UUID assignmentId) {
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET state = 'ACTIVE' WHERE id = ?", assignmentId);
    }

    private static long effectiveDevOpsCount(JdbcTemplate jdbc) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT assignment.user_id)
                FROM spectra_security.sec_role_assignment assignment
                JOIN spectra_security.sec_role role ON role.id = assignment.role_id
                JOIN spectra_core.sys_user user_account ON user_account.id = assignment.user_id
                WHERE role.code = 'ROLE_DEV_OPS'
                  AND role.state = 'ACTIVE'
                  AND assignment.state = 'ACTIVE'
                  AND user_account.status = 'ACTIVE'
                  AND EXISTS (
                      SELECT 1 FROM spectra_security.sec_authentication_identity identity
                      WHERE identity.user_id = assignment.user_id AND identity.state = 'ACTIVE'
                  )
                """, Long.class);
        return count == null ? 0 : count;
    }

    private static int runConcurrent(TransactionTemplate transaction, List<Runnable> operations) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(operations.size());
        CountDownLatch ready = new CountDownLatch(operations.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (Runnable operation : operations) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(30, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("并发 Root 测试未能同步启动");
                    }
                    try {
                        transaction.executeWithoutResult(_ -> operation.run());
                        return true;
                    } catch (RootGovernanceException _) {
                        return false;
                    }
                }));
            }
            assertTrue(ready.await(30, TimeUnit.SECONDS));
            start.countDown();
            int successful = 0;
            for (Future<Boolean> future : futures) {
                try {
                    if (future.get(30, TimeUnit.SECONDS)) {
                        successful++;
                    }
                } catch (ExecutionException exception) {
                    throw new AssertionError("并发 Root 操作出现非预期异常", exception.getCause());
                }
            }
            return successful;
        } finally {
            executor.shutdownNow();
        }
    }

    private record DatabaseConfig(String url, String username, String password) {

        private static DatabaseConfig from(String prefix) {
            String url = environment(prefix + "URL");
            String username = environment(prefix + "USERNAME");
            String password = environment(prefix + "PASSWORD");
            assumeTrue(!url.isBlank() && !username.isBlank(), "未提供专用 PostgreSQL 集成测试连接信息");
            return new DatabaseConfig(url, username, password);
        }
    }

    private static String environment(String name) {
        return System.getenv().getOrDefault(name, "").trim();
    }

    private static final class AvailableAuditWriter implements SecurityAuditWriter {

        @Override
        public void assertAvailable() {
        }

        @Override
        public void append(SecurityAuditEvent event) {
        }
    }
}
