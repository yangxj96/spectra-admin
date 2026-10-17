package com.devops00.spectra.core.security.root;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.mapper.AuthenticationIdentityMapper;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.impl.AuthenticationIdentityServiceImpl;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.security.initialization.constant.SystemStateKeys;
import com.devops00.spectra.core.security.initialization.javabean.entity.SystemState;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationCompleteFrom;
import com.devops00.spectra.core.security.initialization.mapper.SystemStateMapper;
import com.devops00.spectra.core.security.initialization.service.SystemSecretInitializer;
import com.devops00.spectra.core.security.initialization.service.impl.SystemInitializationServiceImpl;
import com.devops00.spectra.core.security.initialization.service.impl.SystemInitializationTokenManager;
import com.devops00.spectra.core.security.root.repository.JdbcRootPolicyRepository;
import com.devops00.spectra.core.security.root.service.impl.JdbcLastEffectiveDevOpsGuard;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void rootPolicyVersionConflictKeepsItsDomainCode() {
        var policy = repository.lock();
        var conflict = assertThrows(RootGovernanceConflictException.class,
                () -> repository.update(policy, policy.version() + 1));
        assertEquals("ROOT_POLICY_VERSION_CONFLICT", conflict.getErrorCode());
        assertEquals(policy.version(), repository.lock().version());
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
        var conflict = assertThrows(RootGovernanceConflictException.class, () -> disableWithGuard(secondRoot));
        assertEquals("ROOT_EFFECTIVE_MINIMUM", conflict.getErrorCode());
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
        var conflict = assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            var before = guard.lockForChange();
            addRoot();
            guard.assertWithinLimits(before);
            return null;
        }));
        assertEquals("ROOT_EFFECTIVE_MAXIMUM", conflict.getErrorCode());
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

    @Test
    void initializationCompleteActivatesRootWithinLimit() {
        UUID pendingUser = addPendingRoot();

        transactions.execute(status -> {
            completeInitialization(pendingUser);
            return null;
        });

        assertEquals(2L, repository.countEffectiveDevOpsUsers());
        assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM spectra_core.sys_user WHERE id = ?",
                String.class, pendingUser));
        assertEquals(1L, assignmentCount(pendingUser));
    }

    @Test
    void initializationCompleteCannotExceedRootLimitAndRollsBack() {
        addRoot();
        addRoot();
        UUID pendingUser = addPendingRoot();

        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            completeInitialization(pendingUser);
            return null;
        }));

        assertEquals(3L, repository.countEffectiveDevOpsUsers());
        assertEquals("LOCKED", jdbc.queryForObject("SELECT status FROM spectra_core.sys_user WHERE id = ?",
                String.class, pendingUser));
        assertEquals(0L, assignmentCount(pendingUser));
    }

    @Test
    void identityServiceKeepsTheLastRootIdentity() {
        UUID identityId = jdbc.queryForObject("""
                SELECT id FROM spectra_security.sec_authentication_identity WHERE user_id = ?
                """, UUID.class, userId);
        var service = identityService(identityId);

        assertThrows(RootGovernanceConflictException.class, () -> transactions.execute(status -> {
            service.revokeByUserIdAndId(userId, identityId);
            return null;
        }));

        assertEquals("ACTIVE", identityState(identityId));
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
    }

    @Test
    void identityServiceCanRevokeRedundantRootIdentity() {
        UUID identityId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO spectra_security.sec_authentication_identity (id, user_id, method_code, provider_code, state)
                VALUES (?, ?, 'SMS', 'LOCAL', 'ACTIVE')
                """, identityId, userId);
        var service = identityService(identityId);

        transactions.execute(status -> {
            service.revokeByUserIdAndId(userId, identityId);
            return null;
        });

        assertEquals("REVOKED", identityState(identityId));
        assertEquals(1L, repository.countEffectiveDevOpsUsers());
    }

    private AuthenticationIdentityServiceImpl identityService(UUID identityId) {
        var mapper = mock(AuthenticationIdentityMapper.class);
        var identity = new AuthenticationIdentity();
        identity.setId(identityId);
        identity.setUserId(userId);
        identity.setState("ACTIVE");
        when(mapper.selectOne(any())).thenReturn(identity);
        when(mapper.updateById(any(AuthenticationIdentity.class))).thenAnswer(call -> {
            AuthenticationIdentity changed = call.getArgument(0);
            return jdbc.update("UPDATE spectra_security.sec_authentication_identity SET state = ? WHERE id = ?",
                    changed.getState(), changed.getId());
        });
        return new AuthenticationIdentityServiceImpl(mapper, guard);
    }

    private String identityState(UUID identityId) {
        return jdbc.queryForObject("SELECT state FROM spectra_security.sec_authentication_identity WHERE id = ?",
                String.class, identityId);
    }

    private void completeInitialization(UUID pendingUser) {
        UUID initializationId = UUID.randomUUID();
        var state = new SystemState();
        state.setState(SystemStateKeys.INITIALIZING);
        state.setInitializationId(initializationId);
        var stateMapper = mock(SystemStateMapper.class);
        when(stateMapper.selectForUpdateByStateKey(SystemStateKeys.SYSTEM)).thenReturn(state);
        when(stateMapper.updateById(any(SystemState.class))).thenReturn(1);

        var user = new User();
        user.setId(pendingUser);
        user.setStatus(UserStatus.LOCKED);
        var userMapper = mock(UserMapper.class);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenAnswer(call -> {
            User changed = call.getArgument(0);
            return jdbc.update("UPDATE spectra_core.sys_user SET status = ? WHERE id = ?",
                    changed.getStatus().getCode(), changed.getId());
        });

        UUID roleId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role LIMIT 1", UUID.class);
        var role = new SecurityRole();
        role.setId(roleId);
        role.setCode("ROLE_DEV_OPS");
        role.setState("ACTIVE");
        var roleMapper = mock(SecurityRoleMapper.class);
        when(roleMapper.selectOne(any())).thenReturn(role);
        var assignmentMapper = mock(RoleAssignmentMapper.class);
        when(assignmentMapper.insert(any(RoleAssignment.class))).thenAnswer(call -> {
            RoleAssignment assignment = call.getArgument(0);
            return jdbc.update("""
                    INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id, state)
                    VALUES (?, ?, ?, ?)
                    """, UUID.randomUUID(), assignment.getUserId(), assignment.getRoleId(), assignment.getState());
        });

        var service = new SystemInitializationServiceImpl(stateMapper, userMapper, mock(ConfiguredService.class),
                roleMapper, assignmentMapper, mock(AuthorizationAssignmentChangeService.class),
                mock(AuthenticationIdentityService.class), mock(PasswordCredentialService.class),
                mock(SystemInitializationTokenManager.class), mock(SecurityPasswordPolicyProvider.class),
                mock(PasswordEncoder.class), jdbc, mock(SystemSecretInitializer.class), guard);
        var request = new SystemInitializationCompleteFrom();
        request.setInitializationId(initializationId.toString());
        service.complete(request);
    }

    private UUID addPendingRoot() {
        UUID pendingUser = UUID.randomUUID();
        jdbc.update("INSERT INTO spectra_core.sys_user (id, status) VALUES (?, 'LOCKED')", pendingUser);
        jdbc.update("""
                INSERT INTO spectra_security.sec_authentication_identity (id, user_id, method_code, provider_code, state)
                VALUES (?, ?, 'PASSWORD', 'LOCAL', 'ACTIVE')
                """, UUID.randomUUID(), pendingUser);
        jdbc.update("""
                INSERT INTO spectra_security.sec_password_credential (id, user_id, password_hash, changed_at)
                VALUES (?, ?, 'test-hash', CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), pendingUser);
        return pendingUser;
    }

    private long assignmentCount(UUID target) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM spectra_security.sec_role_assignment WHERE user_id = ?", Long.class, target);
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
