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

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用两个独立 JVM 的完整应用 HTTP 链路验证共享 Redis Session 撤销。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/12
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class SessionTwoProcessHttpTest {

    private static final String WEB_POLICY_SQL = """
            UPDATE spectra_security.sec_session_policy
            SET access_ttl_seconds = ?, refresh_ttl_seconds = ?, absolute_ttl_seconds = ?, idle_ttl_seconds = ?
            WHERE client_id = (SELECT id FROM spectra_security.sec_security_client WHERE code = 'web')
            """;

    private static final String WEB_CONCURRENCY_SQL = """
            UPDATE spectra_security.sec_session_policy
            SET concurrency_mode = ?, max_sessions = ?
            WHERE client_id = (SELECT id FROM spectra_security.sec_security_client WHERE code = 'web')
            """;

    @Test
    void logoutAndUserRevocationRejectOldTokenAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var identity = first.getBean(SyntheticIdentity.class);
                var issuer = first.getBean(SecuritySessionIssuer.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);

                assertEquals(401, contextStatus(client, secondPort, null));
                String firstToken = issuer.createToken(identity.load(userId), ClientType.WEB).getAccessToken();
                assertEquals(200, contextStatus(client, secondPort, firstToken));
                assertEquals(200, logoutStatus(client, firstPort, firstToken));
                assertEquals(401, contextStatus(client, firstPort, firstToken));
                assertEquals(401, contextStatus(client, secondPort, firstToken));

                String secondToken = issuer.createToken(identity.load(userId), ClientType.WEB).getAccessToken();
                assertEquals(200, contextStatus(client, secondPort, secondToken));
                revocation.revokeUserSessions(userId);
                assertEquals(401, contextStatus(client, firstPort, secondToken));
                assertEquals(401, contextStatus(client, secondPort, secondToken));

                String renewedToken = issuer.createToken(identity.load(userId), ClientType.WEB).getAccessToken();
                assertEquals(200, contextStatus(client, secondPort, renewedToken));
                revocation.revokeUserSessions(userId);
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void naturalTokenExpiryAndPolicyShrinkAreSharedAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-lifetime-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var jdbc = first.getBean(JdbcTemplate.class);
                var original = webPolicy(jdbc);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                try {
                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, 3, 30, null, null));
                    var identity = first.getBean(SyntheticIdentity.class);
                    var issuer = first.getBean(SecuritySessionIssuer.class);
                    var properties = first.getBean(SecurityProperties.class);
                    var mapper = first.getBean(ObjectMapper.class);
                    SecurityToken issued = issuer.createToken(identity.load(userId), ClientType.WEB);
                    String access = issued.getAccessToken();
                    assertEquals(200, contextStatus(client, secondPort, access));
                    awaitRedisExpiry(first, SecurityRedisKey.SESSION.format(TokenDigestService.digest(access)), 8);
                    assertEquals(401, contextStatus(client, firstPort, access));
                    assertEquals(401, contextStatus(client, secondPort, access));

                    var refreshed = refresh(client, secondPort, issued.getRefreshToken(), properties);
                    assertEquals(200, refreshed.statusCode());
                    String replacementAccess = mapper.readTree(refreshed.body())
                            .path("data")
                            .path("access_token")
                            .asText();
                    assertFalse(replacementAccess.isBlank());
                    assertEquals(200, contextStatus(client, firstPort, replacementAccess));

                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, 3, 30, 1, null));
                    assertEquals(401, contextStatus(client, firstPort, replacementAccess));
                    assertEquals(401, contextStatus(client, secondPort, replacementAccess));
                    String replacementRefresh = refreshCookie(refreshed, properties);
                    assertEquals(401, refresh(client, firstPort, replacementRefresh, properties).statusCode());

                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, 3, 5, null, null));
                    SecurityToken expiring = issuer.createToken(identity.load(userId), ClientType.WEB);
                    assertEquals(200, contextStatus(client, secondPort, expiring.getAccessToken()));
                    awaitRedisExpiry(first, SecurityRedisKey.REFRESH_TOKEN.format(
                            TokenDigestService.digest(expiring.getRefreshToken())), 9);
                    assertEquals(401, refresh(client, secondPort, expiring.getRefreshToken(), properties).statusCode());
                } finally {
                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, original.accessTtlSeconds(),
                            original.refreshTtlSeconds(), original.absoluteTtlSeconds(), original.idleTtlSeconds()));
                    revocation.revokeUserSessions(userId);
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void refreshReplayIsFencedAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-replay-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var identity = first.getBean(SyntheticIdentity.class);
                var issuer = first.getBean(SecuritySessionIssuer.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var properties = first.getBean(SecurityProperties.class);
                var mapper = first.getBean(ObjectMapper.class);
                try {
                    SecurityToken issued = issuer.createToken(identity.load(userId), ClientType.WEB);
                    var rotated = refresh(client, secondPort, issued.getRefreshToken(), properties);
                    assertEquals(200, rotated.statusCode());
                    String replacementAccess = mapper.readTree(rotated.body())
                            .path("data")
                            .path("access_token")
                            .asText();
                    assertFalse(replacementAccess.isBlank());
                    assertEquals(200, contextStatus(client, firstPort, replacementAccess));

                    assertEquals(401, refresh(client, firstPort, issued.getRefreshToken(), properties).statusCode());
                    assertEquals(401, contextStatus(client, firstPort, replacementAccess));
                    assertEquals(401, contextStatus(client, secondPort, replacementAccess));
                    assertEquals(401, refresh(client, secondPort, refreshCookie(rotated, properties),
                            properties).statusCode());
                } finally {
                    revocation.revokeUserSessions(userId);
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void concurrentRefreshClaimsOnceAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-concurrent-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var identity = first.getBean(SyntheticIdentity.class);
                var issuer = first.getBean(SecuritySessionIssuer.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var properties = first.getBean(SecurityProperties.class);
                var mapper = first.getBean(ObjectMapper.class);
                try {
                    SecurityToken issued = issuer.createToken(identity.load(userId), ClientType.WEB);
                    var firstFuture = refreshAsync(client, firstPort, issued.getRefreshToken(), properties);
                    var secondFuture = refreshAsync(client, secondPort, issued.getRefreshToken(), properties);
                    HttpResponse<String> firstResponse = firstFuture.get(30, TimeUnit.SECONDS);
                    HttpResponse<String> secondResponse = secondFuture.get(30, TimeUnit.SECONDS);
                    assertTrue(List.of(firstResponse, secondResponse)
                            .stream()
                            .anyMatch(response -> response.statusCode() == 401),
                            "至少一个并发刷新必须作为重放被拒绝");
                    for (HttpResponse<String> response : List.of(firstResponse, secondResponse)) {
                        if (response.statusCode() == 200) {
                            String access = mapper.readTree(response.body())
                                    .path("data")
                                    .path("access_token")
                                    .asText();
                            assertFalse(access.isBlank());
                            assertEquals(401, contextStatus(client, firstPort, access));
                            assertEquals(401, contextStatus(client, secondPort, access));
                        } else {
                            assertEquals(401, response.statusCode());
                        }
                    }
                    assertEquals(401, refresh(client, firstPort, issued.getRefreshToken(), properties).statusCode());
                } finally {
                    revocation.revokeUserSessions(userId);
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void rejectNewSessionQuotaIsSharedAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-quota-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var jdbc = first.getBean(JdbcTemplate.class);
                var original = webConcurrency(jdbc);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var identity = first.getBean(SyntheticIdentity.class);
                var issuer = first.getBean(SecuritySessionIssuer.class);
                var redis = securityRedisTemplate(first);
                try {
                    SecurityToken control = issuer.createToken(identity.load(userId), ClientType.WEB);
                    assertEquals(200, issueSessionStatus(client, secondPort, control.getAccessToken()),
                            "第二进程的合成签发入口应可正常签发");
                    revocation.revokeUserSessions(userId);

                    assertEquals(1, jdbc.update(WEB_CONCURRENCY_SQL, "REJECT_NEW", 1));
                    SecurityToken session = issuer.createToken(identity.load(userId), ClientType.WEB);
                    assertEquals(200, contextStatus(client, secondPort, session.getAccessToken()));

                    assertEquals(409, issueSessionStatus(client, secondPort, session.getAccessToken()),
                            "共享配额应拒绝第二进程的新会话");

                    assertEquals(Boolean.TRUE, redis.hasKey(
                            SecurityRedisKey.SESSION.format(TokenDigestService.digest(session.getAccessToken()))));
                    Set<Object> active = redis.opsForSet()
                            .members(SecurityRedisKey.USER_TOKENS.format(userId.toString()));
                    assertNotNull(active);
                    assertEquals(1, active.size());
                    assertEquals(200, contextStatus(client, firstPort, session.getAccessToken()));
                } finally {
                    assertEquals(1, jdbc.update(WEB_CONCURRENCY_SQL, original.mode(), original.maxSessions()));
                    revocation.revokeUserSessions(userId);
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void revocationAfterAccessExpiryRejectsRefreshAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "session-expired-revoke-two-process-");
        UUID userId = UUID.randomUUID();
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run(applicationArgs(userId))) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(userId, ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var jdbc = first.getBean(JdbcTemplate.class);
                var original = webPolicy(jdbc);
                var identity = first.getBean(SyntheticIdentity.class);
                var issuer = first.getBean(SecuritySessionIssuer.class);
                var revocation = first.getBean(SecuritySessionRevocationPort.class);
                var properties = first.getBean(SecurityProperties.class);
                try {
                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, 3, original.refreshTtlSeconds(),
                            original.absoluteTtlSeconds(), original.idleTtlSeconds()));
                    SecurityToken doomed = issuer.createToken(identity.load(userId), ClientType.WEB);
                    SecurityToken kept = issuer.createToken(identity.load(userId), ClientType.WEB);
                    assertEquals(200, contextStatus(client, secondPort, kept.getAccessToken()));
                    awaitRedisExpiry(first, SecurityRedisKey.SESSION.format(
                            TokenDigestService.digest(kept.getAccessToken())), 8);
                    assertEquals(401, contextStatus(client, firstPort, kept.getAccessToken()));
                    assertEquals(401, contextStatus(client, secondPort, kept.getAccessToken()));

                    revocation.revokeUserSessionsExceptToken(userId, kept.getAccessToken());
                    assertEquals(401, refresh(client, secondPort, doomed.getRefreshToken(), properties).statusCode());
                    var rotated = refresh(client, secondPort, kept.getRefreshToken(), properties);
                    assertEquals(200, rotated.statusCode());

                    revocation.revokeUserSessions(userId);
                    assertEquals(401, refresh(client, firstPort, refreshCookie(rotated, properties),
                            properties).statusCode());
                } finally {
                    assertEquals(1, jdbc.update(WEB_POLICY_SQL, original.accessTtlSeconds(),
                            original.refreshTtlSeconds(), original.absoluteTtlSeconds(), original.idleTtlSeconds()));
                    revocation.revokeUserSessions(userId);
                }
            } finally {
                stop(second);
            }
        }
    }

    private static CompletableFuture<HttpResponse<String>> refreshAsync(HttpClient client, int port, String token,
                                                                        SecurityProperties properties) {
        String csrf = UUID.randomUUID().toString();
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/refresh"))
                .header("Api-Version", "1.0.0")
                .header("Cookie", properties.getRefreshCookieName() + "=" + token + "; "
                        + properties.getCsrfCookieName() + "=" + csrf)
                .header(properties.getCsrfHeaderName(), csrf)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static int issueSessionStatus(HttpClient client, int port, String accessToken) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/test/session/issue"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static WebConcurrency webConcurrency(JdbcTemplate jdbc) {
        return jdbc.queryForObject("""
                SELECT policy.concurrency_mode, policy.max_sessions
                FROM spectra_security.sec_session_policy policy
                JOIN spectra_security.sec_security_client client ON client.id = policy.client_id
                WHERE client.code = 'web'
                """,
                (row, _) -> new WebConcurrency(row.getString("concurrency_mode"), row.getInt("max_sessions")));
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> securityRedisTemplate(ApplicationContext application) {
        return (RedisTemplate<String, Object>) application.getBean("securityRedisTemplate");
    }

    private record WebConcurrency(String mode, int maxSessions) {
    }

    private static WebPolicy webPolicy(JdbcTemplate jdbc) {
        return jdbc.queryForObject("""
                SELECT policy.access_ttl_seconds, policy.refresh_ttl_seconds,
                       policy.absolute_ttl_seconds, policy.idle_ttl_seconds
                FROM spectra_security.sec_session_policy policy
                JOIN spectra_security.sec_security_client client ON client.id = policy.client_id
                WHERE client.code = 'web'
                """,
                (row, _) -> new WebPolicy(row.getInt("access_ttl_seconds"), row.getInt("refresh_ttl_seconds"),
                        row.getObject("absolute_ttl_seconds", Integer.class),
                        row.getObject("idle_ttl_seconds", Integer.class)));
    }

    @SuppressWarnings("unchecked")
    private static void awaitRedisExpiry(ApplicationContext application, String key, int timeoutSeconds)
            throws InterruptedException {
        var redis = (RedisTemplate<String, Object>) application.getBean("securityRedisTemplate");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        while (Boolean.TRUE.equals(redis.hasKey(key)) && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        assertEquals(Boolean.FALSE, redis.hasKey(key));
    }

    private static HttpResponse<String> refresh(HttpClient client, int port, String token, SecurityProperties properties)
            throws Exception {
        String csrf = UUID.randomUUID().toString();
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/refresh"))
                .header("Api-Version", "1.0.0")
                .header("Cookie", properties.getRefreshCookieName() + "=" + token + "; "
                        + properties.getCsrfCookieName() + "=" + csrf)
                .header(properties.getCsrfHeaderName(), csrf)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String refreshCookie(HttpResponse<String> response, SecurityProperties properties) {
        String prefix = properties.getRefreshCookieName() + "=";
        return response.headers()
                .allValues("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith(prefix))
                .map(cookie -> cookie.substring(prefix.length()).split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("Refresh 响应缺少 Web Cookie"));
    }

    private record WebPolicy(int accessTtlSeconds, int refreshTtlSeconds,
                             Integer absoluteTtlSeconds, Integer idleTtlSeconds) {
    }

    private static void requireIsolatedDependencies() {
        String databaseUrl = System.getenv("DB_URL");
        if (databaseUrl == null
                || !databaseUrl.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
    }

    private static String[] applicationArgs(UUID userId) {
        return new String[]{"--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                "--spectra.test.session-http=true", "--spectra.test.session-probe-user-id=" + userId};
    }

    private static Process startSecond(UUID userId, Path ready, Path log) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        return new ProcessBuilder(java, "-Duser.timezone=UTC", "-cp", System.getProperty("java.class.path"),
                SecondApplication.class.getName(), userId.toString(), ready.toString())
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

    private static int contextStatus(HttpClient client, int port, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return client.send(request.GET().build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static int logoutStatus(HttpClient client, int port, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/logout"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "spectra.test.session-http", havingValue = "true")
    static class SyntheticConfiguration {
        @Bean
        @Primary
        SyntheticIdentity syntheticIdentity(@Value("${spectra.test.session-probe-user-id}") UUID userId) {
            return new SyntheticIdentity(userId);
        }

        @Bean
        @Primary
        AuthorizationSnapshotProvider syntheticSnapshotProvider() {
            return ignored -> AuthorizationSnapshot.of(List.of());
        }
    }

    static class SyntheticIdentity implements SecurityUserLoader {
        private final UUID userId;

        SyntheticIdentity(UUID userId) {
            this.userId = userId;
        }

        @Override
        public SecurityPrincipal load(UUID requested) {
            if (!userId.equals(requested)) {
                return null;
            }
            var user = new SecurityUser();
            user.setId(userId);
            user.setUsername("synthetic-session-user");
            user.setName("synthetic session user");
            user.setTimezone("UTC");
            user.setAuthorities(List.of(new SimpleGrantedAuthority("account:read")));
            return user;
        }
    }

    /** 第二个 JVM 中仅启动完整应用，不通过共享内存或测试端口交换会话状态。 */
    public static final class SecondApplication {
        private SecondApplication() {
        }

        public static void main(String[] args) throws Exception {
            UUID userId = UUID.fromString(args[0]);
            Path ready = Path.of(args[1]);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                    .run(applicationArgs(userId))) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                Files.writeString(ready, Integer.toString(port));
                new CountDownLatch(1).await();
            }
        }
    }
}
