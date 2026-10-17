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
 * 使用正式 HTTP、PostgreSQL 与 Redis DB 5 验证会议以外的 Core 分页入口只接受注册排序字段并拒绝恶意排序。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CorePaginationHttpTest {

    @Test
    void corePaginationEntriesRestrictSortFields() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());

                assertEquals(200, get(client, port, token, "/api/user/page?pageNum=1&pageSize=5").statusCode());
                assertEquals(200, get(client, port, token,
                        "/api/user/page?pageNum=1&pageSize=5&orders%5B0%5D.column=username&orders%5B0%5D.asc=true")
                        .statusCode(), "已注册排序字段应被接受");
                assertEquals(400, get(client, port, token,
                        "/api/user/page?orders%5B0%5D.column=username%3Bdrop%20table%20spectra_core.sys_user")
                        .statusCode(), "恶意排序表达式应被拒绝");
                assertEquals(400, get(client, port, token,
                        "/api/user/page?orders%5B0%5D.column=password_hash").statusCode(),
                        "未注册排序字段应被拒绝");

                assertEquals(200, get(client, port, token, "/api/audit/page?pageNum=1&pageSize=5").statusCode());
                assertEquals(400, get(client, port, token, "/api/audit/page?orders%5B0%5D.column=id").statusCode(),
                        "固定排序入口必须拒绝客户端排序");

                assertEquals(200, get(client, port, token, "/api/role/page?pageNum=1&pageSize=5").statusCode());
                assertEquals(400, get(client, port, token,
                        "/api/role/page?orders%5B0%5D.column=name").statusCode(), "未注册排序的角色分页应拒绝排序");
                assertEquals(400, get(client, port, token,
                        "/api/role/page?orders%5B0%5D.column=name%3Bdrop%20table%20spectra_security.sec_role")
                        .statusCode(), "恶意排序表达式应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/scheduler/quartz/jobs?pageNum=1&pageSize=5").statusCode());
                assertEquals(400, get(client, port, token,
                        "/api/scheduler/quartz/jobs?orders%5B0%5D.column=jobKey").statusCode(),
                        "固定排序的 Quartz 任务入口应拒绝客户端排序");

                assertEquals(200, get(client, port, token,
                        "/api/notification/admin/requests?pageNum=1&pageSize=5").statusCode());
                assertEquals(200, get(client, port, token,
                        "/api/notification/admin/requests?pageNum=1&pageSize=5&orders%5B0%5D.column=created_at")
                        .statusCode(), "通知请求分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/notification/admin/requests?orders%5B0%5D.column=recipient_user_id").statusCode(),
                        "未注册的通知请求排序字段应被拒绝");
                assertEquals(400, get(client, port, token,
                        "/api/notification/admin/requests?orders%5B0%5D.column=created_at%3Bdrop%20table%20spectra_core.sys_user")
                        .statusCode(), "恶意通知请求排序表达式应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/notification/admin/deliveries?pageNum=1&pageSize=5").statusCode());
                assertEquals(200, get(client, port, token,
                        "/api/notification/admin/deliveries?pageNum=1&pageSize=5&orders%5B0%5D.column=result_status")
                        .statusCode(), "通知投递分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/notification/admin/deliveries?orders%5B0%5D.column=recipient_user_id").statusCode(),
                        "未注册的通知投递排序字段应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/notification/admin/tasks?pageNum=1&pageSize=5&orders%5B0%5D.column=channel")
                        .statusCode(), "通知任务分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/notification/admin/tasks?orders%5B0%5D.column=recipient_user_id").statusCode(),
                        "未注册的通知任务排序字段应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/file/types/page?pageNum=1&pageSize=5&orders%5B0%5D.column=created_at")
                        .statusCode(), "文件类型分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/file/types/page?orders%5B0%5D.column=name").statusCode(),
                        "未注册的文件类型排序字段应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/file/uploads/page?pageNum=1&pageSize=5&orders%5B0%5D.column=id")
                        .statusCode(), "上传管理分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/file/uploads/page?orders%5B0%5D.column=status").statusCode(),
                        "未注册的上传管理排序字段应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/oa/meeting/page?pageNum=1&pageSize=5&orders%5B0%5D.column=start_time").statusCode(),
                        "会议分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/oa/meeting/page?orders%5B0%5D.column=title").statusCode(),
                        "未注册的会议排序字段应被拒绝");
                assertEquals(200, get(client, port, token,
                        "/api/oa/notice/page?pageNum=1&pageSize=5&orders%5B0%5D.column=publish_at").statusCode(),
                        "公告分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/oa/notice/page?orders%5B0%5D.column=content").statusCode(),
                        "未注册的公告排序字段应被拒绝");
                assertEquals(200, get(client, port, token,
                        "/api/oa/assets/page?pageNum=1&pageSize=5&orders%5B0%5D.column=created_at").statusCode(),
                        "资产分页应接受注册排序字段");
                assertEquals(400, get(client, port, token,
                        "/api/oa/assets/page?orders%5B0%5D.column=name").statusCode(),
                        "未注册的资产排序字段应被拒绝");

                assertEquals(200, get(client, port, token,
                        "/api/workflow/tasks/todo?pageNum=1&pageSize=5").statusCode());
                assertEquals(400, get(client, port, token,
                        "/api/workflow/tasks/todo?orders%5B0%5D.column=id").statusCode(),
                        "固定排序的待办任务入口应拒绝客户端排序");
            } finally {
                fixture.clean();
            }
        }
    }

    private static HttpResponse<String> get(HttpClient client, int port, String token, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
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
        assertEquals(200, response.statusCode());
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
        private static TestUser create() {
            return new TestUser(UUID.randomUUID(),
                    "b02page" + UUID.randomUUID().toString().replace("-", ""),
                    "Pg!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID operatorRole;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.operatorRole = jdbc.queryForObject(
                    "SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'", UUID.class);
        }

        private void insert(TestUser user) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic page user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), operatorRole);
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
