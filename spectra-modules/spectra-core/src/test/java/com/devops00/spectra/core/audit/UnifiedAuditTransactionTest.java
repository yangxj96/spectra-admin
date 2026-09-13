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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.audit.service.impl.CoreAuditService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code UnifiedAuditTransactionTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Tag("integration")
@Testcontainers
class UnifiedAuditTransactionTest {

    private static final String AUDIT_TABLE = "spectra_core.sys_audit_event";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_unified_audit_tx")
            .withUsername("postgres")
            .withPassword("integration");

    private static DriverManagerDataSource dataSource;
    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateAndCreateProbe() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();
        dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE spectra_core.audit_transaction_probe (id uuid PRIMARY KEY)");
        jdbc.execute("""
                CREATE FUNCTION spectra_core.reject_test_audit_write() RETURNS trigger
                LANGUAGE plpgsql AS $$
                BEGIN
                    IF NEW.event_type = 'TEST.AUDIT.WRITE.FAIL' THEN
                        RAISE EXCEPTION 'forced audit writer failure';
                    END IF;
                    RETURN NEW;
                END;
                $$
                """);
        jdbc.execute("CREATE TRIGGER trg_test_audit_write_failure BEFORE INSERT ON " + AUDIT_TABLE
                + " FOR EACH ROW EXECUTE FUNCTION spectra_core.reject_test_audit_write()");
    }

    @BeforeEach
    void clearProbeAndSecurityContext() {
        jdbc.update("DELETE FROM spectra_core.audit_transaction_probe");
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulAuditAndBusinessWriteCommitTogether() throws Throwable {
        UUID businessId = UUID.randomUUID();
        var aspect = aspect();
        var point = invocation("successfulOperation", () -> {
            jdbc.update("INSERT INTO spectra_core.audit_transaction_probe(id) VALUES (?)", businessId);
            return "completed";
        });

        assertEquals("completed", aspect.handleAround(point));
        assertEquals(1, probeCount(businessId));
        assertEquals(1, auditCount("TEST.AUDIT.SUCCESS"));
    }

    @Test
    void auditWriterFailureRollsBackTheBusinessTransaction() throws Throwable {
        UUID businessId = UUID.randomUUID();
        var aspect = aspect();
        var point = invocation("auditWriterFailure", () -> {
            jdbc.update("INSERT INTO spectra_core.audit_transaction_probe(id) VALUES (?)", businessId);
            return "must roll back";
        });

        RuntimeException failure = assertThrows(RuntimeException.class, () -> aspect.handleAround(point));
        assertInstanceOf(AuditService.AuditRecordingException.class, failure);
        assertEquals(0, probeCount(businessId));
        assertEquals(0, auditCount("TEST.AUDIT.WRITE.FAIL"));
    }

    @Test
    void thrownBusinessFailureRollsBackBusinessWriteAndCommitsSanitizedFailureAudit() throws Throwable {
        UUID businessId = UUID.randomUUID();
        var aspect = aspect();
        var original = new IllegalArgumentException("invalid access_token=private-value");
        var point = invocation("businessFailure", () -> {
            jdbc.update("INSERT INTO spectra_core.audit_transaction_probe(id) VALUES (?)", businessId);
            throw original;
        });

        Throwable thrown = assertThrows(Throwable.class, () -> aspect.handleAround(point));

        assertTrue(thrown == original);
        assertEquals(0, probeCount(businessId));
        var row = jdbc.queryForMap("SELECT result, failure_code, failure_type, failure_reason FROM " + AUDIT_TABLE
                + " WHERE event_type = 'TEST.AUDIT.BUSINESS.FAIL'");
        assertEquals("FAILED", row.get("result"));
        assertEquals("INVALID_ARGUMENT", row.get("failure_code"));
        assertEquals("IllegalArgumentException", row.get("failure_type"));
        assertEquals("invalid access_token=[REDACTED]", row.get("failure_reason"));
    }

    /**
     * 处理操作相关数据。
     */
    @Audit(eventType = "TEST.AUDIT.SUCCESS", captureArguments = false, captureResult = false)
    private void successfulOperation() {
    }

    /**
     * 处理审计失败相关数据。
     */
    @Audit(eventType = "TEST.AUDIT.WRITE.FAIL", captureArguments = false, captureResult = false)
    private void auditWriterFailure() {
    }

    /**
     * 处理业务失败相关数据。
     */
    @Audit(eventType = "TEST.AUDIT.BUSINESS.FAIL", captureArguments = false, captureResult = false)
    private void businessFailure() {
    }

    /**
     * 处理审计事务相关数据。
     */
    private static AuditAspect aspect() {
        var transactionManager = new DataSourceTransactionManager(dataSource);
        var auditSanitizer = new DefaultAuditSanitizer();
        AuditService service = new CoreAuditService(
                new com.devops00.spectra.core.audit.repository.JdbcAuditEventWriter(jdbc, new ObjectMapper()),
                auditSanitizer);
        var failureRecorder = new AuditFailureRecorder(service, transactionManager);
        return new AuditAspect(mock(SecurityContextAccessor.class), service, auditSanitizer,
                new TransactionTemplate(transactionManager), new AuditFailureResolver(auditSanitizer), failureRecorder);
    }

    /**
     * 处理审计事务相关数据。
     */
    private static ProceedingJoinPoint invocation(String methodName, ThrowingSupplier work) throws Throwable {
        Method method = UnifiedAuditTransactionTest.class.getDeclaredMethod(methodName);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[0]);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[0]);
        doAnswer(ignored -> work.get()).when(point).proceed();
        return point;
    }

    /**
     * 处理统计相关数据。
     */
    private static int probeCount(UUID id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM spectra_core.audit_transaction_probe WHERE id = ?",
                Integer.class, id);
        return count == null ? 0 : count;
    }

    /**
     * 处理审计统计相关数据。
     */
    private static int auditCount(String eventType) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + AUDIT_TABLE + " WHERE event_type = ?",
                Integer.class, eventType);
        return count == null ? 0 : count;
    }

    /**
     * 为 {@code UnifiedAuditTransactionTest} 测试提供 {@code ThrowingSupplier} 测试类型。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    @FunctionalInterface
    private interface ThrowingSupplier {

        /**
         * 查询相关数据。
         *
         * @return 处理后的结果。
         */
        Object get();
    }
}
