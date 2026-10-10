package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** 在专用合成库验证真实审计切面、JSONB 及本地事务边界。 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class AuditTransactionPostgresTest {
    private JdbcTemplate jdbc;
    private FixtureApplication application;
    private final AtomicBoolean rejectAudit = new AtomicBoolean();

    @BeforeEach
    void setUp() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 DB_USERNAME");
        }
        var source = new DriverManagerDataSource(url, username, System.getenv("DB_PASSWORD"));
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE IF NOT EXISTS b02_audit_business (marker text)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS b02_audit_records (payload jsonb)");
        jdbc.execute("TRUNCATE b02_audit_business, b02_audit_records");
        var mapper = JsonMapper.builder().build();
        var sanitizer = new JacksonAuditSanitizer(mapper);
        AuditService audit = record -> {
            if (rejectAudit.get()) {
                throw new AuditService.AuditRecordingException("synthetic audit storage rejection");
            }
            var payload = Map.of("before", record.before(), "after", record.after(), "result", record.result(),
                    "failure", record.failure() == null ? Map.of() : record.failure());
            jdbc.update("INSERT INTO b02_audit_records(payload) VALUES (CAST(? AS jsonb))",
                    mapper.writeValueAsString(payload));
        };
        var transactions = new DataSourceTransactionManager(source);
        var aspect = new AuditAspect(mock(SecurityContextAccessor.class), audit, sanitizer,
                new TransactionTemplate(transactions), new AuditFailureResolver(sanitizer),
                new AuditFailureRecorder(audit, transactions));
        var proxy = new AspectJProxyFactory(new FixtureApplication(jdbc));
        proxy.addAspect(aspect);
        application = proxy.getProxy();
    }

    @Test
    void committedJsonbContainsSafeFormAndResultSnapshots() {
        application.succeed(form());
        assertEquals(1, count("b02_audit_business"));
        var payload = jdbc.queryForObject("SELECT payload::text FROM b02_audit_records", String.class);
        assertNotNull(payload);
        assertFalse(payload.contains("synthetic-secret"));
        assertTrue(payload.contains("visible-result"));
        assertTrue(payload.contains(com.devops00.spectra.common.audit.AuditSanitizer.REDACTED_VALUE));
    }

    @Test
    void auditFailureRollsBackBusinessMutation() {
        rejectAudit.set(true);
        assertThrows(AuditService.AuditRecordingException.class, () -> application.succeed(form()));
        assertEquals(0, count("b02_audit_business"));
        assertEquals(0, count("b02_audit_records"));
    }

    @Test
    void businessFailureRollsBackThenWritesSanitizedFailureIndependently() {
        assertThrows(IllegalArgumentException.class, () -> application.fail(form()));
        assertEquals(0, count("b02_audit_business"));
        var payload = jdbc.queryForObject("SELECT payload::text FROM b02_audit_records", String.class);
        assertNotNull(payload);
        assertTrue(payload.contains("FAILED"));
        assertFalse(payload.contains("synthetic-secret"));
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    private ChangePasswordFrom form() {
        return new ChangePasswordFrom("synthetic-secret-old", "synthetic-secret-new", "synthetic-secret-confirm");
    }

    public static class FixtureApplication {
        private final JdbcTemplate jdbc;
        public FixtureApplication(JdbcTemplate jdbc) { this.jdbc = jdbc; }

        @Audit(eventType = "B02_AUDIT_SUCCESS")
        public Map<String, Object> succeed(ChangePasswordFrom form) {
            jdbc.update("INSERT INTO b02_audit_business(marker) VALUES ('visible-business')");
            return Map.of("name", "visible-result", "token", "synthetic-secret-token");
        }

        @Audit(eventType = "B02_AUDIT_FAILURE")
        public void fail(ChangePasswordFrom form) {
            jdbc.update("INSERT INTO b02_audit_business(marker) VALUES ('rollback-business')");
            throw new IllegalArgumentException("password=synthetic-secret-exception");
        }
    }
}
