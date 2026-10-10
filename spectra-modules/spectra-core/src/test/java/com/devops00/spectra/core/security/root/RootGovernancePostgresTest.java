package com.devops00.spectra.core.security.root;

import com.devops00.spectra.core.security.root.repository.JdbcRootPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RootGovernancePostgresTest {

    private final JdbcTemplate jdbc = new JdbcTemplate(testDataSource());

    private final JdbcRootPolicyRepository repository = new JdbcRootPolicyRepository(jdbc);

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
