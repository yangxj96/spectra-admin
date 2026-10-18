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
import com.devops00.spectra.core.security.root.RootPolicyRepository;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 使用真实隔离 Redis、PostgreSQL 与正式 HTTP 验证安全 Redis 停启时的 fail-closed 与恢复。
 *
 * <p>应用通过 {@code --spring.data.redis.url} 指向本次测试启动的隔离 Redis；该 Redis 与共享实例隔离，
 * 可安全停止与重启，不会影响其他用例。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RedisFaultRecoveryHttpTest {

    @Test
    void lastRootRemainsProtectedAcrossIsolatedRedisRestart() throws Exception {
        requireIsolatedDependencies();
        String redisServer = System.getenv("SPECTRA_TEST_REDIS_SERVER");
        if (redisServer == null || redisServer.isBlank()) {
            redisServer = "D:/Develop/Platform/redis/redis-server.exe";
        }
        Path redisHome = Files.createTempDirectory(Path.of("target"), "isolated-root-redis-");
        int redisPort = freePort();
        String redisUrl = "redis://127.0.0.1:" + redisPort + "/5";
        Process redis = startRedis(redisServer, redisHome, redisPort);
        try {
            awaitListening(redisPort, redis);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.data.redis.url=" + redisUrl)) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                var mapper = application.getBean(ObjectMapper.class);
                var rootPolicy = application.getBean(RootPolicyRepository.class);
                var fixture = new Fixture(application);
                TestUser root = TestUser.create();
                try (HttpClient client = HttpClient.newHttpClient()) {
                    try {
                        fixture.insertRoot(root);
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                        String oldToken = login(client, port, mapper, root.username(), root.password());
                        assertEquals(200, context(client, port, oldToken));

                        stop(redis);
                        assertEquals(503, context(client, port, oldToken),
                                "安全 Redis 断开期间旧 Root Access 必须拒绝");
                        assertEquals(503, passwordLogin(client, port, mapper, root.username(), root.password())
                                .statusCode(), "安全 Redis 不可用时 Root 登录必须拒绝");
                        assertEquals(503, disable(client, port, oldToken, root.id()).statusCode(),
                                "安全 Redis 不可用时 Root 写请求必须拒绝");
                        assertEquals("ACTIVE", fixture.status(root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());

                        redis = startRedis(redisServer, redisHome, redisPort);
                        awaitListening(redisPort, redis);
                        loginWithRetry(client, port, mapper, root.username(), root.password());
                        String newToken = login(client, port, mapper, root.username(), root.password());
                        assertEquals(401, context(client, port, oldToken),
                                "重启后的空白隔离 Redis 不应恢复丢失的旧 Session");
                        assertEquals(200, context(client, port, newToken));
                        var rejected = disable(client, port, newToken, root.id());
                        assertEquals(409, rejected.statusCode());
                        assertEquals("ROOT_EFFECTIVE_MINIMUM",
                                mapper.readTree(rejected.body()).path("error_code").asText());
                        assertEquals("ACTIVE", fixture.status(root.id()));
                        assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                    } finally {
                        fixture.clean();
                    }
                }
            }
        } finally {
            if (redis != null && redis.isAlive()) {
                stop(redis);
            }
        }
    }

    @Test
    void redisStopFailsClosedAndRestartRecovers() throws Exception {
        requireIsolatedDependencies();
        String redisServer = System.getenv("SPECTRA_TEST_REDIS_SERVER");
        if (redisServer == null || redisServer.isBlank()) {
            redisServer = "D:/Develop/Platform/redis/redis-server.exe";
        }
        Path redisHome = Files.createTempDirectory(Path.of("target"), "isolated-redis-");
        int redisPort = freePort();
        String redisUrl = "redis://127.0.0.1:" + redisPort + "/5";
        Process redis = startRedis(redisServer, redisHome, redisPort);
        try {
            awaitListening(redisPort, redis);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.data.redis.url=" + redisUrl)) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                var mapper = application.getBean(ObjectMapper.class);
                var fixture = new Fixture(application);
                TestUser user = TestUser.create();
                try (HttpClient client = HttpClient.newHttpClient()) {
                    fixture.insert(user);
                    String token = login(client, port, mapper, user.username(), user.password());

                    stop(redis);
                    assertNotEquals(200, context(client, port, token),
                            "安全 Redis 不可用时受保护请求必须 fail-closed");
                    assertEquals(503, passwordLogin(client, port, mapper, user.username(), user.password())
                            .statusCode(), "安全 Redis 不可用时登录应返回 503");

                    redis = startRedis(redisServer, redisHome, redisPort);
                    awaitListening(redisPort, redis);
                    loginWithRetry(client, port, mapper, user.username(), user.password());
                } finally {
                    fixture.clean();
                }
            }
        } finally {
            if (redis != null && redis.isAlive()) {
                stop(redis);
            }
        }
    }

    private static Process startRedis(String redisServer, Path home, int port) throws Exception {
        return new ProcessBuilder(redisServer, "--port", Integer.toString(port), "--bind", "127.0.0.1",
                "--save", "", "--appendonly", "no", "--dir", home.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .redirectOutput(home.resolve("redis.out").toFile())
                .start();
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
                if (process != null && !process.isAlive()) {
                    throw new IllegalStateException("隔离 Redis 未启动，退出码 " + process.exitValue());
                }
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("隔离 Redis 未在 15 秒内监听端口 " + port);
    }

    private static void stop(Process process) throws InterruptedException {
        process.destroy();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    private static int context(HttpClient client, int port, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
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

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = passwordLogin(client, port, mapper, username, password);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).path("data").path("access_token").asText();
    }

    private static void loginWithRetry(HttpClient client, int port, ObjectMapper mapper, String username,
                                       String password)
            throws Exception {
        // 探针在故障时会退避到 30 秒，且正在执行的 Redis 命令可能再等待 10 秒超时。
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
        int lastStatus = 0;
        while (System.nanoTime() < deadline) {
            try {
                lastStatus = passwordLogin(client, port, mapper, username, password).statusCode();
                if (lastStatus == 200) {
                    return;
                }
            } catch (Exception ignored) {
                // Redis 重启过程中连接可能短暂失败，重试直到就绪。
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("隔离 Redis 重启后登录未恢复，最后 HTTP 状态 " + lastStatus);
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

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
    }

    private record TestUser(UUID id, String username, String password) {
        private static TestUser create() {
            return new TestUser(UUID.randomUUID(),
                    "b02rd" + UUID.randomUUID().toString().replace("-", ""),
                    "Rd!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID userRole;
        private final UUID rootRole;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.userRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_USER'",
                    UUID.class);
            this.rootRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                    UUID.class);
        }

        private void insert(TestUser user) {
            insertWithRole(user, userRole);
        }

        private void insertRoot(TestUser user) {
            insertWithRole(user, rootRole);
        }

        private void insertWithRole(TestUser user, UUID roleId) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic redis user");
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

        private String status(UUID userId) {
            return jdbc.queryForObject("SELECT status FROM spectra_core.sys_user WHERE id = ?",
                    String.class, userId);
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
