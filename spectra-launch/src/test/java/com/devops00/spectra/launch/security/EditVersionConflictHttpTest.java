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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用正式 HTTP 验证普通编辑入口的读取版本、清空持久化和稳定 409 冲突。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class EditVersionConflictHttpTest {

    private static final String API_VERSION = "1.0.0";

    private ApplicationContext application;
    private HttpClient client;
    private ObjectMapper mapper;
    private JdbcTemplate jdbc;
    private PasswordEncoder encoder;
    private UUID departmentId;
    private TestUser operator;
    private String token;

    @BeforeAll
    void startApplication() throws Exception {
        requireIsolatedDependencies();
        application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false");
        mapper = application.getBean(ObjectMapper.class);
        jdbc = new JdbcTemplate(application.getBean(DataSource.class));
        encoder = application.getBean(PasswordEncoder.class);
        client = HttpClient.newHttpClient();
        departmentId = UUID.randomUUID();
        jdbc.update("INSERT INTO spectra_core.sys_department (id, name, code, created_at, updated_at) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", departmentId,
                "synthetic edit version department", "b04edit" + departmentId.toString().replace("-", ""));
        operator = new TestUser(UUID.randomUUID(),
                "b04edit" + UUID.randomUUID().toString().replace("-", ""),
                "Ed!" + UUID.randomUUID());
        insertLoginUser(operator);
        token = login(operator);
    }

    @AfterAll
    void stopApplication() {
        if (operator != null) {
            deleteUser(operator.id());
        }
        if (departmentId != null) {
            jdbc.update("DELETE FROM spectra_core.sys_department WHERE id = ?", departmentId);
        }
        if (application != null) {
            ((org.springframework.context.ConfigurableApplicationContext) application).close();
        }
    }

    @Test
    void normalUserEditRejectsStaleVersionAndPersistsClearedFields() throws Exception {
        UUID userId = UUID.randomUUID();
        String username = "b04target" + UUID.randomUUID().toString().replace("-", "");
        insertPlainUser(userId, username);
        try {
            var edit = mapper.createObjectNode();
            var user = edit.putObject("user");
            user.put("id", userId.toString())
                    .put("version", 0)
                    .put("employee_no", username)
                    .put("real_name", "synthetic edited user")
                    .put("status", "ACTIVE")
                    .put("username", username)
                    .put("primary_department_id", departmentId.toString())
                    .putArray("associated_department_ids");
            user.putNull("language").putNull("timezone");
            var authorization = edit.putObject("authorization");
            authorization.putArray("assignments");
            authorization.putArray("removed_assignments");

            var saved = call("PUT", "/api/user/onboarding", edit);
            assertEquals(200, saved.statusCode(), saved.body());
            assertEquals(1, jdbc.queryForObject("""
                    SELECT count(*) FROM spectra_core.sys_user
                    WHERE id = ? AND language IS NULL AND timezone IS NULL AND version = 1
                    """, Integer.class, userId));

            var stale = call("PUT", "/api/user/onboarding", edit);
            assertConflict(stale);

            user.put("version", 1).put("phone", "13800000000");
            var contactEdit = call("PUT", "/api/user/onboarding", edit);
            assertEquals(400, contactEdit.statusCode(), contactEdit.body());
            assertEquals("BUSINESS_RULE_VIOLATION",
                    mapper.readTree(contactEdit.body()).path("error_code").asString());
            assertEquals(1, jdbc.queryForObject("""
                    SELECT count(*) FROM spectra_core.sys_user
                    WHERE id = ? AND real_name = 'synthetic edited user' AND version = 1
                    """, Integer.class, userId));
        } finally {
            deleteUser(userId);
        }
    }

    @Test
    void omittedVersionAndMissingAuthenticationUseStableErrors() throws Exception {
        var menu = mapper.createObjectNode()
                .put("id", UUID.randomUUID().toString())
                .put("icon", "icon-test")
                .put("menu_type", "DIRECTORY")
                .put("name", "menu without version")
                .put("sort", 1);
        var omitted = call("PUT", "/api/menu/modify", menu);
        assertEquals(400, omitted.statusCode(), omitted.body());
        assertEquals("REQUEST_INVALID", mapper.readTree(omitted.body()).path("error_code").asString());

        var unauthenticated = client.send(HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port() + "/api/user/profile"))
                .header("Api-Version", API_VERSION)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, unauthenticated.statusCode(), unauthenticated.body());
        var body = mapper.readTree(unauthenticated.body());
        assertEquals(401, body.path("code").asInt());
        assertEquals("UNAUTHENTICATED", body.path("error_code").asString());
    }

    @Test
    void reinstatementClearsDepartedAt() throws Exception {
        UUID userId = UUID.randomUUID();
        String username = "b04departed" + UUID.randomUUID().toString().replace("-", "");
        insertDepartedUser(userId, username);
        try {
            var response = call("PUT", "/api/user/reinstate/" + userId + "?reason=synthetic",
                    mapper.createObjectNode());
            assertEquals(200, response.statusCode(), response.body());
            assertEquals(1, jdbc.queryForObject("""
                    SELECT count(*) FROM spectra_core.sys_user
                    WHERE id = ? AND status = 'ACTIVE' AND departed_at IS NULL AND version = 1
                    """, Integer.class, userId));
        } finally {
            deleteUser(userId);
        }
    }

    @Test
    void concurrentProfileUpdatesAcceptOneVersion() throws Exception {
        var profileUser = new TestUser(UUID.randomUUID(),
                "b04profile" + UUID.randomUUID().toString().replace("-", ""),
                "Pr!" + UUID.randomUUID());
        insertLoginUser(profileUser);
        try {
            String profileToken = login(profileUser);
            var initial = call("GET", "/api/user/profile", null, profileToken);
            assertEquals(200, initial.statusCode(), initial.body());
            long version = mapper.readTree(initial.body()).path("data").path("version").asLong();
            var first = mapper.createObjectNode()
                    .put("version", version)
                    .put("real_name", "concurrent a")
                    .putNull("language")
                    .putNull("timezone");
            var second = mapper.createObjectNode()
                    .put("version", version)
                    .put("real_name", "concurrent b")
                    .putNull("language")
                    .putNull("timezone");
            var start = new CountDownLatch(1);
            var left = CompletableFuture.supplyAsync(() -> callUnchecked(start, "/api/user/profile", first, profileToken));
            var right = CompletableFuture.supplyAsync(() -> callUnchecked(start, "/api/user/profile", second, profileToken));
            start.countDown();
            var responses = List.of(left.get(30, TimeUnit.SECONDS), right.get(30, TimeUnit.SECONDS));
            var statuses = responses.stream().map(HttpResponse::statusCode).sorted().toList();
            assertEquals(List.of(200, 409), statuses, responses.toString());
            assertConflict(responses.stream()
                    .filter(response -> response.statusCode() == 409)
                    .findFirst()
                    .orElseThrow());
            assertEquals(version + 1, jdbc.queryForObject(
                    "SELECT version FROM spectra_core.sys_user WHERE id = ?", Long.class, profileUser.id()));
        } finally {
            deleteUser(profileUser.id());
        }
    }

    @Test
    void menuRegionDictAndConfiguredClearFieldsAndRejectStaleVersion() throws Exception {
        UUID parentMenu = UUID.randomUUID();
        UUID childMenu = UUID.randomUUID();
        insertMenu(parentMenu, null, "parent menu");
        insertMenu(childMenu, parentMenu, "child menu");
        UUID dictGroup = UUID.randomUUID();
        UUID dictItem = UUID.randomUUID();
        UUID parentDictGroup = UUID.randomUUID();
        insertDictGroup(parentDictGroup, null, "parent dict");
        insertDictGroup(dictGroup, parentDictGroup, "child dict");
        insertDictItem(dictItem, dictGroup);
        UUID region = UUID.randomUUID();
        insertRegion(region);
        UUID configuredId = UUID.randomUUID();
        String configuredKey = "other.b04." + configuredId.toString().replace("-", "");
        insertConfigured(configuredId, configuredKey);
        try {
            assertMenuClearAndConflict(childMenu);
            assertDictClearAndConflict(dictGroup, dictItem);
            assertRegionClearAndConcurrentConflict(region);
            assertConfiguredClearAndConflict(configuredId);
        } finally {
            jdbc.update("DELETE FROM spectra_core.sys_menu WHERE id IN (?, ?)", childMenu, parentMenu);
            jdbc.update("DELETE FROM spectra_core.sys_dict_item WHERE id = ?", dictItem);
            jdbc.update("DELETE FROM spectra_core.sys_dict_group WHERE id IN (?, ?)", dictGroup, parentDictGroup);
            jdbc.update("DELETE FROM spectra_core.sys_region WHERE id = ?", region);
            jdbc.update("DELETE FROM spectra_core.sys_config WHERE id = ?", configuredId);
        }
    }

    @Test
    void quartzEditVersionRejectsStaleSubmission() throws Exception {
        var jobs = call("GET", "/api/scheduler/quartz/jobs", null);
        assertEquals(200, jobs.statusCode(), jobs.body());
        var records = mapper.readTree(jobs.body()).path("data").path("records");
        assertTrue(records.isArray() && !records.isEmpty(), jobs.body());
        var original = records.get(0);
        for (var record : records) {
            if (!record.path("trigger").path("next_fire_at").asString().isBlank()) {
                original = record;
                break;
            }
        }
        String jobKey = original.path("job_key").asString();
        long version = original.path("version").asLong();
        String parametersJson = original.path("parameters_json").asString();
        if (parametersJson.isBlank()) {
            parametersJson = mapper.writeValueAsString(Map.of("version",
                    original.path("parameter_version").asString("1")));
        }
        var update = mapper.createObjectNode();
        update.put("version", version)
                .put("display_name", original.path("display_name").asString() + " b04")
                .put("parameters_json", parametersJson)
                .set("trigger", original.path("trigger").deepCopy());
        var saved = call("PUT", "/api/scheduler/quartz/jobs/" + URLEncoder.encode(jobKey, StandardCharsets.UTF_8),
                update);
        assertEquals(200, saved.statusCode(), saved.body());
        var savedJob = mapper.readTree(saved.body()).path("data");
        assertEquals(version + 1, savedJob.path("version").asLong());
        String nextFireAt = savedJob.path("trigger").path("next_fire_at").asString();
        assertTrue(nextFireAt.endsWith("Z") || nextFireAt.matches(".*[+-]\\d{2}:\\d{2}$"), nextFireAt);

        var stale = call("PUT", "/api/scheduler/quartz/jobs/" + URLEncoder.encode(jobKey, StandardCharsets.UTF_8),
                update);
        assertConflict(stale);
    }

    private void assertMenuClearAndConflict(UUID menuId) throws Exception {
        var edit = mapper.createObjectNode()
                .put("id", menuId.toString())
                .put("version", 0)
                .put("icon", "icon-test")
                .put("menu_type", "DIRECTORY")
                .put("name", "child menu updated")
                .put("sort", 2);
        edit.putNull("pid").putNull("route_name");
        var saved = call("PUT", "/api/menu/modify", edit);
        assertEquals(200, saved.statusCode(), saved.body());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM spectra_core.sys_menu
                WHERE id = ? AND pid IS NULL AND route_name IS NULL AND version = 1
                """, Integer.class, menuId));
        assertConflict(call("PUT", "/api/menu/modify", edit));
    }

    private void assertDictClearAndConflict(UUID groupId, UUID itemId) throws Exception {
        var groupEdit = mapper.createObjectNode()
                .put("id", groupId.toString())
                .put("version", 0)
                .put("name", "child dict updated")
                .put("code", "b04_dict_" + groupId.toString().replace("-", ""))
                .put("state", true);
        groupEdit.putNull("pid").putNull("remark");
        var saved = call("PUT", "/api/dict/group", groupEdit);
        assertEquals(200, saved.statusCode(), saved.body());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM spectra_core.sys_dict_group
                WHERE id = ? AND pid IS NULL AND remark IS NULL AND version = 1
                """, Integer.class, groupId));
        assertConflict(call("PUT", "/api/dict/group", groupEdit));

        var itemEdit = mapper.createObjectNode()
                .put("id", itemId.toString())
                .put("version", 0)
                .put("gid", groupId.toString())
                .put("label", "dict item")
                .put("value", "b04_value")
                .put("sort", 0)
                .put("state", 0);
        itemEdit.putNull("remark");
        var itemSaved = call("PUT", "/api/dict/data", itemEdit);
        assertEquals(200, itemSaved.statusCode(), itemSaved.body());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM spectra_core.sys_dict_item
                WHERE id = ? AND remark IS NULL AND version = 1
                """, Integer.class, itemId));
        assertConflict(call("PUT", "/api/dict/data", itemEdit));
    }

    private void assertRegionClearAndConcurrentConflict(UUID regionId) throws Exception {
        var edit = mapper.createObjectNode()
                .put("id", regionId.toString())
                .put("version", 0)
                .put("name", "region updated")
                .put("short_name", "short")
                .put("code", "b04_region")
                .put("path", "b04")
                .put("level", "PROVINCES")
                .put("status", true)
                .put("sort", 2);
        edit.putNull("full_name").putNull("pid");
        var saved = call("PUT", "/api/region/modify", edit);
        assertEquals(200, saved.statusCode(), saved.body());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM spectra_core.sys_region
                WHERE id = ? AND full_name IS NULL AND pid IS NULL AND version = 1
                """, Integer.class, regionId));

        var first = edit.deepCopy().put("version", 1).put("name", "concurrent a");
        var second = edit.deepCopy().put("version", 1).put("name", "concurrent b");
        var start = new CountDownLatch(1);
        var left = CompletableFuture.supplyAsync(() -> callUnchecked(start, "/api/region/modify", first));
        var right = CompletableFuture.supplyAsync(() -> callUnchecked(start, "/api/region/modify", second));
        start.countDown();
        var responses = List.of(left.get(30, TimeUnit.SECONDS), right.get(30, TimeUnit.SECONDS));
        var statuses = responses.stream().map(HttpResponse::statusCode).sorted().toList();
        assertEquals(List.of(200, 409), statuses, responses.toString());
        assertConflict(responses.stream().filter(response -> response.statusCode() == 409).findFirst().orElseThrow());
        assertEquals(2, jdbc.queryForObject(
                "SELECT version FROM spectra_core.sys_region WHERE id = ?", Integer.class, regionId));
    }

    private void assertConfiguredClearAndConflict(UUID configuredId) throws Exception {
        var edit = mapper.createObjectNode().put("category", "OTHER");
        edit.putArray("items")
                .addObject()
                .put("id", configuredId.toString())
                .put("version", 0)
                .put("value", "updated value")
                .putNull("remarks");
        var saved = call("PUT", "/api/configured/batch", edit);
        assertEquals(200, saved.statusCode(), saved.body());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM spectra_core.sys_config
                WHERE id = ? AND remarks IS NULL AND value = 'updated value' AND version = 1
                """, Integer.class, configuredId));
        assertConflict(call("PUT", "/api/configured/batch", edit));
    }

    private HttpResponse<String> callUnchecked(CountDownLatch start, String path, ObjectNode body,
                                               String bearerToken) {
        try {
            start.await();
            return call("PUT", path, body, bearerToken);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private HttpResponse<String> callUnchecked(CountDownLatch start, String path, ObjectNode body) {
        return callUnchecked(start, path, body, token);
    }

    private HttpResponse<String> call(String method, String path, Object body) throws Exception {
        return call(method, path, body, token);
    }

    private HttpResponse<String> call(String method, String path, Object body, String bearerToken) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port() + path))
                .header("Api-Version", API_VERSION)
                .header("Authorization", "Bearer " + bearerToken);
        if (body == null) {
            builder.GET();
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(TestUser user) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port()
                + "/api/security/authentication/login"))
                .header("Api-Version", API_VERSION)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", user.username(), "password", user.password()))))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return mapper.readTree(response.body()).path("data").path("access_token").asText();
    }

    private int port() {
        return ((WebServerApplicationContext) application).getWebServer().getPort();
    }

    private void assertConflict(HttpResponse<String> response) throws Exception {
        assertEquals(409, response.statusCode(), response.body());
        assertEquals("ENTITY_UPDATE_CONFLICT", mapper.readTree(response.body()).path("error_code").asString());
    }

    private void insertLoginUser(TestUser user) {
        jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name, primary_department_id) "
                + "VALUES (?, ?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic operator",
                departmentId);
        jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                user.id(), AuthenticationIdentifierHash.digest(user.username()));
        jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                user.id(), encoder.encode(user.password()));
        UUID operatorRole = jdbc.queryForObject(
                "SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'", UUID.class);
        jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                UUID.randomUUID(), user.id(), operatorRole);
    }

    private void insertPlainUser(UUID userId, String username) {
        jdbc.update("INSERT INTO spectra_core.sys_user "
                + "(id, username, employee_no, real_name, primary_department_id, language, timezone) "
                + "VALUES (?, ?, ?, ?, ?, 'en', 'UTC')",
                userId, username, username, "synthetic target user", departmentId);
    }

    private void insertDepartedUser(UUID userId, String username) {
        jdbc.update("INSERT INTO spectra_core.sys_user "
                + "(id, username, employee_no, real_name, primary_department_id, status, departed_at) "
                + "VALUES (?, ?, ?, ?, ?, 'DEPARTED', CURRENT_TIMESTAMP)",
                userId, username, username, "synthetic departed user", departmentId);
    }

    private void insertMenu(UUID id, UUID pid, String name) {
        jdbc.update("INSERT INTO spectra_core.sys_menu "
                + "(id, name, pid, icon, menu_type, route_name, sort, created_at, updated_at, version) "
                + "VALUES (?, ?, ?, 'icon-test', 'DIRECTORY', NULL, 1, CURRENT_TIMESTAMP, "
                + "CURRENT_TIMESTAMP, 0)", id, name, pid);
    }

    private void insertDictGroup(UUID id, UUID pid, String name) {
        jdbc.update("INSERT INTO spectra_core.sys_dict_group "
                + "(id, name, code, pid, state, remark, builtin, hide, created_at, updated_at, version) "
                + "VALUES (?, ?, ?, ?, true, 'initial remark', false, false, CURRENT_TIMESTAMP, "
                + "CURRENT_TIMESTAMP, 0)", id, name, "b04_dict_" + id.toString().replace("-", ""), pid);
    }

    private void insertDictItem(UUID id, UUID groupId) {
        jdbc.update("INSERT INTO spectra_core.sys_dict_item "
                + "(id, gid, label, value, sort, state, remark, default_flag, created_at, updated_at, version) "
                + "VALUES (?, ?, 'dict item', 'b04_value', 0, 0, 'initial remark', false, "
                + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)", id, groupId);
    }

    private void insertRegion(UUID id) {
        jdbc.update("INSERT INTO spectra_core.sys_region "
                + "(id, pid, name, full_name, short_name, code, path, level, status, sort, created_at, "
                + "updated_at, version) VALUES (?, ?, 'region', 'full region', 'short', 'b04_region', "
                + "'b04', 1, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                id, UUID.randomUUID());
    }

    private void insertConfigured(UUID id, String key) {
        jdbc.update("INSERT INTO spectra_core.sys_config "
                + "(id, key, value, type, dict_code, remarks, created_at, updated_at, version) "
                + "VALUES (?, ?, 'initial value', 0, NULL, 'initial remark', CURRENT_TIMESTAMP, "
                + "CURRENT_TIMESTAMP, 0)", id, key);
    }

    private void deleteUser(UUID userId) {
        jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN "
                + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", userId);
        jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN "
                + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", userId);
        jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM spectra_core.sys_user_department_membership WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM spectra_security.sec_user_contact WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", userId);
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
}
