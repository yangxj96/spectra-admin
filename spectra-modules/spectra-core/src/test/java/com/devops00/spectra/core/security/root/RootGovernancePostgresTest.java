package com.devops00.spectra.core.security.root;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.security.root.repository.JdbcRootPolicyRepository;
import com.devops00.spectra.core.security.root.service.impl.JdbcLastEffectiveDevOpsGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RootGovernancePostgresTest {

    private final DriverManagerDataSource dataSource = testDataSource();

    private final JdbcTemplate jdbc = new JdbcTemplate(dataSource);

    private final JdbcRootPolicyRepository repository = new JdbcRootPolicyRepository(jdbc);

    private final JdbcLastEffectiveDevOpsGuard guard = new JdbcLastEffectiveDevOpsGuard(repository, mock(AuditService.class));

    private final TransactionTemplate transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

    private UUID userId;

    private static DriverManagerDataSource testDataSource() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 DB_USERNAME");
        }
        return new DriverManagerDataSource(url, username, System.getenv("DB_PASSWORD"));
    }

    @BeforeEach
    void prepare() {
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS spectra_security");
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS spectra_core");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_root_policy (
                    policy_key text PRIMARY KEY, min_effective_dev_ops_users int, max_dev_ops_users int, version bigint)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_password_policy (
                    policy_key text PRIMARY KEY, max_age_days int, deleted timestamptz)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_core.sys_user (
                    id uuid PRIMARY KEY, status text, deleted timestamptz)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_role (
                    id uuid PRIMARY KEY, code text, state text, deleted timestamptz)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_role_assignment (
                    id uuid PRIMARY KEY, user_id uuid, role_id uuid, state text,
                    valid_from timestamptz, valid_until timestamptz, deleted timestamptz)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_authentication_identity (
                    id uuid PRIMARY KEY, user_id uuid, method_code text, provider_code text, state text,
                    deleted timestamptz)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS spectra_security.sec_password_credential (
                    id uuid PRIMARY KEY, user_id uuid, password_hash text, changed_at timestamptz,
                    expires_at timestamptz, locked_until timestamptz, deleted timestamptz)
                """);
        jdbc.execute("ALTER TABLE spectra_security.sec_root_policy ADD COLUMN IF NOT EXISTS deleted timestamptz");
        jdbc.execute("ALTER TABLE spectra_security.sec_password_policy ADD COLUMN IF NOT EXISTS deleted timestamptz");
        jdbc.execute("""
                TRUNCATE spectra_security.sec_root_policy, spectra_security.sec_password_policy,
                    spectra_core.sys_user, spectra_security.sec_role, spectra_security.sec_role_assignment,
                    spectra_security.sec_authentication_identity, spectra_security.sec_password_credential
                """);
        jdbc.update("INSERT INTO spectra_security.sec_root_policy VALUES ('SYSTEM', 1, 3, 0)");
        jdbc.update("INSERT INTO spectra_security.sec_password_policy VALUES ('SYSTEM', NULL, NULL)");
        userId = addRoot();
    }

    @Test
    void countsOnlyCurrentlyLoginCapableRoot() {
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_core.sys_user SET deleted = CURRENT_TIMESTAMP WHERE id = ?", userId);
        assertEquals(0L, repository.countEffectiveDevOpsUsers(), "soft deleted Root cannot log in");
    }

    @Test
    void ignoresFutureAssignmentAndExpiredCredential() {
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_from = CURRENT_TIMESTAMP + INTERVAL '1 day'");
        assertEquals(0L, repository.countEffectiveDevOpsUsers(), "future assignment is not effective");
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_from = NULL");
        jdbc.update("UPDATE spectra_security.sec_password_credential SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 day'");
        assertEquals(0L, repository.countEffectiveDevOpsUsers(), "expired credential cannot log in");
    }

    @Test
    void excludesInactiveRoleAssignmentAndIdentityRows() {
        jdbc.update("UPDATE spectra_security.sec_role SET deleted = CURRENT_TIMESTAMP");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_role SET deleted = NULL");
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET deleted = CURRENT_TIMESTAMP");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET deleted = NULL");
        jdbc.update("UPDATE spectra_security.sec_authentication_identity SET deleted = CURRENT_TIMESTAMP");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_authentication_identity SET deleted = NULL");
        jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_until = CURRENT_TIMESTAMP - INTERVAL '1 day'");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void appliesPasswordAgeAndRejectsUnknownPolicy() {
        jdbc.update("UPDATE spectra_security.sec_password_policy SET max_age_days = 30");
        jdbc.update("UPDATE spectra_security.sec_password_credential SET changed_at = CURRENT_TIMESTAMP - INTERVAL '31 days'");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_password_credential SET changed_at = CURRENT_TIMESTAMP + INTERVAL '1 day'");
        assertEquals(0L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_password_credential SET changed_at = CURRENT_TIMESTAMP - INTERVAL '1 day'");
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
        jdbc.update("UPDATE spectra_security.sec_password_policy SET deleted = CURRENT_TIMESTAMP");
        assertThrows(RootGovernanceException.class, repository::countEffectiveDevOpsUsers);
    }

    @Test
    void permitsOneOfTwoRootsToBeDisabledAndRollsBackLastRootChange() {
        UUID secondRoot = addRoot();
        disableWithGuard(userId);
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
        assertThrows(RootGovernanceConflictException.class, () -> disableWithGuard(secondRoot));
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
        assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM spectra_core.sys_user WHERE id = ?",
                String.class, secondRoot));
    }

    @Test
    void refusesRevokingTheLastRootAssignmentOrIdentity() {
        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_security.sec_role_assignment SET state = 'REVOKED' WHERE user_id = ?", userId);
            guard.assertWithinLimits(before);
            return null;
        }));
        assertEquals(1L, repository.countEffectiveDevOpsUsers(), "assignment revocation must roll back");

        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_security.sec_authentication_identity SET state = 'REVOKED' WHERE user_id = ?", userId);
            guard.assertWithinLimits(before);
            return null;
        }));
        assertEquals(1L, repository.countEffectiveDevOpsUsers(), "identity revocation must roll back");
    }

    @Test
    void allowsRevokingOneOfTwoRootAssignmentsForTheSameUser() {
        UUID roleId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                UUID.class);
        jdbc.update("""
                INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id, state)
                VALUES (?, ?, ?, 'ACTIVE')
                """, UUID.randomUUID(), userId, roleId);
        UUID assignmentId = jdbc.queryForObject("""
                SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ? ORDER BY id LIMIT 1
                """, UUID.class, userId);
        transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_security.sec_role_assignment SET state = 'REVOKED' WHERE id = ?", assignmentId);
            guard.assertWithinLimits(before);
            return null;
        });
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void rejectsPasswordPolicyThatWouldExpireTheLastRoot() {
        jdbc.update("UPDATE spectra_security.sec_password_credential SET changed_at = CURRENT_TIMESTAMP - INTERVAL '31 days'");
        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_security.sec_password_policy SET max_age_days = 30");
            guard.assertWithinLimits(before);
            return null;
        }));
        assertEquals(1L, repository.countEffectiveDevOpsUsers(), "password policy update must roll back");
    }

    @Test
    void rejectsAddingAFourthEffectiveRoot() {
        addRoot();
        addRoot();
        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            var before = guard.lockForChange();
            addRoot();
            guard.assertWithinLimits(before);
            return null;
        }));
        assertEquals(3L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void permitsRestoringIdentityWithinRootLimit() {
        addRoot();
        UUID dormantRoot = addDormantRoot();
        activateIdentityWithGuard(dormantRoot);
        assertEquals(3L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void rejectsRestoringIdentityAboveRootLimitAndRollsBack() {
        addRoot();
        addRoot();
        UUID dormantRoot = addDormantRoot();
        assertThrows(RootGovernanceConflictException.class, () -> activateIdentityWithGuard(dormantRoot));
        assertEquals(3L, repository.countEffectiveDevOpsUsers());
        assertEquals("REVOKED", jdbc.queryForObject("""
                SELECT state FROM spectra_security.sec_authentication_identity WHERE user_id = ?
                """, String.class, dormantRoot));
    }

    @Test
    void serializesConcurrentIdentityRestoresAtRootLimit() throws Exception {
        addRoot();
        UUID firstDormant = addDormantRoot();
        UUID secondDormant = addDormantRoot();
        var start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> tryActivateIdentityAfter(start, firstDormant));
            var second = workers.submit(() -> tryActivateIdentityAfter(start, secondDormant));
            start.countDown();
            assertEquals(1L, List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS))
                    .stream()
                    .filter(Boolean::booleanValue)
                    .count());
        }
        assertEquals(3L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void serializesConcurrentAttemptsToDisableBothRoots() throws Exception {
        UUID secondRoot = addRoot();
        var start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> tryDisableAfter(start, userId));
            var second = workers.submit(() -> tryDisableAfter(start, secondRoot));
            start.countDown();
            assertEquals(1L, List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS))
                    .stream()
                    .filter(Boolean::booleanValue)
                    .count());
        }
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void refusesRootLockWithoutCallerTransaction() {
        assertThrows(RootGovernanceException.class, guard::lockForChange);
    }

    private boolean tryDisableAfter(CountDownLatch start, UUID target) throws InterruptedException {
        start.await();
        try {
            disableWithGuard(target);
            return true;
        } catch (RootGovernanceConflictException expected) {
            return false;
        }
    }

    private boolean tryActivateIdentityAfter(CountDownLatch start, UUID target) throws InterruptedException {
        start.await();
        try {
            activateIdentityWithGuard(target);
            return true;
        } catch (RootGovernanceConflictException expected) {
            return false;
        }
    }

    private void disableWithGuard(UUID target) {
        transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_core.sys_user SET status = 'DISABLED' WHERE id = ?", target);
            guard.assertWithinLimits(before);
            return null;
        });
    }

    private void activateIdentityWithGuard(UUID target) {
        transactions.execute(status -> {
            var before = guard.lockForChange();
            jdbc.update("UPDATE spectra_security.sec_authentication_identity SET state = 'ACTIVE' WHERE user_id = ?", target);
            guard.assertWithinLimits(before);
            return null;
        });
    }

    private UUID addDormantRoot() {
        UUID user = addRoot();
        jdbc.update("UPDATE spectra_security.sec_authentication_identity SET state = 'REVOKED' WHERE user_id = ?", user);
        return user;
    }

    private UUID addRoot() {
        UUID user = UUID.randomUUID();
        UUID role = UUID.randomUUID();
        jdbc.update("INSERT INTO spectra_core.sys_user (id, status) VALUES (?, 'ACTIVE')", user);
        jdbc.update("INSERT INTO spectra_security.sec_role (id, code, state) VALUES (?, 'ROLE_DEV_OPS', 'ACTIVE')", role);
        jdbc.update("""
                INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id, state)
                VALUES (?, ?, ?, 'ACTIVE')
                """, UUID.randomUUID(), user, role);
        jdbc.update("""
                INSERT INTO spectra_security.sec_authentication_identity (id, user_id, method_code, provider_code, state)
                VALUES (?, ?, 'PASSWORD', 'LOCAL', 'ACTIVE')
                """, UUID.randomUUID(), user);
        jdbc.update("""
                INSERT INTO spectra_security.sec_password_credential (id, user_id, password_hash, changed_at)
                VALUES (?, ?, 'test-hash', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), user);
        return user;
    }
}
