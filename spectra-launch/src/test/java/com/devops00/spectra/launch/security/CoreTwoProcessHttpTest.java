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

import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.root.RootPolicyRepository;
import com.devops00.spectra.framework.persistence.scope.context.DataScopeExecutor;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.launch.LaunchApplication;
import org.flowable.engine.RepositoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用两个独立应用 JVM 的正式 Core HTTP 链路验证共享 PostgreSQL/Redis 下的凭据重载与跨进程会话撤销。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CoreTwoProcessHttpTest {

    private static final String WEB_CONCURRENCY_SQL = """
            UPDATE spectra_security.sec_session_policy
            SET concurrency_mode = ?, max_sessions = ?
            WHERE client_id = (SELECT id FROM spectra_security.sec_security_client WHERE code = 'web')
            """;

    @Test
    void lastRootSurvivesOneApplicationRedisNetworkIsolation() throws Exception {
        requireIsolatedDependencies();
        String redisServer = System.getenv("SPECTRA_TEST_REDIS_SERVER");
        if (redisServer == null || redisServer.isBlank()) {
            redisServer = "D:/Develop/Platform/redis/redis-server.exe";
        }
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-root-redis-isolation-");
        int redisPort = freePort();
        Process redis = startRedis(redisServer, temporary, redisPort);
        try {
            awaitListening(redisPort, redis);
            try (var proxy = new RedisProxy(redisPort);
                    var first = new SpringApplicationBuilder(LaunchApplication.class)
                            .run(coreArgs("redis://127.0.0.1:" + proxy.port() + "/5"))) {
                Path ready = temporary.resolve("second-port.txt");
                Process second = startSecond(ready, temporary.resolve("second.log"),
                        "redis://127.0.0.1:" + redisPort + "/5");
                try (HttpClient client = HttpClient.newHttpClient()) {
                    int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                    int secondPort = awaitPort(second, ready);
                    assertNotEquals(ProcessHandle.current().pid(), second.pid());
                    var mapper = first.getBean(ObjectMapper.class);
                    var properties = first.getBean(SecurityProperties.class);
                    var rootPolicy = first.getBean(RootPolicyRepository.class);
                    var jdbc = first.getBean(JdbcTemplate.class);
                    var fixture = new Fixture(first);
                    TestUser root = TestUser.create("b02rootni");
                    try {
                        fixture.insertWithRole(root, "ROLE_DEV_OPS");
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                        Session before = login(client, firstPort, mapper, properties,
                                root.username(), root.password());
                        assertEquals(200, context(client, firstPort, before.accessToken()));
                        assertEquals(200, context(client, secondPort, before.accessToken()));

                        proxy.isolate();
                        assertTrue(redis.isAlive());
                        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                            var firstDenied = executor.submit(() -> context(client, firstPort, before.accessToken()));
                            var secondAccepted = executor.submit(() -> context(client, secondPort, before.accessToken()));
                            assertEquals(503, firstDenied.get(30, TimeUnit.SECONDS));
                            assertEquals(200, secondAccepted.get(30, TimeUnit.SECONDS));
                        }
                        assertEquals(503, disable(client, firstPort, before.accessToken(), root.id()).statusCode());
                        assertEquals(503, passwordLogin(client, firstPort, mapper,
                                root.username(), root.password()).statusCode());
                        assertLastRootConflict(disable(client, secondPort, before.accessToken(), root.id()), mapper);
                        assertEquals(200, context(client, secondPort, before.accessToken()));
                        assertEquals("ACTIVE", jdbc.queryForObject(
                                "SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());

                        proxy.restore();
                        awaitLoginRecovery(client, firstPort, mapper, root);
                        assertTrue(redis.isAlive());
                        assertEquals(200, context(client, firstPort, before.accessToken()));
                        assertEquals(200, context(client, secondPort, before.accessToken()));
                        assertLastRootConflict(disable(client, firstPort, before.accessToken(), root.id()), mapper);
                        assertEquals("ACTIVE", jdbc.queryForObject(
                                "SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    } finally {
                        fixture.clean();
                    }
                } finally {
                    stop(second);
                }
            }
        } finally {
            stop(redis);
        }
    }

    @Test
    void lastRootSurvivesSharedIsolatedRedisOutageAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        String redisServer = System.getenv("SPECTRA_TEST_REDIS_SERVER");
        if (redisServer == null || redisServer.isBlank()) {
            redisServer = "D:/Develop/Platform/redis/redis-server.exe";
        }
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-root-redis-fault-two-process-");
        int redisPort = freePort();
        String redisUrl = "redis://127.0.0.1:" + redisPort + "/5";
        Process redis = startRedis(redisServer, temporary, redisPort);
        try {
            awaitListening(redisPort, redis);
            try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs(redisUrl))) {
                Path ready = temporary.resolve("second-port.txt");
                Process second = startSecond(ready, temporary.resolve("second.log"), redisUrl);
                try (HttpClient client = HttpClient.newHttpClient()) {
                    int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                    int secondPort = awaitPort(second, ready);
                    assertNotEquals(ProcessHandle.current().pid(), second.pid());
                    var mapper = first.getBean(ObjectMapper.class);
                    var properties = first.getBean(SecurityProperties.class);
                    var rootPolicy = first.getBean(RootPolicyRepository.class);
                    var jdbc = first.getBean(JdbcTemplate.class);
                    var fixture = new Fixture(first);
                    TestUser root = TestUser.create("b02rootrf");
                    try {
                        fixture.insertWithRole(root, "ROLE_DEV_OPS");
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                        Session before = login(client, firstPort, mapper, properties,
                                root.username(), root.password());
                        assertEquals(200, context(client, firstPort, before.accessToken()));
                        assertEquals(200, context(client, secondPort, before.accessToken()));

                        stop(redis);
                        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                            var firstDenied = executor.submit(() -> context(client, firstPort, before.accessToken()));
                            var secondDenied = executor.submit(() -> context(client, secondPort, before.accessToken()));
                            assertEquals(503, firstDenied.get(30, TimeUnit.SECONDS));
                            assertEquals(503, secondDenied.get(30, TimeUnit.SECONDS));
                        }
                        assertEquals(503, disable(client, firstPort, before.accessToken(), root.id()).statusCode());
                        assertEquals(503, disable(client, secondPort, before.accessToken(), root.id()).statusCode());
                        assertEquals(503, passwordLogin(client, firstPort, mapper,
                                root.username(), root.password()).statusCode());
                        assertEquals(503, passwordLogin(client, secondPort, mapper,
                                root.username(), root.password()).statusCode());
                        assertEquals("ACTIVE", jdbc.queryForObject(
                                "SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());

                        redis = startRedis(redisServer, temporary, redisPort);
                        awaitListening(redisPort, redis);
                        awaitLoginRecovery(client, firstPort, mapper, root);
                        awaitLoginRecovery(client, secondPort, mapper, root);
                        Session after = login(client, secondPort, mapper, properties,
                                root.username(), root.password());
                        assertEquals(401, context(client, firstPort, before.accessToken()));
                        assertEquals(401, context(client, secondPort, before.accessToken()));
                        assertEquals(401, refresh(client, firstPort, before.refreshToken(), properties).statusCode());
                        assertEquals(401, refresh(client, secondPort, before.refreshToken(), properties).statusCode());
                        assertEquals(200, context(client, firstPort, after.accessToken()));
                        assertEquals(200, context(client, secondPort, after.accessToken()));
                        assertLastRootConflict(disable(client, firstPort, after.accessToken(), root.id()), mapper);
                        assertLastRootConflict(disable(client, secondPort, after.accessToken(), root.id()), mapper);
                        assertEquals("ACTIVE", jdbc.queryForObject(
                                "SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    } finally {
                        fixture.clean();
                    }
                } finally {
                    stop(second);
                }
            }
        } finally {
            if (redis.isAlive()) {
                stop(redis);
            }
        }
    }

    @Test
    void naturalRootAssignmentAndCredentialExpiryPropagatesAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-root-natural-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var jdbc = first.getBean(JdbcTemplate.class);
                var rootPolicy = first.getBean(RootPolicyRepository.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser operator = TestUser.create("b02rootoper");
                TestUser target = TestUser.create("b02rootexp");
                var business = new RootBusinessFixture(jdbc, operator, target);
                var repository = first.getBean(RepositoryService.class);
                String deploymentId = null;
                try {
                    fixture.insertWithRole(operator, "ROLE_DEV_OPS");
                    fixture.insertWithRole(target, "ROLE_DEV_OPS");
                    business.insert();
                    String processKey = "rootExpiry" + UUID.randomUUID().toString().replace("-", "");
                    var deployment = repository.createDeployment()
                            .addString(processKey + ".bpmn20.xml", simpleProcess(processKey))
                            .deploy();
                    deploymentId = deployment.getId();
                    String processId = repository.createProcessDefinitionQuery()
                            .deploymentId(deploymentId)
                            .singleResult()
                            .getId();
                    String operatorToken = login(client, firstPort, mapper, properties, operator.username(),
                            operator.password()).accessToken();
                    Session targetSession = login(client, secondPort, mapper, properties, target.username(),
                            target.password());
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(200, context(client, firstPort, targetSession.accessToken()));
                    assertEquals(200, context(client, secondPort, targetSession.accessToken()));
                    assertModuleReads(client, firstPort, secondPort, targetSession.accessToken(), 200);
                    assertMeetingVisible(client, firstPort, secondPort, mapper, targetSession.accessToken(), business,
                            true);
                    assertEquals(200, businessWrite(client, firstPort, targetSession.accessToken(),
                            "/api/oa/meeting/" + business.visibleMeeting + "/response",
                            "{\"status\":\"accepted\"}").statusCode());
                    assertEquals("accepted", business.participantStatus());
                    String workflowWrite = "/api/workflow/process-definitions/" + processId + "/suspend";
                    assertEquals(200, businessWrite(client, firstPort, targetSession.accessToken(),
                            workflowWrite, null).statusCode());
                    assertTrue(repository.createProcessDefinitionQuery()
                            .processDefinitionId(processId)
                            .singleResult()
                            .isSuspended());
                    assertEquals(200, businessWrite(client, firstPort, targetSession.accessToken(),
                            "/api/workflow/process-definitions/" + processId + "/activate", null).statusCode());

                    assertEquals(1, jdbc.update("UPDATE spectra_security.sec_role_assignment "
                            + "SET valid_until = statement_timestamp() + INTERVAL '5 seconds' "
                            + "WHERE user_id = ? AND role_id = "
                            + "(SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS')",
                            target.id()));
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());
                    awaitEffectiveRoots(rootPolicy, 1L);
                    assertEquals(200, context(client, firstPort, targetSession.accessToken()));
                    assertEquals(200, context(client, secondPort, targetSession.accessToken()));
                    assertModuleReads(client, firstPort, secondPort, targetSession.accessToken(), 403);
                    assertMeetingVisible(client, firstPort, secondPort, mapper, targetSession.accessToken(), business,
                            false);
                    for (int port : List.of(firstPort, secondPort)) {
                        assertEquals(403, businessWrite(client, port, targetSession.accessToken(),
                                "/api/oa/meeting/" + business.visibleMeeting + "/response",
                                "{\"status\":\"declined\"}").statusCode());
                        assertEquals(403, businessWrite(client, port, targetSession.accessToken(),
                                workflowWrite, null).statusCode());
                    }
                    assertEquals("accepted", business.participantStatus());
                    assertTrue(!repository.createProcessDefinitionQuery()
                            .processDefinitionId(processId)
                            .singleResult()
                            .isSuspended());
                    assertThrows(DataScopeViolationException.class,
                            () -> first.getBean(DataScopeExecutor.class).withoutScope(() -> business.visibleMeeting));
                    assertModuleReads(client, firstPort, secondPort, operatorToken, 200);
                    assertEquals(403, disable(client, firstPort, targetSession.accessToken(),
                            operator.id()).statusCode());
                    assertEquals(403, disable(client, secondPort, targetSession.accessToken(),
                            operator.id()).statusCode());
                    assertLastRootConflict(disable(client, firstPort, operatorToken, operator.id()), mapper);

                    assertEquals(1, jdbc.update("UPDATE spectra_security.sec_role_assignment "
                            + "SET valid_until = NULL WHERE user_id = ? AND role_id = "
                            + "(SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS')", target.id()));
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());
                    assertEquals(1, jdbc.update("UPDATE spectra_security.sec_password_credential "
                            + "SET expires_at = statement_timestamp() + INTERVAL '5 seconds' WHERE user_id = ?",
                            target.id()));
                    assertEquals(2L, rootPolicy.countEffectiveDevOpsUsers());
                    awaitEffectiveRoots(rootPolicy, 1L);
                    assertEquals(401, context(client, firstPort, targetSession.accessToken()));
                    assertEquals(401, context(client, secondPort, targetSession.accessToken()));
                    assertModuleReads(client, firstPort, secondPort, targetSession.accessToken(), 401);
                    for (int port : List.of(firstPort, secondPort)) {
                        assertEquals(401, businessWrite(client, port, targetSession.accessToken(),
                                "/api/oa/meeting/" + business.visibleMeeting + "/response",
                                "{\"status\":\"declined\"}").statusCode());
                        assertEquals(401, businessWrite(client, port, targetSession.accessToken(),
                                workflowWrite, null).statusCode());
                    }
                    assertModuleReads(client, firstPort, secondPort, operatorToken, 200);
                    assertEquals(401, refresh(client, secondPort, targetSession.refreshToken(),
                            properties).statusCode());
                    assertLastRootConflict(disable(client, secondPort, operatorToken, operator.id()), mapper);
                    assertEquals("ACTIVE", jdbc.queryForObject(
                            "SELECT status FROM spectra_core.sys_user WHERE id = ?", String.class, operator.id()));
                } finally {
                    if (deploymentId != null) {
                        repository.deleteDeployment(deploymentId, true);
                    }
                    business.clean();
                    revocation.revokeUserSessions(operator.id());
                    revocation.revokeUserSessions(target.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    private static void awaitEffectiveRoots(RootPolicyRepository rootPolicy, long expected)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (rootPolicy.countEffectiveDevOpsUsers() != expected && System.nanoTime() < deadline) {
            Thread.sleep(100);
        }
        assertEquals(expected, rootPolicy.countEffectiveDevOpsUsers());
    }

    private static HttpResponse<String> disable(HttpClient client, int port, String token, UUID userId)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/user/disable/" + userId))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertLastRootConflict(HttpResponse<String> response, ObjectMapper mapper)
            throws Exception {
        assertEquals(409, response.statusCode());
        assertEquals("ROOT_EFFECTIVE_MINIMUM", mapper.readTree(response.body()).path("error_code").asText());
    }

    @Test
    void credentialChangeRevokesSessionAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser user = TestUser.create();
                String newPassword = strongPassword();
                try {
                    fixture.insert(user);
                    Session session = login(client, firstPort, mapper, properties, user.username(), user.password());
                    assertEquals(200, context(client, secondPort, session.accessToken()),
                            "第二进程应接受首进程签发的正式 Access");

                    assertEquals(200, changePassword(client, firstPort, mapper, session.accessToken(),
                            user.password(), newPassword).statusCode());

                    assertEquals(401, context(client, firstPort, session.accessToken()),
                            "改密后首进程应拒绝旧 Access");
                    assertEquals(401, context(client, secondPort, session.accessToken()),
                            "改密后第二进程应拒绝旧 Access");
                    assertEquals(401, refresh(client, secondPort, session.refreshToken(), properties).statusCode(),
                            "改密后第二进程应拒绝旧 Refresh");

                    Session reloaded = login(client, secondPort, mapper, properties, user.username(), newPassword);
                    assertTrue(!reloaded.accessToken().isBlank(), "第二进程应接受新口令登录");
                } finally {
                    revocation.revokeUserSessions(user.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void clientAndKickOldRevocationAreSharedAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-revoke-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var jdbc = first.getBean(JdbcTemplate.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser operator = TestUser.create("b02oper");
                TestUser target = TestUser.create("b02targ");
                try {
                    fixture.insertWithRole(operator, "ROLE_DEV_OPS");
                    fixture.insert(target);
                    String operatorToken = login(client, firstPort, mapper, properties, operator.username(),
                            operator.password()).accessToken();
                    String targetToken = login(client, secondPort, mapper, properties, target.username(),
                            target.password()).accessToken();
                    assertEquals(200, context(client, firstPort, targetToken));

                    assertEquals(200, cachePost(client, firstPort, mapper, operatorToken,
                            "/api/cache/admin/security/session/revoke", Map.of(
                                    "user_id", target.id().toString(), "client_type", "WEB",
                                    "reason", "synthetic client revoke", "confirmed", true))
                            .statusCode());
                    assertEquals(401, context(client, secondPort, targetToken),
                            "按客户端撤销后第二进程应拒绝旧 Access");

                    String renewedToken = login(client, secondPort, mapper, properties, target.username(),
                            target.password()).accessToken();
                    assertEquals(200, cachePost(client, firstPort, mapper, operatorToken,
                            "/api/cache/admin/security/session/revoke-all", Map.of(
                                    "user_id", target.id().toString(), "reason", "synthetic revoke all",
                                    "confirmed", true))
                            .statusCode());
                    assertEquals(401, context(client, secondPort, renewedToken),
                            "按用户全量撤销后第二进程应拒绝旧 Access");

                    String handleToken = login(client, secondPort, mapper, properties, target.username(),
                            target.password()).accessToken();
                    String handle = onlineSessionHandle(client, firstPort, mapper, operatorToken, target.id());
                    assertEquals(200, cachePost(client, firstPort, mapper, operatorToken,
                            "/api/cache/admin/security/session/revoke-one", Map.of(
                                    "session_id", handle, "reason", "synthetic handle revoke",
                                    "confirmed", true))
                            .statusCode());
                    assertEquals(401, context(client, secondPort, handleToken),
                            "按在线句柄精确撤销后第二进程应拒绝旧 Access");

                    WebConcurrency original = webConcurrency(jdbc);
                    try {
                        assertEquals(1, jdbc.update(WEB_CONCURRENCY_SQL, "KICK_OLD", original.maxSessions()));
                        String firstSession = login(client, secondPort, mapper, properties, target.username(),
                                target.password()).accessToken();
                        assertEquals(200, context(client, secondPort, firstSession));
                        String secondSession = login(client, firstPort, mapper, properties, target.username(),
                                target.password()).accessToken();
                        assertEquals(401, context(client, secondPort, firstSession),
                                "KICK_OLD 应跨进程踢掉同客户端旧会话");
                        assertEquals(200, context(client, secondPort, secondSession));
                    } finally {
                        assertEquals(1, jdbc.update(WEB_CONCURRENCY_SQL, original.mode(), original.maxSessions()));
                    }
                } finally {
                    revocation.revokeUserSessions(operator.id());
                    revocation.revokeUserSessions(target.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void sessionPolicyTightenRevokesAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-policy-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var jdbc = first.getBean(JdbcTemplate.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser operator = TestUser.create("b02poli");
                TestUser target = TestUser.create("b02polo");
                Map<String, Object> original = null;
                try {
                    fixture.insertWithRole(operator, "ROLE_DEV_OPS");
                    fixture.insert(target);
                    String targetToken = login(client, secondPort, mapper, properties, target.username(),
                            target.password()).accessToken();
                    String operatorToken = login(client, firstPort, mapper, properties, operator.username(),
                            operator.password()).accessToken();
                    assertEquals(200, context(client, secondPort, targetToken));

                    original = webPolicyRow(jdbc);
                    Map<String, Object> tightened = new java.util.LinkedHashMap<>(original);
                    tightened.remove("client_id");
                    tightened.remove("version");
                    tightened.put("absolute_ttl_seconds", 1);
                    tightened.put("expected_version", ((Number) original.get("version")).longValue());
                    assertEquals(200, put(client, firstPort, mapper, operatorToken,
                            "/api/security/policy/session/" + original.get("client_id"), tightened).statusCode());

                    Thread.sleep(2000);
                    assertEquals(401, context(client, secondPort, targetToken),
                            "会话策略收紧应跨进程使旧 Access 失效");
                } finally {
                    if (original != null) {
                        restoreWebPolicy(jdbc, original);
                    }
                    revocation.revokeUserSessions(operator.id());
                    revocation.revokeUserSessions(target.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    private static Map<String, Object> webPolicyRow(JdbcTemplate jdbc) {
        return jdbc.queryForMap("""
                SELECT client_id, concurrency_mode, allow_concurrent, max_sessions, access_ttl_seconds,
                       refresh_ttl_seconds, absolute_ttl_seconds, idle_ttl_seconds, version
                FROM spectra_security.sec_session_policy
                WHERE client_id = (SELECT id FROM spectra_security.sec_security_client WHERE code = 'web')
                """);
    }

    private static void restoreWebPolicy(JdbcTemplate jdbc, Map<String, Object> original) {
        jdbc.update("""
                UPDATE spectra_security.sec_session_policy
                SET concurrency_mode = ?, allow_concurrent = ?, max_sessions = ?, access_ttl_seconds = ?,
                    refresh_ttl_seconds = ?, absolute_ttl_seconds = ?, idle_ttl_seconds = ?, version = version + 1
                WHERE client_id = ?
                """, original.get("concurrency_mode"), original.get("allow_concurrent"),
                original.get("max_sessions"), original.get("access_ttl_seconds"),
                original.get("refresh_ttl_seconds"), original.get("absolute_ttl_seconds"),
                original.get("idle_ttl_seconds"), original.get("client_id"));
    }

    private static HttpResponse<String> put(HttpClient client, int port, ObjectMapper mapper, String token,
                                            String path, Map<String, Object> body)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void clientRevokeAndKickOldAfterAccessExpiryAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-expired-revoke-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var jdbc = first.getBean(JdbcTemplate.class);
                var redis = securityRedisTemplate(first);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser operator = TestUser.create("b02exp");
                TestUser target = TestUser.create("b02ext");
                Map<String, Object> original = null;
                try {
                    fixture.insertWithRole(operator, "ROLE_DEV_OPS");
                    fixture.insert(target);
                    original = webPolicyRow(jdbc);
                    jdbc.update("UPDATE spectra_security.sec_session_policy SET access_ttl_seconds = 3 "
                            + "WHERE client_id = (SELECT id FROM spectra_security.sec_security_client "
                            + "WHERE code = 'web')");

                    Session expired = login(client, secondPort, mapper, properties, target.username(),
                            target.password());
                    awaitRedisExpiry(redis, SecurityRedisKey.SESSION.format(
                            TokenDigestService.digest(expired.accessToken())), 12);
                    assertEquals(401, context(client, secondPort, expired.accessToken()));
                    String operatorToken = login(client, firstPort, mapper, properties, operator.username(),
                            operator.password()).accessToken();
                    assertEquals(200, cachePost(client, firstPort, mapper, operatorToken,
                            "/api/cache/admin/security/session/revoke", Map.of(
                                    "user_id", target.id().toString(), "client_type", "WEB",
                                    "reason", "synthetic expired client revoke", "confirmed", true))
                            .statusCode());
                    assertEquals(401, refresh(client, secondPort, expired.refreshToken(), properties).statusCode(),
                            "Access 到期后按客户端撤销应拒绝 Refresh");

                    assertEquals(1, jdbc.update(WEB_CONCURRENCY_SQL, "KICK_OLD", original.get("max_sessions")));
                    Session oldSession = login(client, secondPort, mapper, properties, target.username(),
                            target.password());
                    awaitRedisExpiry(redis, SecurityRedisKey.SESSION.format(
                            TokenDigestService.digest(oldSession.accessToken())), 12);
                    Session newSession = login(client, firstPort, mapper, properties, target.username(),
                            target.password());
                    assertEquals(401, refresh(client, secondPort, oldSession.refreshToken(), properties).statusCode(),
                            "Access 到期后 KICK_OLD 应跨进程踢掉同端旧 Refresh");
                    assertEquals(200, refresh(client, secondPort, newSession.refreshToken(), properties).statusCode());
                } finally {
                    if (original != null) {
                        restoreWebPolicy(jdbc, original);
                    }
                    revocation.revokeUserSessions(operator.id());
                    revocation.revokeUserSessions(target.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void naturalAccessExpiryAndRefreshReloadAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-natural-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var jdbc = first.getBean(JdbcTemplate.class);
                var redis = securityRedisTemplate(first);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var fixture = new Fixture(first);
                TestUser target = TestUser.create("b02nat");
                Map<String, Object> original = null;
                try {
                    fixture.insert(target);
                    original = webPolicyRow(jdbc);
                    assertEquals(1, jdbc.update("UPDATE spectra_security.sec_session_policy SET access_ttl_seconds = 2 "
                            + "WHERE client_id = (SELECT id FROM spectra_security.sec_security_client "
                            + "WHERE code = 'web')"));

                    Session session = login(client, secondPort, mapper, properties, target.username(),
                            target.password());
                    assertEquals(200, context(client, firstPort, session.accessToken()));
                    awaitRedisExpiry(redis, SecurityRedisKey.SESSION.format(
                            TokenDigestService.digest(session.accessToken())), 8);
                    assertEquals(401, context(client, firstPort, session.accessToken()),
                            "Access 自然到期后首进程应拒绝");
                    assertEquals(401, context(client, secondPort, session.accessToken()),
                            "Access 自然到期后第二进程应拒绝");

                    var refreshed = refresh(client, firstPort, session.refreshToken(), properties);
                    assertEquals(200, refreshed.statusCode(), "Refresh 仍有效应可跨进程轮换");
                    String replacement = mapper.readTree(refreshed.body())
                            .path("data")
                            .path("access_token")
                            .asText();
                    assertTrue(!replacement.isBlank());
                    assertEquals(200, context(client, secondPort, replacement));
                } finally {
                    if (original != null) {
                        restoreWebPolicy(jdbc, original);
                    }
                    revocation.revokeUserSessions(target.id());
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> securityRedisTemplate(ApplicationContext application) {
        return (RedisTemplate<String, Object>) application.getBean("securityRedisTemplate");
    }

    private static void awaitRedisExpiry(RedisTemplate<String, Object> redis, String key, int timeoutSeconds)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        while (Boolean.TRUE.equals(redis.hasKey(key)) && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        assertTrue(!Boolean.TRUE.equals(redis.hasKey(key)));
    }

    private static String onlineSessionHandle(HttpClient client, int port, ObjectMapper mapper, String token,
                                              UUID userId)
            throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/user/online?pageNum=1&pageSize=100"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        for (var record : mapper.readTree(response.body()).path("data").path("records")) {
            if (userId.toString().equals(record.path("user_id").asText())) {
                var sessions = record.path("sessions");
                if (sessions.isArray() && !sessions.isEmpty()) {
                    String handle = sessions.get(0).path("session_id").asText();
                    if (!handle.isBlank()) {
                        return handle;
                    }
                }
            }
        }
        throw new AssertionError("在线会话列表未返回目标用户句柄");
    }

    private static HttpResponse<String> cachePost(HttpClient client, int port, ObjectMapper mapper, String token,
                                                  String path, Map<String, Object> body)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static WebConcurrency webConcurrency(JdbcTemplate jdbc) {
        return jdbc.queryForObject("""
                SELECT policy.concurrency_mode, policy.max_sessions
                FROM spectra_security.sec_session_policy policy
                JOIN spectra_security.sec_security_client client ON client.id = policy.client_id
                WHERE client.code = 'web'
                """, (row, _) -> new WebConcurrency(row.getString("concurrency_mode"), row.getInt("max_sessions")));
    }

    private record WebConcurrency(String mode, int maxSessions) {
    }

    private static String[] coreArgs() {
        return new String[]{"--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false"};
    }

    private static String[] coreArgs(String redisUrl) {
        String[] base = coreArgs();
        String[] args = Arrays.copyOf(base, base.length + 1);
        args[args.length - 1] = "--spring.data.redis.url=" + redisUrl;
        return args;
    }

    private static Process startSecond(Path ready, Path log) throws Exception {
        return startSecond(ready, log, null);
    }

    private static Process startSecond(Path ready, Path log, String redisUrl) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        ProcessBuilder builder = new ProcessBuilder(java, "-Duser.timezone=UTC", "-cp",
                System.getProperty("java.class.path"), SecondApplication.class.getName(), ready.toString())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile());
        if (redisUrl != null) {
            builder.environment().put("SPRING_DATA_REDIS_URL", redisUrl);
        }
        return builder.start();
    }

    private static Process startRedis(String redisServer, Path home, int port) throws Exception {
        return new ProcessBuilder(redisServer, "--port", Integer.toString(port), "--bind", "127.0.0.1",
                "--save", "", "--appendonly", "no", "--dir", home.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .redirectOutput(home.resolve("redis.out").toFile())
                .start();
    }

    /** 仅转发测试进程的 Redis TCP 流量，可断开一端而保持 Redis 和另一应用可用。 */
    private static final class RedisProxy implements AutoCloseable {
        private final int redisPort;
        private final ServerSocket listener;
        private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
        private final AtomicBoolean isolated = new AtomicBoolean();
        private final java.util.concurrent.ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();

        private RedisProxy(int redisPort) throws IOException {
            this.redisPort = redisPort;
            this.listener = new ServerSocket();
            listener.bind(new InetSocketAddress("127.0.0.1", 0));
            workers.submit(this::accept);
        }

        private int port() {
            return listener.getLocalPort();
        }

        private void isolate() {
            isolated.set(true);
            sockets.forEach(RedisProxy::closeSocket);
        }

        private void restore() {
            isolated.set(false);
        }

        private void accept() {
            while (!listener.isClosed()) {
                try {
                    Socket client = listener.accept();
                    if (isolated.get()) {
                        closeSocket(client);
                        continue;
                    }
                    workers.submit(() -> forward(client));
                } catch (IOException exception) {
                    if (!listener.isClosed()) {
                        throw new IllegalStateException("隔离 Redis 转发器异常", exception);
                    }
                }
            }
        }

        private void forward(Socket client) {
            Socket redis = null;
            try (client) {
                redis = new Socket("127.0.0.1", redisPort);
                sockets.add(client);
                sockets.add(redis);
                if (isolated.get()) {
                    return;
                }
                Socket upstream = redis;
                workers.submit(() -> pump(upstream, client));
                pump(client, redis);
            } catch (IOException ignored) {
                // 断开当前转发链路，让应用直接观察 Redis 连接失败。
            } finally {
                sockets.remove(client);
                closeSocket(client);
                if (redis != null) {
                    sockets.remove(redis);
                    closeSocket(redis);
                }
            }
        }

        private static void pump(Socket source, Socket destination) {
            try {
                source.getInputStream().transferTo(destination.getOutputStream());
            } catch (IOException ignored) {
                // 对端关闭或主动隔离时结束转发。
            } finally {
                closeSocket(source);
                closeSocket(destination);
            }
        }

        private static void closeSocket(Socket socket) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // 关闭已断开的测试连接是幂等操作。
            }
        }

        @Override
        public void close() throws IOException {
            listener.close();
            sockets.forEach(RedisProxy::closeSocket);
            workers.shutdownNow();
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void awaitListening(int port, Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
                return;
            } catch (Exception ignored) {
                if (!process.isAlive()) {
                    throw new IllegalStateException("隔离 Redis 未启动，退出码 " + process.exitValue());
                }
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("隔离 Redis 未在 15 秒内监听端口 " + port);
    }

    private static int awaitPort(Process process, Path ready) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
        while (System.nanoTime() < deadline) {
            if (Files.exists(ready)) {
                String value = Files.readString(ready).trim();
                if (!value.isEmpty()) {
                    return Integer.parseInt(value);
                }
            }
            if (!process.isAlive()) {
                throw new IllegalStateException("第二应用进程启动失败，退出码 " + process.exitValue());
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("第二应用进程未在 90 秒内就绪");
    }

    private static void stop(Process process) throws InterruptedException {
        process.destroy();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        }
    }

    private static Session login(HttpClient client, int port, ObjectMapper mapper, SecurityProperties properties,
                                 String username, String password)
            throws Exception {
        var response = passwordLogin(client, port, mapper, username, password);
        assertEquals(200, response.statusCode());
        String accessToken = mapper.readTree(response.body()).path("data").path("access_token").asText();
        String prefix = properties.getRefreshCookieName() + "=";
        String refreshToken = response.headers()
                .allValues("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith(prefix))
                .map(cookie -> cookie.substring(prefix.length()).split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("登录响应缺少 Web Refresh Cookie"));
        return new Session(accessToken, refreshToken);
    }

    private static HttpResponse<String> passwordLogin(HttpClient client, int port, ObjectMapper mapper,
                                                      String username, String password)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void awaitLoginRecovery(HttpClient client, int port, ObjectMapper mapper,
                                           TestUser user)
            throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
        int lastStatus = 0;
        while (System.nanoTime() < deadline) {
            try {
                lastStatus = passwordLogin(client, port, mapper, user.username(), user.password()).statusCode();
                if (lastStatus == 200) {
                    return;
                }
            } catch (Exception ignored) {
                // 隔离 Redis 重启期间等待两个应用的就绪探针恢复。
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("隔离 Redis 重启后登录未恢复，最后 HTTP 状态 " + lastStatus);
    }

    private static int context(HttpClient client, int port, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static int securedGet(HttpClient client, int port, String token, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static void assertModuleReads(HttpClient client, int firstPort, int secondPort,
                                          String token, int expectedStatus)
            throws Exception {
        assertEquals(expectedStatus, securedGet(client, firstPort, token,
                "/api/oa/contact/page?pageNum=1&pageSize=5"));
        assertEquals(expectedStatus, securedGet(client, secondPort, token,
                "/api/oa/contact/page?pageNum=1&pageSize=5"));
        assertEquals(expectedStatus, securedGet(client, firstPort, token,
                "/api/workflow/process-definitions"));
        assertEquals(expectedStatus, securedGet(client, secondPort, token,
                "/api/workflow/process-definitions"));
    }

    private static void assertMeetingVisible(HttpClient client, int firstPort, int secondPort,
                                             ObjectMapper mapper, String token, RootBusinessFixture business,
                                             boolean rootActive)
            throws Exception {
        for (int port : List.of(firstPort, secondPort)) {
            var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                    + "/api/oa/meeting/page?pageNum=1&pageSize=100"))
                    .header("Api-Version", "1.0.0")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            String records = mapper.readTree(response.body()).path("data").path("records").toString();
            assertTrue(records.contains(business.visibleMeeting.toString()));
            assertEquals(rootActive, records.contains(business.hiddenMeeting.toString()));
        }
    }

    private static HttpResponse<String> businessWrite(HttpClient client, int port, String token,
                                                      String path, String body)
            throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token);
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        return client.send(request.POST(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String simpleProcess(String key) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             targetNamespace="http://spectra.dev/test">
                  <process id="%s" isExecutable="true">
                    <startEvent id="start"/>
                    <endEvent id="end"/>
                    <sequenceFlow id="flow" sourceRef="start" targetRef="end"/>
                  </process>
                </definitions>
                """.formatted(key);
    }

    private static HttpResponse<String> changePassword(HttpClient client, int port, ObjectMapper mapper, String token,
                                                       String oldPassword, String newPassword)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/user/password"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "old_password", oldPassword, "new_password", newPassword, "verify_password", newPassword))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> refresh(HttpClient client, int port, String refreshToken,
                                                SecurityProperties properties)
            throws Exception {
        String csrf = UUID.randomUUID().toString();
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/refresh"))
                .header("Api-Version", "1.0.0")
                .header("Cookie", properties.getRefreshCookieName() + "=" + refreshToken + "; "
                        + properties.getCsrfCookieName() + "=" + csrf)
                .header(properties.getCsrfHeaderName(), csrf)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String strongPassword() {
        return "Core1!" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
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

    private record Session(String accessToken, String refreshToken) {
    }

    private record TestUser(UUID id, String username, String password) {
        private static TestUser create() {
            return create("b02core");
        }

        private static TestUser create(String prefix) {
            return new TestUser(UUID.randomUUID(), prefix + UUID.randomUUID().toString().replace("-", ""),
                    "Old!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
        }

        private void insert(TestUser user) {
            insertWithRole(user, "ROLE_USER");
        }

        private void insertWithRole(TestUser user, String roleCode) {
            UUID roleId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = ?",
                    UUID.class, roleCode);
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic core user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), roleId);
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

    private static final class RootBusinessFixture {
        private final JdbcTemplate jdbc;
        private final TestUser operator;
        private final TestUser target;
        private final UUID role = UUID.randomUUID();
        private final UUID assignment = UUID.randomUUID();
        private final UUID scope = UUID.randomUUID();
        private final UUID visibleMeeting = UUID.randomUUID();
        private final UUID hiddenMeeting = UUID.randomUUID();
        private final UUID participant = UUID.randomUUID();

        private RootBusinessFixture(JdbcTemplate jdbc, TestUser operator, TestUser target) {
            this.jdbc = jdbc;
            this.operator = operator;
            this.target = target;
        }

        private void insert() {
            UUID permission = jdbc.queryForObject("SELECT id FROM spectra_security.sec_permission WHERE code = ?",
                    UUID.class, "oa:meeting:read");
            String code = "ROLE_ROOT_SCOPE_" + role.toString().replace("-", "").toUpperCase();
            jdbc.update("INSERT INTO spectra_security.sec_role (id, code, name, authority_level) "
                    + "VALUES (?, ?, ?, 100)", role, code, code);
            jdbc.update("INSERT INTO spectra_security.sec_role_permission (role_id, permission_id) VALUES (?, ?)",
                    role, permission);
            jdbc.update("INSERT INTO spectra_security.sec_authorization_scope "
                    + "(id, scope_mode, resource_code) VALUES (?, 'SELF', 'oa:meeting')", scope);
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    assignment, target.id(), role);
            jdbc.update("INSERT INTO spectra_security.sec_assignment_permission_boundary "
                    + "(assignment_id, permission_id, scope_id) VALUES (?, ?, ?)", assignment, permission, scope);
            jdbc.update("INSERT INTO spectra_oa.oa_meeting "
                    + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                    + "VALUES (?, ?, ?, now(), now(), ?, now(), now())",
                    visibleMeeting, "synthetic visible", operator.id(), operator.id());
            jdbc.update("INSERT INTO spectra_oa.oa_meeting "
                    + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                    + "VALUES (?, ?, ?, now(), now(), ?, now(), now())",
                    hiddenMeeting, "synthetic hidden", operator.id(), operator.id());
            jdbc.update("INSERT INTO spectra_oa.oa_meeting_participant "
                    + "(id, meeting_id, user_id, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
                    participant, visibleMeeting, target.id());
        }

        private String participantStatus() {
            return jdbc.queryForObject("SELECT status FROM spectra_oa.oa_meeting_participant WHERE id = ?",
                    String.class, participant);
        }

        private void clean() {
            jdbc.update("DELETE FROM spectra_oa.oa_meeting_participant WHERE id = ?", participant);
            jdbc.update("DELETE FROM spectra_oa.oa_meeting WHERE id IN (?, ?)", visibleMeeting, hiddenMeeting);
            jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id = ?",
                    assignment);
            jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id = ?", assignment);
            jdbc.update("DELETE FROM spectra_security.sec_authorization_scope WHERE id = ?", scope);
            jdbc.update("DELETE FROM spectra_security.sec_role_permission WHERE role_id = ?", role);
            jdbc.update("DELETE FROM spectra_security.sec_role WHERE id = ?", role);
        }
    }

    /** 第二个 JVM 中启动完整应用，仅通过共享数据库与 Redis 交换状态。 */
    public static final class SecondApplication {
        private SecondApplication() {
        }

        public static void main(String[] args) throws Exception {
            Path ready = Path.of(args[0]);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                Files.writeString(ready, Integer.toString(port));
                new CountDownLatch(1).await();
            }
        }
    }
}
