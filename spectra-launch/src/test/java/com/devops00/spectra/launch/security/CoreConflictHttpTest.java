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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 使用正式 HTTP 验证重复角色代码返回 409 且 body error_code 稳定。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CoreConflictHttpTest {

    @Test
    void duplicateRoleCodeReturnsConflictWithStableErrorCode() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            var user = new TestUser(UUID.randomUUID(),
                    "b02conflict" + UUID.randomUUID().toString().replace("-", ""),
                    "Cf!" + UUID.randomUUID());
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/role/editor"))
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                                "name", "duplicate-role",
                                "code", "ROLE_USER",
                                "authority_level", 10,
                                "permission_codes", List.of(),
                                "grantable_permission_codes", List.of(),
                                "menu_ids", List.of()))))
                        .build(), HttpResponse.BodyHandlers.ofString());

                assertEquals(409, response.statusCode(), response.body());
                assertEquals("DATA_EXIST", mapper.readTree(response.body()).path("error_code").asString());
            } finally {
                fixture.clean();
            }
        }
    }

    @Test
    void profileClearPersistsAndStaleVersionReturns409() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            var user = new TestUser(UUID.randomUUID(),
                    "b04profile" + UUID.randomUUID().toString().replace("-", ""),
                    "Pf!" + UUID.randomUUID());
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                var uri = URI.create("http://127.0.0.1:" + port + "/api/user/profile");
                var initial = client.send(HttpRequest.newBuilder(uri)
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, initial.statusCode(), initial.body());
                var initialData = mapper.readTree(initial.body()).path("data");
                long version = initialData.path("version").asLong();
                assertEquals("Asia/Shanghai", initialData.path("timezone").asText());

                var edit = mapper.createObjectNode()
                        .put("version", version)
                        .put("real_name", "synthetic cleared profile")
                        .putNull("language")
                        .putNull("timezone");
                var saved = client.send(HttpRequest.newBuilder(uri)
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(edit)))
                        .build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, saved.statusCode(), saved.body());
                assertEquals(1, fixture.jdbc.queryForObject("""
                        SELECT count(*) FROM spectra_core.sys_user
                        WHERE id = ? AND language IS NULL AND timezone IS NULL AND version = ?
                        """, Integer.class, user.id(), version + 1));

                var reread = client.send(HttpRequest.newBuilder(uri)
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, reread.statusCode(), reread.body());
                var data = mapper.readTree(reread.body()).path("data");
                assertEquals(version + 1, data.path("version").asLong());
                assertEquals("synthetic cleared profile", data.path("real_name").asText());
                assertEquals(false, data.has("language"));
                assertEquals(false, data.has("timezone"));

                var stale = client.send(HttpRequest.newBuilder(uri)
                        .header("Api-Version", "1.0.0")
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(edit)))
                        .build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(409, stale.statusCode(), stale.body());
                assertEquals("ENTITY_UPDATE_CONFLICT",
                        mapper.readTree(stale.body()).path("error_code").asText());
                assertEquals(1, fixture.jdbc.queryForObject("""
                        SELECT count(*) FROM spectra_core.sys_user
                        WHERE id = ? AND real_name = ? AND version = ?
                        """, Integer.class, user.id(), "synthetic cleared profile", version + 1));
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
                    "synthetic conflict department", "b02conflict" + departmentId.toString().replace("-", ""));
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name, primary_department_id) "
                    + "VALUES (?, ?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic conflict user",
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
