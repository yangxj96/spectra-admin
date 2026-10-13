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

import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.exception.BusinessConflictException;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authentication.crypto.VerificationCodeDigest;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityBindingService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 使用正式 HTTP、MyBatis、PostgreSQL 和 Redis Session 验证最后有效 Root 保护。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/12
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RootGovernanceFullHttpTest {

    @Test
    void concurrentDisablesPreserveLastRootAndRevokeOnlyCommittedSessions() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var revocation = application.getBean(SecuritySessionRevocationPort.class);
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            ObjectMapper mapper = application.getBean(ObjectMapper.class);
            try (HttpClient client = HttpClient.newHttpClient()) {
                try {
                    assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                    fixture.insertRoot(fixture.operator);
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    String operatorToken = login(client, port, mapper, fixture.operator);

                    assertConflict(disable(client, port, operatorToken, fixture.operator.id()), mapper);
                    assertEquals("ACTIVE", fixture.status(fixture.operator.id()));
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(200, context(client, port, operatorToken).statusCode());

                    fixture.insertRoot(fixture.firstTarget);
                    fixture.insertRoot(fixture.secondTarget);
                    assertEquals(3L, rootPolicy.countEffectiveDevOpsUsers());
                    String firstToken = login(client, port, mapper, fixture.firstTarget);
                    String secondToken = login(client, port, mapper, fixture.secondTarget);

                    assertEquals(1, fixture.minimumEffectiveRoots());
                    fixture.setMinimumEffectiveRoots(2);
                    HttpResponse<String> firstResult;
                    HttpResponse<String> secondResult;
                    try {
                        var first = client.sendAsync(disableRequest(port, operatorToken, fixture.firstTarget.id()),
                                HttpResponse.BodyHandlers.ofString());
                        var second = client.sendAsync(disableRequest(port, operatorToken, fixture.secondTarget.id()),
                                HttpResponse.BodyHandlers.ofString());
                        firstResult = first.join();
                        secondResult = second.join();
                    } finally {
                        fixture.setMinimumEffectiveRoots(1);
                    }
                    assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                            .stream()
                            .filter(status -> status == 200)
                            .count(),
                            "concurrent role removal statuses: "
                                    + firstResult.statusCode() + ", " + secondResult.statusCode());
                    assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                            .stream()
                            .filter(status -> status == 409)
                            .count());
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());

                    var disabled = firstResult.statusCode() == 200 ? fixture.firstTarget : fixture.secondTarget;
                    var active = firstResult.statusCode() == 200 ? fixture.secondTarget : fixture.firstTarget;
                    String disabledToken = firstResult.statusCode() == 200 ? firstToken : secondToken;
                    String activeToken = firstResult.statusCode() == 200 ? secondToken : firstToken;
                    assertConflict(firstResult.statusCode() == 409 ? firstResult : secondResult, mapper);
                    assertEquals("DISABLED", fixture.status(disabled.id()));
                    assertEquals("ACTIVE", fixture.status(active.id()));
                    assertEquals(401, context(client, port, disabledToken).statusCode());
                    assertEquals(200, context(client, port, activeToken).statusCode());

                    assertEquals(200, disable(client, port, operatorToken, active.id()).statusCode());
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(401, context(client, port, activeToken).statusCode());
                    assertConflict(disable(client, port, operatorToken, fixture.operator.id()), mapper);
                    assertEquals("ACTIVE", fixture.status(fixture.operator.id()));
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(200, context(client, port, operatorToken).statusCode());
                } finally {
                    try {
                        for (var user : fixture.users()) {
                            revocation.revokeUserSessions(user.id());
                        }
                    } finally {
                        fixture.clean();
                    }
                }
            }
        }
    }

    @Test
    void concurrentRoleRemovalsPreserveLastRootAndRevokeOnlyCommittedSessions() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var revocation = application.getBean(SecuritySessionRevocationPort.class);
            var assignmentChanges = application.getBean(AuthorizationAssignmentChangeService.class);
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            ObjectMapper mapper = application.getBean(ObjectMapper.class);
            try (HttpClient client = HttpClient.newHttpClient()) {
                try {
                    assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                    fixture.insertDepartment();
                    for (var user : fixture.users()) {
                        fixture.insertRoot(user);
                        fixture.attachDepartment(user);
                        assignmentChanges.ensureDefaultUserRole(user.id());
                    }
                    assertEquals(3L, rootPolicy.countEffectiveDevOpsUsers());
                    String operatorToken = login(client, port, mapper, fixture.operator);
                    String firstToken = login(client, port, mapper, fixture.firstTarget);
                    String secondToken = login(client, port, mapper, fixture.secondTarget);

                    assertEquals(1, fixture.minimumEffectiveRoots());
                    fixture.setMinimumEffectiveRoots(2);
                    HttpResponse<String> firstResult;
                    HttpResponse<String> secondResult;
                    try {
                        var first = client.sendAsync(removeRoleRequest(port, operatorToken, fixture,
                                fixture.firstTarget, mapper), HttpResponse.BodyHandlers.ofString());
                        var second = client.sendAsync(removeRoleRequest(port, operatorToken, fixture,
                                fixture.secondTarget, mapper), HttpResponse.BodyHandlers.ofString());
                        firstResult = first.join();
                        secondResult = second.join();
                    } finally {
                        fixture.setMinimumEffectiveRoots(1);
                    }
                    assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                            .stream()
                            .filter(status -> status == 200)
                            .count());
                    assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                            .stream()
                            .filter(status -> status == 409)
                            .count());
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());

                    var removed = firstResult.statusCode() == 200 ? fixture.firstTarget : fixture.secondTarget;
                    var retained = firstResult.statusCode() == 200 ? fixture.secondTarget : fixture.firstTarget;
                    String removedToken = firstResult.statusCode() == 200 ? firstToken : secondToken;
                    String retainedToken = firstResult.statusCode() == 200 ? secondToken : firstToken;
                    assertConflict(firstResult.statusCode() == 409 ? firstResult : secondResult, mapper);
                    assertEquals("REVOKED", fixture.assignmentState(removed));
                    assertEquals("ACTIVE", fixture.assignmentState(retained));
                    assertEquals(401, context(client, port, removedToken).statusCode());
                    assertEquals(200, context(client, port, retainedToken).statusCode());

                    assertEquals(200, client.send(removeRoleRequest(port, operatorToken, fixture, retained, mapper),
                            HttpResponse.BodyHandlers.ofString()).statusCode());
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(401, context(client, port, retainedToken).statusCode());
                    assertConflict(client.send(removeRoleRequest(port, operatorToken, fixture, fixture.operator,
                            mapper), HttpResponse.BodyHandlers.ofString()), mapper);
                    assertEquals("ACTIVE", fixture.assignmentState(fixture.operator));
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(200, context(client, port, operatorToken).statusCode());
                } finally {
                    try {
                        for (var user : fixture.users()) {
                            revocation.revokeUserSessions(user.id());
                        }
                    } finally {
                        fixture.clean();
                    }
                }
            }
        }
    }

    @Test
    void missingDefaultRoleDoesNotRevokeSessionBeforeRejectedRootRemoval() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var revocation = application.getBean(SecuritySessionRevocationPort.class);
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            ObjectMapper mapper = application.getBean(ObjectMapper.class);
            try (HttpClient client = HttpClient.newHttpClient()) {
                try {
                    assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                    fixture.insertDepartment();
                    fixture.insertRoot(fixture.operator);
                    fixture.attachDepartment(fixture.operator);
                    String operatorToken = login(client, port, mapper, fixture.operator);
                    assertEquals(0L, fixture.defaultRoleCount(fixture.operator));

                    assertConflict(client.send(removeRoleRequest(port, operatorToken, fixture, fixture.operator,
                            mapper), HttpResponse.BodyHandlers.ofString()), mapper);
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals("ACTIVE", fixture.assignmentState(fixture.operator));
                    assertEquals(0L, fixture.defaultRoleCount(fixture.operator));
                    assertEquals(200, context(client, port, operatorToken).statusCode());

                    fixture.insertRoot(fixture.firstTarget);
                    fixture.attachDepartment(fixture.firstTarget);
                    String targetToken = login(client, port, mapper, fixture.firstTarget);
                    assertEquals(0L, fixture.defaultRoleCount(fixture.firstTarget));
                    assertEquals(200, client.send(removeRoleRequest(port, operatorToken, fixture,
                            fixture.firstTarget, mapper), HttpResponse.BodyHandlers.ofString()).statusCode());
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals("REVOKED", fixture.assignmentState(fixture.firstTarget));
                    assertEquals(1L, fixture.defaultRoleCount(fixture.firstTarget));
                    assertEquals(401, context(client, port, targetToken).statusCode());
                    assertEquals(200, context(client, port, operatorToken).statusCode());
                } finally {
                    try {
                        for (var user : fixture.users()) {
                            revocation.revokeUserSessions(user.id());
                        }
                    } finally {
                        fixture.clean();
                    }
                }
            }
        }
    }

    @Test
    void identityUnbindPreservesLastRootAndAllowsRedundantLocalIdentityRemoval() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomTestSecret())) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var identities = application.getBean(AuthenticationIdentityService.class);
            var revocation = application.getBean(SecuritySessionRevocationPort.class);
            var verificationCodes = application.getBean(SecurityVerificationCodeStore.class);
            var secrets = application.getBean(SecretRuntimeService.class);
            var secretManagement = application.getBean(SecretManagementService.class);
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            ObjectMapper mapper = application.getBean(ObjectMapper.class);
            try (HttpClient client = HttpClient.newHttpClient()) {
                String email = fixture.operator.username() + "@example.test";
                UUID secretVersionId = null;
                try {
                    assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(0L, fixture.activeSecretCount("security.verification-code-hmac"));
                    secretVersionId = secretManagement.createPending("security.verification-code-hmac",
                            randomTestSecret(), "GENERATED").getId();
                    secretManagement.publish(secretVersionId);
                    fixture.insertRoot(fixture.operator);
                    UUID localEmailId = identities.createIdentity(fixture.operator.id(), "EMAIL", email).getId();
                    fixture.insertExternalIdentity(fixture.operator);
                    identities.revokeByUserIdAndMethod(fixture.operator.id(), "PASSWORD");
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    String operatorToken = loginByEmail(client, port, mapper, verificationCodes, secrets, email);

                    assertConflict(unbind(client, port, operatorToken, localEmailId), mapper);
                    assertEquals("ACTIVE", fixture.identityState(localEmailId));
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(200, context(client, port, operatorToken).statusCode());

                    identities.updatePasswordIdentifier(fixture.operator.id(), fixture.operator.username());
                    assertEquals(200, unbind(client, port, operatorToken, localEmailId).statusCode());
                    assertEquals("REVOKED", fixture.identityState(localEmailId));
                    assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(401, context(client, port, operatorToken).statusCode());
                } finally {
                    try {
                        revocation.revokeUserSessions(fixture.operator.id());
                    } finally {
                        verificationCodes.delete(RedisCacheKey.LOGIN_EMAIL_CODE + email);
                        verificationCodes.delete(RedisCacheKey.LOGIN_EMAIL_CODE_ATTEMPTS + email);
                        try {
                            fixture.clean();
                        } finally {
                            if (secretVersionId != null) {
                                fixture.deleteSecretVersion(secretVersionId);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void concurrentUnbindsKeepOneIdentityForOrdinaryUser() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var identities = application.getBean(AuthenticationIdentityService.class);
            var binding = application.getBean(AuthenticationIdentityBindingService.class);
            try {
                assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                fixture.insertRoot(fixture.operator);
                fixture.insertRoot(fixture.firstTarget);
                fixture.removeRootAssignment(fixture.firstTarget);
                UUID emailId = identities.createIdentity(fixture.firstTarget.id(), "EMAIL",
                        fixture.firstTarget.username() + "@example.test").getId();
                UUID smsId = identities.createIdentity(fixture.firstTarget.id(), "SMS",
                        "138" + fixture.firstTarget.id().toString().replace("-", "").substring(0, 8)).getId();
                identities.revokeByUserIdAndMethod(fixture.firstTarget.id(), "PASSWORD");
                assertEquals(2L, fixture.activeIdentityCount(fixture.firstTarget));
                assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());

                var start = new CountDownLatch(1);
                try (var workers = Executors.newFixedThreadPool(2)) {
                    var first = workers.submit(() -> tryUnbind(start, binding, fixture.firstTarget.id(), emailId));
                    var second = workers.submit(() -> tryUnbind(start, binding, fixture.firstTarget.id(), smsId));
                    start.countDown();
                    assertEquals(1L, List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS))
                            .stream()
                            .filter(Boolean::booleanValue)
                            .count());
                }
                assertEquals(1L, fixture.activeIdentityCount(fixture.firstTarget));
                assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
            } finally {
                fixture.clean();
            }
        }
    }

    private static boolean tryUnbind(CountDownLatch start, AuthenticationIdentityBindingService binding,
                                     UUID userId, UUID identityId)
            throws InterruptedException {
        start.await();
        try {
            binding.unbind(userId, identityId);
            return true;
        } catch (BusinessConflictException exception) {
            return false;
        }
    }

    private static String randomTestSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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

    private static String login(HttpClient client, int port, ObjectMapper mapper, TestUser user) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", user.username(), "password", user.password()))))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        assertFalse(token.isBlank());
        return token;
    }

    private static String loginByEmail(HttpClient client, int port, ObjectMapper mapper,
                                       SecurityVerificationCodeStore verificationCodes, SecretRuntimeService secrets,
                                       String email)
            throws Exception {
        String code = "214365";
        verificationCodes.save(RedisCacheKey.LOGIN_EMAIL_CODE + email,
                VerificationCodeDigest.digest(code, secrets.requireActiveValue("security.verification-code-hmac")),
                Duration.ofMinutes(5));
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "EMAIL", "username", email, "email_code", code))))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        assertFalse(token.isBlank());
        return token;
    }

    private static HttpResponse<String> disable(HttpClient client, int port, String token, UUID userId)
            throws Exception {
        return client.send(disableRequest(port, token, userId), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> unbind(HttpClient client, int port, String token, UUID identityId)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/identities/" + identityId))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest disableRequest(int port, String token, UUID userId) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/user/disable/" + userId))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private static HttpRequest removeRoleRequest(int port, String token, Fixture fixture, TestUser user,
                                                 ObjectMapper mapper)
            throws Exception {
        var request = Map.of(
                "user", Map.of("id", user.id(), "employee_no", user.username(), "real_name", "synthetic root user",
                        "status", "ACTIVE", "username", user.username(), "primary_department_id", fixture.departmentId,
                        "associated_department_ids", List.of()),
                "authorization", Map.of("assignments", List.of(), "removed_assignments",
                        List.of(Map.of("assignment_id", fixture.assignmentId(user), "expected_version", 0))));
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/user/onboarding"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(request)))
                .build();
    }

    private static HttpResponse<String> context(HttpClient client, int port, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertConflict(HttpResponse<String> response, ObjectMapper mapper) throws Exception {
        assertEquals(409, response.statusCode());
        assertEquals(409, mapper.readTree(response.body()).path("code").asInt());
    }

    private record TestUser(UUID id, String username, String password) {
        private static TestUser create() {
            return new TestUser(UUID.randomUUID(), "b02root" + UUID.randomUUID().toString().replace("-", ""),
                    UUID.randomUUID().toString() + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final TestUser operator = TestUser.create();
        private final TestUser firstTarget = TestUser.create();
        private final TestUser secondTarget = TestUser.create();
        private final UUID rootRole;
        private final UUID departmentId = UUID.randomUUID();

        private Fixture(JdbcTemplate jdbc, PasswordEncoder encoder) {
            this.jdbc = jdbc;
            this.encoder = encoder;
            rootRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                    UUID.class);
        }

        private List<TestUser> users() {
            return List.of(operator, firstTarget, secondTarget);
        }

        private void insertRoot(TestUser user) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic root user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), rootRole);
        }

        private void insertDepartment() {
            jdbc.update("INSERT INTO spectra_core.sys_department (id, name, code, created_at, updated_at) "
                    + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", departmentId,
                    "synthetic root department", "b02root" + departmentId.toString().replace("-", ""));
        }

        private void attachDepartment(TestUser user) {
            jdbc.update("UPDATE spectra_core.sys_user SET primary_department_id = ? WHERE id = ?", departmentId,
                    user.id());
        }

        private UUID assignmentId(TestUser user) {
            return jdbc.queryForObject("SELECT id FROM spectra_security.sec_role_assignment "
                    + "WHERE user_id = ? AND role_id = ? AND state = 'ACTIVE'", UUID.class, user.id(), rootRole);
        }

        private String assignmentState(TestUser user) {
            return jdbc.queryForObject("SELECT state FROM spectra_security.sec_role_assignment "
                    + "WHERE user_id = ? AND role_id = ?", String.class, user.id(), rootRole);
        }

        private void removeRootAssignment(TestUser user) {
            jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", user.id());
        }

        private void insertExternalIdentity(TestUser user) {
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, provider_code, identifier_hash) VALUES (?, 'EMAIL', 'EXTERNAL', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username() + ".external@example.test"));
        }

        private String identityState(UUID identityId) {
            return jdbc.queryForObject("SELECT state FROM spectra_security.sec_authentication_identity WHERE id = ?",
                    String.class, identityId);
        }

        private long activeIdentityCount(TestUser user) {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_authentication_identity "
                    + "WHERE user_id = ? AND state = 'ACTIVE'", Long.class, user.id());
        }

        private long activeSecretCount(String code) {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_secret_version version "
                    + "JOIN spectra_security.sec_secret_definition definition "
                    + "ON definition.id = version.secret_definition_id "
                    + "WHERE definition.code = ? AND version.state = 'ACTIVE' AND version.deleted IS NULL",
                    Long.class, code);
        }

        private void deleteSecretVersion(UUID versionId) {
            jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", versionId);
        }

        private long defaultRoleCount(TestUser user) {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_role_assignment assignment "
                    + "JOIN spectra_security.sec_role role ON role.id = assignment.role_id "
                    + "WHERE assignment.user_id = ? AND role.code = 'ROLE_USER' AND assignment.state = 'ACTIVE'",
                    Long.class, user.id());
        }

        private String status(UUID userId) {
            return jdbc.queryForObject("SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, userId);
        }

        private int minimumEffectiveRoots() {
            return jdbc.queryForObject("SELECT min_effective_dev_ops_users FROM spectra_security.sec_root_policy "
                    + "WHERE policy_key = 'SYSTEM'", Integer.class);
        }

        private void setMinimumEffectiveRoots(int minimum) {
            jdbc.update("UPDATE spectra_security.sec_root_policy SET min_effective_dev_ops_users = ? "
                    + "WHERE policy_key = 'SYSTEM'", minimum);
        }

        private void clean() {
            for (var user : users()) {
                var scopes = jdbc.queryForList("SELECT boundary.scope_id "
                        + "FROM spectra_security.sec_assignment_permission_boundary boundary "
                        + "JOIN spectra_security.sec_role_assignment assignment "
                        + "ON assignment.id = boundary.assignment_id WHERE assignment.user_id = ? "
                        + "UNION SELECT boundary.scope_id "
                        + "FROM spectra_security.sec_assignment_grant_boundary boundary "
                        + "JOIN spectra_security.sec_role_assignment assignment "
                        + "ON assignment.id = boundary.assignment_id WHERE assignment.user_id = ?",
                        UUID.class, user.id(), user.id());
                for (var scope : scopes) {
                    jdbc.update("DELETE FROM spectra_security.sec_scope_rule WHERE scope_id = ?", scope);
                }
                jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                for (var scope : scopes) {
                    jdbc.update("DELETE FROM spectra_security.sec_authorization_scope WHERE id = ?", scope);
                }
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", user.id());
            }
            jdbc.update("DELETE FROM spectra_core.sys_department WHERE id = ?", departmentId);
        }
    }
}
