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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用正式 Workflow HTTP 验证审计记录不包含敏感明文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class WorkflowAuditRedactionHttpTest {

    @Test
    void taskAuditDoesNotContainSensitivePlaintext() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            var user = new TestUser(UUID.randomUUID(),
                    "b02wfaudit" + UUID.randomUUID().toString().replace("-", ""),
                    "Wf!" + UUID.randomUUID());
            String marker = "synthetic-wf-secret-" + UUID.randomUUID();
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                        + "/api/workflow/tasks/" + UUID.randomUUID() + "/complete"))
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                                "comment", "password=" + marker + " token:" + marker))))
                        .build(), HttpResponse.BodyHandlers.ofString());

                assertTrue(response.statusCode() >= 400, "不存在的任务应失败: " + response.body());

                Integer leaked = fixture.jdbc.queryForObject("""
                        SELECT count(*) FROM spectra_core.sys_audit_event
                        WHERE operator_id = ?
                          AND (before_snapshot::text LIKE ? OR after_snapshot::text LIKE ?
                               OR reason LIKE ? OR failure_reason LIKE ?)
                        """, Integer.class, user.id(), "%" + marker + "%", "%" + marker + "%",
                        "%" + marker + "%", "%" + marker + "%");
                assertEquals(0, leaked, "Workflow 审计记录不得包含敏感明文");
            } finally {
                fixture.clean();
            }
        }
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
        assertEquals(200, response.statusCode(), response.body());
        return mapper.readTree(response.body()).path("data").path("access_token").asText();
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
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID operatorRole;
        private final UUID departmentId = UUID.randomUUID();
        private TestUser user;

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.operatorRole = jdbc.queryForObject(
                    "SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'", UUID.class);
        }

        private void insert(TestUser user) {
            this.user = user;
            jdbc.update("INSERT INTO spectra_core.sys_department (id, name, code, created_at, updated_at) "
                    + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", departmentId,
                    "synthetic wf department", "b02wf" + departmentId.toString().replace("-", ""));
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name, primary_department_id) "
                    + "VALUES (?, ?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic wf user",
                    departmentId);
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), operatorRole);
        }

        private void clean() {
            if (user == null) {
                return;
            }
            UUID userId = user.id();
            jdbc.update("DELETE FROM spectra_security.sec_user_contact WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN "
                    + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", userId);
            jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN "
                    + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", userId);
            jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", userId);
            jdbc.update("DELETE FROM spectra_core.sys_department WHERE id = ?", departmentId);
        }
    }
}
