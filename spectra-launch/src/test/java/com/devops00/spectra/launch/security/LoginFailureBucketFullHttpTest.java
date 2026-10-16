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

import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisNamespace;
import com.devops00.spectra.framework.security.redis.store.SecurityRedisCounter;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 使用正式 HTTP、PostgreSQL 与 Redis DB 5 验证登录失败身份桶的规范化、锁定与成功清理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class LoginFailureBucketFullHttpTest {

    @Test
    void normalizedVariantsShareBucketAndLockAccount() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var redis = securityRedisTemplate(application);
            var properties = application.getBean(SecurityProperties.class);
            var fixture = new Fixture(application);
            int maxAttempts = properties.getLockoutMaxAttempts();
            TestUser victim = TestUser.create();
            String bucket = AuthenticationIdentifierHash.digest(victim.username());
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(victim);
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    clearLoginRateLimit(redis);
                    String variant = attempt % 2 == 0
                            ? victim.username().toUpperCase(Locale.ROOT)
                            : "  " + victim.username() + "  ";
                    assertEquals(401, passwordLogin(client, port, mapper, variant, wrongPassword(attempt)).statusCode(),
                            "规范化后的大小写/空白变体应计入同一身份桶");
                }
                assertEquals(maxAttempts,
                        SecurityRedisCounter.current(redis, SecurityRedisKey.LOGIN_FAIL.format(bucket)),
                        "变体失败应累计到规范化身份桶");
                clearLoginRateLimit(redis);
                assertEquals(401, passwordLogin(client, port, mapper, victim.username(), victim.password()).statusCode(),
                        "达到阈值后正确密码也应因锁定被拒绝");
            } finally {
                fixture.clean();
            }
        }
    }

    @Test
    void successfulLoginClearsIdentityBucket() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var redis = securityRedisTemplate(application);
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            String bucket = AuthenticationIdentifierHash.digest(user.username());
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                for (int attempt = 0; attempt < 2; attempt++) {
                    clearLoginRateLimit(redis);
                    assertEquals(401, passwordLogin(client, port, mapper, user.username(), wrongPassword(attempt))
                            .statusCode());
                }
                assertEquals(2, SecurityRedisCounter.current(redis, SecurityRedisKey.LOGIN_FAIL.format(bucket)));

                clearLoginRateLimit(redis);
                var success = passwordLogin(client, port, mapper, user.username(), user.password());
                assertEquals(200, success.statusCode());
                assertEquals(0, SecurityRedisCounter.current(redis, SecurityRedisKey.LOGIN_FAIL.format(bucket)),
                        "成功登录应清理同一身份桶");
            } finally {
                fixture.clean();
            }
        }
    }

    @Test
    void opsPortClearsLockedIdentityBucketByHttp() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var redis = securityRedisTemplate(application);
            var properties = application.getBean(SecurityProperties.class);
            var fixture = new Fixture(application);
            int maxAttempts = properties.getLockoutMaxAttempts();
            TestUser operator = TestUser.create();
            TestUser victim = TestUser.create();
            String bucket = AuthenticationIdentifierHash.digest(victim.username());
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insertWithRole(operator, "ROLE_DEV_OPS");
                fixture.insert(victim);
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    clearLoginRateLimit(redis);
                    assertEquals(401, passwordLogin(client, port, mapper, victim.username(), wrongPassword(attempt))
                            .statusCode());
                }
                clearLoginRateLimit(redis);
                assertEquals(401, passwordLogin(client, port, mapper, victim.username(), victim.password())
                        .statusCode(), "达到阈值后正确密码应被锁定拒绝");

                clearLoginRateLimit(redis);
                String operatorToken = login(client, port, mapper, operator.username(), operator.password());
                assertEquals(200, clearLoginFailure(client, port, mapper, operatorToken, victim.username())
                        .statusCode(), "运维端口应能定向清理锁定计数");
                assertEquals(0, SecurityRedisCounter.current(redis, SecurityRedisKey.LOGIN_FAIL.format(bucket)),
                        "定向清理应删除该身份桶");
                clearLoginRateLimit(redis);
                assertEquals(200, passwordLogin(client, port, mapper, victim.username(), victim.password())
                        .statusCode(), "清理后正确密码应可登录");
            } finally {
                fixture.clean();
            }
        }
    }

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = passwordLogin(client, port, mapper, username, password);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).path("data").path("access_token").asText();
    }

    private static HttpResponse<String> clearLoginFailure(HttpClient client, int port, ObjectMapper mapper,
                                                          String token, String username)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/cache/admin/security/login-failure/clear"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "username", username, "reason", "synthetic ops clear", "confirmed", true))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String wrongPassword(int attempt) {
        return "wrong!" + attempt + UUID.randomUUID();
    }

    private static HttpResponse<String> passwordLogin(HttpClient client, int port, ObjectMapper mapper,
                                                      String username, String password)
            throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void clearLoginRateLimit(RedisTemplate<String, Object> redis) throws Exception {
        String digest = HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256")
                        .digest("ip:127.0.0.1".getBytes(StandardCharsets.UTF_8)));
        long window = Math.floorDiv(System.currentTimeMillis(), 60_000L);
        for (long candidate = window; candidate <= window + 1; candidate++) {
            redis.delete(SecurityRedisNamespace.PREFIX + "ratelimit:v1:authentication-login:" + digest + ":"
                    + candidate);
        }
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
                    "b02login" + UUID.randomUUID().toString().replace("-", ""),
                    "Pw!" + UUID.randomUUID());
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
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic login user");
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
}
