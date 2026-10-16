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

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用正式 HTTP、正式审计表和 PostgreSQL 验证改密审计与业务同事务且口令已脱敏。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class AuditRedactionFullHttpTest {

    @Test
    void changePasswordAuditIsTransactionalAndRedacted() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            String newPassword = strongPassword();
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                String correlationId = UUID.randomUUID().toString();

                assertEquals(200, changePassword(client, port, mapper, token, correlationId,
                        user.password(), newPassword).statusCode());

                assertEquals(401, passwordLogin(client, port, mapper, user.username(), user.password()).statusCode(),
                        "旧口令应失效");
                login(client, port, mapper, user.username(), newPassword);

                List<Map<String, Object>> records = awaitAudit(jdbc, correlationId);
                Map<String, Object> change = records.stream()
                        .filter(row -> String.valueOf(row.get("event_type")).endsWith("#changePassword"))
                        .findFirst()
                        .orElse(null);
                assertNotNull(change, "改密审计必须与业务同事务写入正式审计表");
                assertEquals("SUCCEEDED", change.get("result"));
                assertTrue(String.valueOf(change.get("before_text")).contains(AuditSanitizer.REDACTED_VALUE),
                        "改密入参快照应包含脱敏标记");
                for (Map<String, Object> row : records) {
                    for (String column : List.of("before_text", "after_text", "reason", "failure_reason")) {
                        String text = String.valueOf(row.get(column));
                        assertTrue(!text.contains(user.password()) && !text.contains(newPassword),
                                "审计 " + column + " 不得包含明文口令");
                    }
                }
            } finally {
                fixture.clean();
            }
        }
    }

    private static List<Map<String, Object>> awaitAudit(JdbcTemplate jdbc, String correlationId)
            throws InterruptedException {
        String sql = "SELECT event_type, result, before_snapshot::text AS before_text, "
                + "after_snapshot::text AS after_text, coalesce(reason, '') AS reason, "
                + "coalesce(failure_reason, '') AS failure_reason "
                + "FROM spectra_core.sys_audit_event WHERE correlation_id = ?";
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, correlationId);
            if (rows.stream().anyMatch(row -> String.valueOf(row.get("event_type")).endsWith("#changePassword"))) {
                return rows;
            }
            Thread.sleep(50);
        }
        return jdbc.queryForList(sql, correlationId);
    }

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = passwordLogin(client, port, mapper, username, password);
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        assertTrue(!token.isBlank());
        return token;
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

    private static HttpResponse<String> changePassword(HttpClient client, int port, ObjectMapper mapper, String token,
                                                       String correlationId, String oldPassword, String newPassword)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/user/password"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("X-Correlation-ID", correlationId)
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "old_password", oldPassword, "new_password", newPassword, "verify_password", newPassword))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String strongPassword() {
        return "Audit1!" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
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
                    "b02audit" + UUID.randomUUID().toString().replace("-", ""),
                    "Old!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID userRole;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.userRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_USER'",
                    UUID.class);
        }

        private void insert(TestUser user) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic audit user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), userRole);
            users.add(user);
        }

        private void clean() {
            for (TestUser user : users) {
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
