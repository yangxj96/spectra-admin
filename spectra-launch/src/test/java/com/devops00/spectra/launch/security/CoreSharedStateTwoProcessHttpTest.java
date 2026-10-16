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
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisNamespace;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 两个独立应用 JVM 共享 Redis 时，验证登录失败身份桶与绑定验证码尝试预算均为进程间共享。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CoreSharedStateTwoProcessHttpTest {

    private static final String WRONG_CODE = "111111";

    @Test
    void loginFailureBucketAndBindingBudgetAreSharedAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "core-shared-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class).run(coreArgs())) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                var mapper = first.getBean(ObjectMapper.class);
                var properties = first.getBean(SecurityProperties.class);
                var codes = first.getBean(SecurityVerificationCodeStore.class);
                var secrets = first.getBean(SecretRuntimeService.class);
                var secretManagement = first.getBean(SecretManagementService.class);
                var redis = securityRedisTemplate(first);
                var fixture = new Fixture(first);
                TestUser locked = TestUser.create("b02lk");
                TestUser binder = TestUser.create("b02bd");
                UUID secretVersionId = null;
                try {
                    fixture.insert(locked);
                    fixture.insertWithRole(binder, "ROLE_DEV_OPS");

                    int lockout = properties.getLockoutMaxAttempts();
                    for (int attempt = 0; attempt < lockout - 1; attempt++) {
                        clearLoginRateLimit(redis);
                        assertEquals(401, passwordLogin(client, firstPort, mapper, locked.username(), wrong(attempt))
                                .statusCode());
                    }
                    clearLoginRateLimit(redis);
                    assertEquals(401, passwordLogin(client, secondPort, mapper, locked.username(), "another-wrong")
                            .statusCode());
                    clearLoginRateLimit(redis);
                    assertEquals(401, passwordLogin(client, firstPort, mapper, locked.username(), locked.password())
                            .statusCode(), "首进程失败 + 第二进程失败应共享同一身份桶并锁定");

                    secretVersionId = secretManagement.createPending("security.verification-code-hmac",
                            randomSecret(), "GENERATED").getId();
                    secretManagement.publish(secretVersionId);

                    String email = "b02shared" + UUID.randomUUID().toString().replace("-", "") + "@example.test";
                    String code = "246813";
                    codes.save(RedisCacheKey.BIND_EMAIL_CODE + email, binder.id() + ":"
                            + VerificationCodeDigest.digest(code, secrets.requireActiveValue("security.verification-code-hmac")),
                            Duration.ofMinutes(5));
                    String binderToken = login(client, firstPort, mapper, binder.username(), binder.password());

                    int budget = properties.getVerificationCodeMaxAttempts();
                    for (int attempt = 0; attempt < budget - 1; attempt++) {
                        assertEquals(400, bindEmail(client, firstPort, mapper, binderToken, email, WRONG_CODE)
                                .statusCode());
                    }
                    assertEquals(400, bindEmail(client, secondPort, mapper, binderToken, email, WRONG_CODE)
                            .statusCode());
                    assertFalse(Boolean.TRUE.equals(redis.hasKey(RedisCacheKey.BIND_EMAIL_CODE + email)),
                            "首进程与第二进程的尝试应共享预算并在耗尽时删除验证码");
                    assertEquals(400, bindEmail(client, firstPort, mapper, binderToken, email, code).statusCode(),
                            "耗尽后即使正确验证码也应被拒绝");
                } finally {
                    if (secretVersionId != null) {
                        fixture.deleteSecretVersion(secretVersionId);
                    }
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
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
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    private static String wrong(int attempt) {
        return "wrong-" + attempt + "-" + UUID.randomUUID();
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = passwordLogin(client, port, mapper, username, password);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).path("data").path("access_token").asText();
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
        private static TestUser create(String prefix) {
            return new TestUser(UUID.randomUUID(), prefix + UUID.randomUUID().toString().replace("-", ""),
                    "Sh!" + UUID.randomUUID());
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
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic shared user");
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

        private void deleteSecretVersion(UUID versionId) {
            jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", versionId);
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

    /** 第二个 JVM 中启动完整应用，仅共享数据库与 Redis。 */
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
