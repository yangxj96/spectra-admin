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

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.service.NotificationControlledSendService;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.framework.security.session.SecuritySessionRevocationService;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 在隔离 PostgreSQL 与 Redis 上核对受控发送的正式 HTTP Preview/Apply 授权、漂移和并发消费。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class NotificationControlledSendFullHttpTest {

    @Test
    void previewApplyRejectsInvalidAndChangedInputAndConsumesOnce() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var encoder = application.getBean(PasswordEncoder.class);
            UUID operatorId = UUID.randomUUID();
            UUID recipientId = UUID.randomUUID();
            String username = "b02send" + UUID.randomUUID().toString().replace("-", "");
            String password = "Bs!" + UUID.randomUUID();
            String idempotencyKey = "b02:controlled-send:" + UUID.randomUUID();
            UUID roleAssignmentId = UUID.randomUUID();
            UUID rootRoleId = jdbc.queryForObject(
                    "SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'", UUID.class);
            try (HttpClient client = HttpClient.newHttpClient()) {
                insertUser(jdbc, operatorId, username);
                insertUser(jdbc, recipientId, "b02target" + UUID.randomUUID().toString().replace("-", ""));
                jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                        + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                        operatorId, AuthenticationIdentifierHash.digest(username));
                jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                        + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                        operatorId, encoder.encode(password));
                jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                        roleAssignmentId, operatorId, rootRoleId);
                String token = login(client, port, mapper, username, password);
                UUID templateId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_template "
                        + "WHERE template_group_code = 'system.notice' AND channel = 'IN_APP' "
                        + "AND state = 'PUBLISHED' AND deleted IS NULL", UUID.class);
                var previewBody = Map.of("idempotency_key", idempotencyKey, "purpose", "SYSTEM_NOTICE",
                        "channels", List.of("IN_APP"), "template_version_ids", Map.of("IN_APP", templateId),
                        "audience", Map.of("user_ids", List.of(recipientId)), "parameters", Map.of());

                var anonymous = post(client, port, mapper, "/preview", null, previewBody);
                assertNotEquals(200, anonymous.statusCode(), "无身份不得创建 Preview");
                assertEquals(0L, previewCount(jdbc, operatorId));

                var previewResponse = post(client, port, mapper, "/preview", token, previewBody);
                assertEquals(200, previewResponse.statusCode(), previewResponse.body());
                JsonNode preview = mapper.readTree(previewResponse.body()).path("data");
                assertEquals(1, preview.path("candidate_user_count").asInt());
                assertEquals(1, preview.path("eligible_task_count").asInt());
                String previewId = preview.path("preview_id").asText();
                String previewToken = preview.path("preview_token").asText();
                String requestHash = preview.path("request_hash").asText();
                assertFalse(previewId.isBlank());
                assertFalse(previewToken.isBlank());
                var applyBody = Map.of("preview_id", previewId, "preview_token", previewToken,
                        "request_hash", requestHash);
                var invalidToken = post(client, port, mapper, "/apply", token,
                        Map.of("preview_id", previewId, "preview_token", "wrong-token",
                                "request_hash", requestHash));
                assertNotEquals(200, invalidToken.statusCode());
                assertEquals(0L, requestCount(jdbc, idempotencyKey));

                var directApply = new NotificationControlledSendApplyFrom();
                directApply.setPreviewId(UUID.fromString(previewId));
                directApply.setPreviewToken(previewToken);
                directApply.setRequestHash(requestHash);
                var principal = mock(SecurityPrincipal.class);
                when(principal.getId()).thenReturn(operatorId);
                SecurityContextHolder.getContext()
                        .setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
                try {
                    var service = application.getBean(NotificationControlledSendService.class);
                    directApply.setPreviewToken("wrong-token");
                    assertThrows(DataException.class, () -> service.apply(directApply),
                            "有效 Root 直接调用 Service 应先通过权限检查");
                    directApply.setPreviewToken(previewToken);
                    jdbc.update("UPDATE spectra_security.sec_role_assignment "
                            + "SET valid_until = statement_timestamp() - INTERVAL '1 second' WHERE id = ?",
                            roleAssignmentId);
                    assertThrows(AccessDeniedException.class, () -> service.apply(directApply),
                            "角色自然到期后直接调用 Service 必须拒绝");
                    assertEquals("PREVIEWED", jdbc.queryForObject("SELECT status FROM spectra_notification.ntf_send_preview "
                            + "WHERE id = ?", String.class, directApply.getPreviewId()));
                    assertEquals(0L, requestCount(jdbc, idempotencyKey));
                } finally {
                    jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_until = NULL WHERE id = ?",
                            roleAssignmentId);
                    SecurityContextHolder.clearContext();
                }

                jdbc.update("UPDATE spectra_core.sys_user SET status = 'DISABLED' WHERE id = ?", recipientId);
                var changedAudience = post(client, port, mapper, "/apply", token, applyBody);
                assertNotEquals(200, changedAudience.statusCode(), "受众变化后必须重新 Preview");
                assertEquals(0L, requestCount(jdbc, idempotencyKey));
                jdbc.update("UPDATE spectra_core.sys_user SET status = 'ACTIVE' WHERE id = ?", recipientId);

                var start = new CountDownLatch(1);
                var first = CompletableFuture.supplyAsync(() -> applyWhenReleased(start, client, port, mapper, token, applyBody));
                var second = CompletableFuture.supplyAsync(() -> applyWhenReleased(start, client, port, mapper, token, applyBody));
                start.countDown();
                var firstResponse = first.join();
                var secondResponse = second.join();
                assertTrue(firstResponse.statusCode() == 200 || secondResponse.statusCode() == 200,
                        "并发 Apply 至少一个成功");
                assertEquals(1L, requestCount(jdbc, idempotencyKey), "并发消费只能产生一个逻辑请求");
                UUID requestId = jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_request WHERE idempotency_key = ?",
                        UUID.class, idempotencyKey);
                UUID sentRequestId = requestId;
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_task "
                        + "WHERE notification_request_id = ?", Long.class, sentRequestId));
                var replay = post(client, port, mapper, "/apply", token, applyBody);
                assertEquals(200, replay.statusCode(), replay.body());
                assertTrue(mapper.readTree(replay.body()).path("data").path("idempotent_replay").asBoolean());
                assertEquals(1L, requestCount(jdbc, idempotencyKey));
            } finally {
                SecurityContextHolder.clearContext();
                application.getBean(SecuritySessionRevocationService.class).deleteByUserId(operatorId);
                for (UUID requestId : jdbc.queryForList("SELECT id FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key = ?", UUID.class, idempotencyKey)) {
                    jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                            + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
                }
                jdbc.update("DELETE FROM spectra_notification.ntf_send_preview WHERE operator_user_id = ?", operatorId);
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id = ?", roleAssignmentId);
                jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", operatorId);
                jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", operatorId);
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id IN (?, ?)", operatorId, recipientId);
            }
        }
    }

    private static void insertUser(JdbcTemplate jdbc, UUID id, String username) {
        jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) VALUES (?, ?, ?, ?)",
                id, username, username, "synthetic controlled send user");
    }

    private static long previewCount(JdbcTemplate jdbc, UUID operatorId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_send_preview "
                + "WHERE operator_user_id = ?", Long.class, operatorId);
    }

    private static long requestCount(JdbcTemplate jdbc, String idempotencyKey) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                + "WHERE idempotency_key = ?", Long.class, idempotencyKey);
    }

    private static HttpResponse<String> post(HttpClient client, int port, ObjectMapper mapper, String endpoint,
                                             String token, Map<String, ?> body)
            throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/notification/admin/send" + endpoint))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> postUnchecked(HttpClient client, int port, ObjectMapper mapper, String endpoint,
                                                      String token, Map<String, ?> body) {
        try {
            return post(client, port, mapper, endpoint, token, body);
        } catch (Exception exception) {
            throw new IllegalStateException("受控发送 HTTP 请求失败", exception);
        }
    }

    private static HttpResponse<String> applyWhenReleased(CountDownLatch start, HttpClient client, int port,
                                                          ObjectMapper mapper, String token, Map<String, ?> body) {
        try {
            start.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("并发 Apply 被中断", exception);
        }
        return postUnchecked(client, port, mapper, "/apply", token, body);
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

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
