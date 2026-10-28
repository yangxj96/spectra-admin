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

package com.devops00.spectra.launch.security;

import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.security.authentication.crypto.VerificationCodeDigest;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.secret.service.SecretProvisioningService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.core.notification.dispatch.NotificationTaskWorker;
import com.devops00.spectra.core.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.core.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.core.notification.service.NotificationProviderAdminService;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用正式 HTTP、PostgreSQL 与 Redis DB 5 验证认证身份绑定验证码的用途/地址/用户绑定与原子尝试预算耗尽。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class BindingCodeBudgetFullHttpTest {

    private static final String WRONG_CODE = "111111";

    @Test
    void bindingCodeDeliveryRejectsUnavailableChannelAndRecordsOneMockDelivery() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var redis = securityRedisTemplate(application);
            var providers = application.getBean(NotificationProviderAdminService.class);
            var providerRuntime = application.getBean(NotificationProviderRuntime.class);
            var worker = application.getBean(NotificationTaskWorker.class);
            var secretManagement = application.getBean(SecretProvisioningService.class);
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            String failedEmail = uniqueEmail();
            String deliveredEmail = uniqueEmail();
            UUID requestId = null;
            List<UUID> secretVersionIds = new ArrayList<>();
            assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_config "
                    + "WHERE key = 'notification.provider.email' AND deleted IS NULL", Long.class),
                    "隔离测试库不得已有邮件 Provider 配置");
            try (HttpClient client = HttpClient.newHttpClient()) {
                for (String code : List.of("security.verification-code-hmac",
                        "notification.sensitive-payload-key", "notification.address-encryption-key")) {
                    UUID versionId = secretManagement.createPending(code, randomSecret(), "GENERATED").getId();
                    secretVersionIds.add(versionId);
                    secretManagement.publish(code, versionId);
                }
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                assertNotEquals(200, sendBindingEmailCode(client, port, mapper, token, failedEmail).statusCode(),
                        "渠道不可用时必须拒绝验证码发送");
                assertFalse(Boolean.TRUE.equals(redis.hasKey(RedisCacheKey.BIND_EMAIL_CODE + failedEmail)),
                        "入队失败后应删除刚保存的验证码");
                assertEquals(0L, bindingRequestCount(jdbc, user.id()), "拒绝路径不得留下通知请求");

                var configuration = new NotificationProviderSaveFrom();
                configuration.setProviderType("MOCK");
                configuration.setEnabled(true);
                providers.modify(NotificationChannel.EMAIL, configuration);
                assertEquals("HEALTHY", providerRuntime.check(NotificationChannel.EMAIL).state().name());
                assertEquals(200, sendBindingEmailCode(client, port, mapper, token, deliveredEmail).statusCode());
                assertTrue(Boolean.TRUE.equals(redis.hasKey(RedisCacheKey.BIND_EMAIL_CODE + deliveredEmail)));
                requestId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key LIKE ?", UUID.class, bindingRequestPrefix(user.id()));
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_task "
                        + "WHERE notification_request_id = ? AND status = 'PENDING'", Long.class, requestId));
                assertTrue(worker.processPending(1) >= 1);
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_task "
                        + "WHERE notification_request_id = ? AND status = 'SENT'", Long.class, requestId));
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery d "
                        + "JOIN spectra_notification.ntf_task t ON t.id = d.notification_task_id "
                        + "WHERE t.notification_request_id = ? AND d.provider = 'MOCK' "
                        + "AND d.result_status = 'SENT'", Long.class, requestId));
                assertEquals(0, worker.processPending(1), "已投递任务不可重复领取");
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery d "
                        + "JOIN spectra_notification.ntf_task t ON t.id = d.notification_task_id "
                        + "WHERE t.notification_request_id = ?", Long.class, requestId));
            } finally {
                redis.delete(RedisCacheKey.BIND_EMAIL_CODE + failedEmail);
                redis.delete(RedisCacheKey.BIND_EMAIL_CODE + deliveredEmail);
                if (requestId != null) {
                    jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                            + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
                }
                jdbc.update("DELETE FROM spectra_core.sys_config WHERE key = 'notification.provider.email'");
                fixture.clean();
                for (UUID versionId : secretVersionIds) {
                    jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", versionId);
                }
            }
        }
    }

    @Test
    void quartzJobDeliversBindingCodeOnceWithoutRequestIdentity() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=true", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var redis = securityRedisTemplate(application);
            var providers = application.getBean(NotificationProviderAdminService.class);
            var providerRuntime = application.getBean(NotificationProviderRuntime.class);
            var secretManagement = application.getBean(SecretProvisioningService.class);
            var schedulerLifecycle = application.getBean(QuartzSchedulerLifecycle.class);
            var scheduler = schedulerLifecycle.scheduler();
            var jobKey = new JobKey("notification.task-worker", "SPECTRA_BUILTIN");
            var triggerKey = new TriggerKey("notification.task-worker.trigger", "SPECTRA_BUILTIN");
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            String email = uniqueEmail();
            String blockedEmail = uniqueEmail();
            String recoveredEmail = uniqueEmail();
            UUID requestId = null;
            List<UUID> requestIds = new ArrayList<>();
            List<UUID> secretVersionIds = new ArrayList<>();
            Instant historyStart = null;
            assertTrue(schedulerLifecycle.isReady(), "Quartz JDBC Scheduler 必须就绪");
            assertTrue(scheduler.checkExists(jobKey), "内置通知 Worker Job 必须已注册");
            assertTrue(scheduler.checkExists(triggerKey), "内置通知 Worker Trigger 必须已注册");
            assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_config "
                    + "WHERE key = 'notification.provider.email' AND deleted IS NULL", Long.class));
            scheduler.pauseTrigger(triggerKey);
            try (HttpClient client = HttpClient.newHttpClient()) {
                for (String code : List.of("security.verification-code-hmac",
                        "notification.sensitive-payload-key", "notification.address-encryption-key")) {
                    UUID versionId = secretManagement.createPending(code, randomSecret(), "GENERATED").getId();
                    secretVersionIds.add(versionId);
                    secretManagement.publish(code, versionId);
                }
                fixture.insert(user);
                var configuration = new NotificationProviderSaveFrom();
                configuration.setProviderType("MOCK");
                configuration.setEnabled(true);
                providers.modify(NotificationChannel.EMAIL, configuration);
                assertEquals("HEALTHY", providerRuntime.check(NotificationChannel.EMAIL).state().name());
                String token = login(client, port, mapper, user.username(), user.password());
                assertEquals(200, sendBindingEmailCode(client, port, mapper, token, email).statusCode());
                requestId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key LIKE ?", UUID.class, bindingRequestPrefix(user.id()));
                requestIds.add(requestId);
                assertEquals(1L, taskStatusCount(jdbc, requestId, "PENDING"));
                historyStart = Instant.now();
                scheduler.triggerJob(jobKey);
                UUID deliveredRequestId = requestId;
                Instant startedAt = historyStart;
                assertTrue(await(Duration.ofSeconds(30), () -> taskStatusCount(jdbc, deliveredRequestId, "SENT") == 1L
                        && quartzSuccessCount(jdbc, startedAt) >= 1L), "Quartz 应完成通知投递并记录成功历史");
                assertEquals(1L, deliveryCount(jdbc, requestId));

                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> quartzSuccessCount(jdbc, startedAt) >= 2L),
                        "再次触发 Quartz Job 应产生终态历史");
                assertEquals(1L, deliveryCount(jdbc, requestId), "重复触发不得重复投递已发送任务");

                assertEquals(200, sendBindingEmailCode(client, port, mapper, token, blockedEmail).statusCode());
                UUID blockedRequestId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key LIKE ? AND id <> ?", UUID.class,
                        bindingRequestPrefix(user.id()), requestId);
                requestIds.add(blockedRequestId);
                assertEquals(1L, taskStatusCount(jdbc, blockedRequestId, "PENDING"));
                configuration.setEnabled(false);
                providers.modify(NotificationChannel.EMAIL, configuration);
                assertFalse(providerRuntime.check(NotificationChannel.EMAIL).state().name().equals("HEALTHY"));
                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> taskStatusCount(jdbc, blockedRequestId, "BLOCKED") == 1L
                        && quartzSuccessCount(jdbc, startedAt) >= 3L),
                        "入队后渠道停用时 Quartz 必须明确阻断投递");
                assertEquals(1L, deliveryStatusCount(jdbc, blockedRequestId, "BLOCKED"));

                configuration.setEnabled(true);
                providers.modify(NotificationChannel.EMAIL, configuration);
                assertEquals("HEALTHY", providerRuntime.check(NotificationChannel.EMAIL).state().name());
                assertEquals(200, sendBindingEmailCode(client, port, mapper, token, recoveredEmail).statusCode());
                UUID recoveredRequestId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key LIKE ? AND id NOT IN (?, ?)", UUID.class,
                        bindingRequestPrefix(user.id()), requestId, blockedRequestId);
                requestIds.add(recoveredRequestId);
                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> taskStatusCount(jdbc, recoveredRequestId, "SENT") == 1L
                        && quartzSuccessCount(jdbc, startedAt) >= 4L), "渠道恢复后新通知应成功投递");
                assertEquals(1L, deliveryStatusCount(jdbc, recoveredRequestId, "SENT"));
                assertEquals(1L, deliveryCount(jdbc, blockedRequestId), "阻断任务不得在恢复后自动重发");
            } finally {
                scheduler.resumeTrigger(triggerKey);
                redis.delete(RedisCacheKey.BIND_EMAIL_CODE + email);
                redis.delete(RedisCacheKey.BIND_EMAIL_CODE + blockedEmail);
                redis.delete(RedisCacheKey.BIND_EMAIL_CODE + recoveredEmail);
                for (UUID fixtureRequestId : requestIds) {
                    jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                            + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", fixtureRequestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", fixtureRequestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", fixtureRequestId);
                }
                if (historyStart != null) {
                    jdbc.update("DELETE FROM spectra_core.quartz_job_execution_history "
                            + "WHERE job_type = 'notification.task-worker' AND started_at >= ?",
                            Timestamp.from(historyStart));
                }
                jdbc.update("DELETE FROM spectra_core.sys_config WHERE key = 'notification.provider.email'");
                fixture.clean();
                for (UUID versionId : secretVersionIds) {
                    jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", versionId);
                }
            }
        }
    }

    private static long taskStatusCount(JdbcTemplate jdbc, UUID requestId, String status) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_task "
                + "WHERE notification_request_id = ? AND status = ?", Long.class, requestId, status);
    }

    private static long deliveryCount(JdbcTemplate jdbc, UUID requestId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery d "
                + "JOIN spectra_notification.ntf_task t ON t.id = d.notification_task_id "
                + "WHERE t.notification_request_id = ?", Long.class, requestId);
    }

    private static long deliveryStatusCount(JdbcTemplate jdbc, UUID requestId, String status) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery d "
                + "JOIN spectra_notification.ntf_task t ON t.id = d.notification_task_id "
                + "WHERE t.notification_request_id = ? AND d.result_status = ?", Long.class, requestId, status);
    }

    private static long quartzSuccessCount(JdbcTemplate jdbc, Instant since) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_core.quartz_job_execution_history "
                + "WHERE job_type = 'notification.task-worker' AND status = 'SUCCEEDED' "
                + "AND started_at >= ?", Long.class, Timestamp.from(since));
    }

    private static boolean await(Duration timeout, java.util.function.BooleanSupplier condition)
            throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(100);
        }
        return condition.getAsBoolean();
    }

    private static long bindingRequestCount(JdbcTemplate jdbc, UUID userId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                + "WHERE idempotency_key LIKE ?", Long.class, bindingRequestPrefix(userId));
    }

    private static String bindingRequestPrefix(UUID userId) {
        return "security:bind-email-code:EMAIL:" + userId + ":%";
    }

    private static HttpResponse<String> sendBindingEmailCode(HttpClient client, int port, ObjectMapper mapper,
                                                             String token, String email)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/authentication/bind/email"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("email", email))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void bindingCodeIsBoundByAddressAndExhaustsAtomically() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var codes = application.getBean(SecurityVerificationCodeStore.class);
            var secrets = application.getBean(SecretRuntimeService.class);
            var secretManagement = application.getBean(SecretProvisioningService.class);
            var properties = application.getBean(SecurityProperties.class);
            var redis = securityRedisTemplate(application);
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            int maxAttempts = properties.getVerificationCodeMaxAttempts();
            UUID secretVersionId = null;
            try (HttpClient client = HttpClient.newHttpClient()) {
                secretVersionId = secretManagement.createPending("security.verification-code-hmac",
                        randomSecret(), "GENERATED").getId();
                secretManagement.publish("security.verification-code-hmac", secretVersionId);
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());

                String emailA = uniqueEmail();
                String codeA = "246813";
                seedBindingCode(codes, secrets, emailA, user.id(), codeA);
                for (int attempt = 0; attempt < maxAttempts - 1; attempt++) {
                    assertEquals(400, bindEmail(client, port, mapper, token, emailA, WRONG_CODE).statusCode(),
                            "错误验证码应被拒绝");
                }
                assertEquals(200, bindEmail(client, port, mapper, token, emailA, codeA).statusCode(),
                        "预算内的正确验证码应绑定成功");
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_authentication_identity "
                        + "WHERE user_id = ? AND method_code = 'EMAIL' AND identifier_hash = ?", Long.class,
                        user.id(), AuthenticationIdentifierHash.digest(emailA)));

                String emailB = uniqueEmail();
                String codeB = "975310";
                seedBindingCode(codes, secrets, emailB, user.id(), codeB);
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    assertEquals(400, bindEmail(client, port, mapper, token, emailB, WRONG_CODE).statusCode());
                }
                assertFalse(Boolean.TRUE.equals(redis.hasKey(RedisCacheKey.BIND_EMAIL_CODE + emailB)),
                        "达到预算后验证码应被原子失效");
                assertEquals(400, bindEmail(client, port, mapper, token, emailB, codeB).statusCode(),
                        "耗尽后即使正确验证码也应被拒绝");

                String emailC = uniqueEmail();
                String emailD = uniqueEmail();
                String codeC = "864209";
                seedBindingCode(codes, secrets, emailC, user.id(), codeC);
                assertEquals(400, bindEmail(client, port, mapper, token, emailD, codeC).statusCode(),
                        "验证码按地址绑定，不得用于其他地址");
                assertEquals(200, bindEmail(client, port, mapper, token, emailC, codeC).statusCode(),
                        "其他地址的失败尝试不应消耗本地址预算");
            } finally {
                fixture.clean();
                if (secretVersionId != null) {
                    jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", secretVersionId);
                }
            }
        }
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static void seedBindingCode(SecurityVerificationCodeStore codes, SecretRuntimeService secrets,
                                        String email, UUID userId, String code) {
        String hmac = secrets.requireActiveValue("security.verification-code-hmac");
        codes.save(RedisCacheKey.BIND_EMAIL_CODE + email,
                userId + ":" + VerificationCodeDigest.digest(code, hmac), Duration.ofMinutes(5));
    }

    private static HttpResponse<String> bindEmail(HttpClient client, int port, ObjectMapper mapper, String token,
                                                  String email, String code)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/identities/email"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "email", email, "code", code))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        return token;
    }

    private static String uniqueEmail() {
        return "b02bind" + UUID.randomUUID().toString().replace("-", "") + "@example.test";
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> securityRedisTemplate(ApplicationContext application) {
        return (RedisTemplate<String, Object>) application.getBean("securityRedisTemplate");
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
    }

    private record TestUser(UUID id, String username, String password) {
        private static TestUser create() {
            return new TestUser(UUID.randomUUID(),
                    "b02bind" + UUID.randomUUID().toString().replace("-", ""),
                    "Bd!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID operatorRole;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.operatorRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                    UUID.class);
        }

        private void insert(TestUser user) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic binding user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), operatorRole);
            users.add(user);
        }

        private void clean() {
            for (TestUser user : users) {
                jdbc.update("DELETE FROM spectra_security.sec_user_contact WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", user.id());
            }
        }
    }
}
