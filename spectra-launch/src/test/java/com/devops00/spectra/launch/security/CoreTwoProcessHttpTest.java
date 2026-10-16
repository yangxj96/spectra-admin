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
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.launch.LaunchApplication;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    private static Process startSecond(Path ready, Path log) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        return new ProcessBuilder(java, "-Duser.timezone=UTC", "-cp", System.getProperty("java.class.path"),
                SecondApplication.class.getName(), ready.toString())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
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
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build(), HttpResponse.BodyHandlers.ofString());
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

    private static int context(HttpClient client, int port, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
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
