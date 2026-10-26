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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.notification.NotificationAudience;
import com.devops00.spectra.common.notification.NotificationAudienceDirectory;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.service.NotificationControlledSendService;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.security.authorization.service.RoleAuthorizationChangeService;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 使用正式 Flyway schema、Core HTTP 登录与角色变更服务验证缩权后的重新登录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/12
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RoleAuthorizationFullHttpTest {

    @Test
    void concurrentRoleApplyRevokesOldSessionAndReloginUsesReducedSnapshot() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class, TestConfiguration.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.test.role-http=true")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert();
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                ObjectMapper mapper = application.getBean(ObjectMapper.class);
                String operatorToken = login(client, port, mapper, fixture.operatorUsername, fixture.password);
                String oldTargetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                assertTrue(hasPermission(context(client, port, mapper, oldTargetToken), "role:grant"));
                assertTrue(hasPermission(context(client, port, mapper, oldTargetToken), "role:read"));

                String path = "/api/security/authorization/roles/" + fixture.targetRole + "/impact-preview";
                String requested = mapper.writeValueAsString(Map.of("expected_version", 0,
                        "authority_level", 100, "permission_codes", List.of("role:read"),
                        "grantable_permission_codes", List.of("role:read")));
                var preview = post(client, port, path, operatorToken, requested);
                assertEquals(200, preview.statusCode());
                String previewToken = mapper.readTree(preview.body()).path("data").path("preview_token").asText();
                assertFalse(previewToken.isBlank());

                String applied = mapper.writeValueAsString(Map.of("expected_version", 0,
                        "authority_level", 100, "permission_codes", List.of("role:read"),
                        "grantable_permission_codes", List.of("role:read"), "preview_token", previewToken));
                String applyPath = "/api/security/authorization/roles/" + fixture.targetRole + "/impact-apply";
                var first = client.sendAsync(request(port, applyPath, operatorToken, applied),
                        HttpResponse.BodyHandlers.ofString());
                var second = client.sendAsync(request(port, applyPath, operatorToken, applied),
                        HttpResponse.BodyHandlers.ofString());
                var statuses = List.of(first.join().statusCode(), second.join().statusCode());
                assertEquals(1, statuses.stream().filter(status -> status == 200).count());
                assertEquals(1, statuses.stream().filter(status -> status == 409).count());

                assertEquals(1L, fixture.roleVersion());
                assertEquals(1L, fixture.targetSecurityVersion());
                assertEquals(0L, fixture.removedPermissionCount());
                assertEquals(0L, fixture.removedBoundaryCount());
                assertEquals(0L, fixture.removedScopeCount());
                assertEquals(1L, fixture.retainedGrantableCount());
                assertEquals(0L, fixture.removedGrantableCount());
                assertEquals(1L, fixture.retainedGrantBoundaryCount());
                assertEquals(0L, fixture.removedGrantBoundaryCount());
                assertEquals(0L, fixture.removedGrantScopeCount());
                assertEquals(401, get(client, port, "/api/security/context", oldTargetToken).statusCode());

                String renewedTargetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                JsonNode reduced = context(client, port, mapper, renewedTargetToken);
                assertTrue(hasPermission(reduced, "role:read"));
                assertFalse(hasPermission(reduced, "role:grant"));
                assertEquals(403, post(client, port, path, renewedTargetToken, requested).statusCode());
            } finally {
                try {
                    var revocation = application.getBean(SecuritySessionRevocationPort.class);
                    revocation.revokeUserSessions(fixture.operator);
                    revocation.revokeUserSessions(fixture.target);
                } finally {
                    fixture.clean();
                }
            }
        }
    }

    @Test
    void roleDefinitionShrinkSerializesWithControlledSendApply() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class, TestConfiguration.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.test.role-http=true")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            String requestKey = "b02:controlled-send:role-shrink:" + UUID.randomUUID();
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert();
                fixture.insertNotificationSendScenario();
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                ObjectMapper mapper = application.getBean(ObjectMapper.class);
                String operatorToken = login(client, port, mapper, fixture.operatorUsername, fixture.password);
                String targetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                UUID templateId = fixture.jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_template "
                        + "WHERE template_group_code = 'system.notice' AND channel = 'IN_APP' "
                        + "AND state = 'PUBLISHED' AND deleted IS NULL", UUID.class);
                var notificationBody = mapper.writeValueAsString(Map.of("idempotency_key", requestKey,
                        "purpose", "SYSTEM_NOTICE", "channels", List.of("IN_APP"),
                        "template_version_ids", Map.of("IN_APP", templateId),
                        "audience", Map.of("user_ids", List.of(fixture.target)), "parameters", Map.of()));
                var sendPreview = post(client, port, "/api/notification/admin/send/preview", targetToken,
                        notificationBody);
                assertEquals(200, sendPreview.statusCode(), sendPreview.body());
                var sendData = mapper.readTree(sendPreview.body()).path("data");
                var sendApply = new NotificationControlledSendApplyFrom();
                sendApply.setPreviewId(UUID.fromString(sendData.path("preview_id").asText()));
                sendApply.setPreviewToken(sendData.path("preview_token").asText());
                sendApply.setRequestHash(sendData.path("request_hash").asText());

                var retainedPermissions = List.of("role:read", "role:grant", "user:read",
                        "notification:send:preview");
                var roleChange = Map.of("expected_version", 0, "authority_level", 100,
                        "permission_codes", retainedPermissions,
                        "grantable_permission_codes", List.of("role:read", "role:grant"));
                String rolePath = "/api/security/authorization/roles/" + fixture.targetRole;
                var rolePreview = post(client, port, rolePath + "/impact-preview", operatorToken,
                        mapper.writeValueAsString(roleChange));
                assertEquals(200, rolePreview.statusCode(), rolePreview.body());
                String rolePreviewToken = mapper.readTree(rolePreview.body()).path("data").path("preview_token").asText();
                var roleApply = new RoleAuthorizationApplyFrom();
                roleApply.setExpectedVersion(0L);
                roleApply.setAuthorityLevel(100);
                roleApply.setPermissionCodes(Set.copyOf(retainedPermissions));
                roleApply.setGrantablePermissionCodes(Set.of("role:read", "role:grant"));
                roleApply.setPreviewToken(rolePreviewToken);
                var operatorPrincipal = mock(SecurityPrincipal.class);
                when(operatorPrincipal.getId()).thenReturn(fixture.operator);
                var targetPrincipal = mock(SecurityPrincipal.class);
                when(targetPrincipal.getId()).thenReturn(fixture.target);
                var roleChangedInTransaction = new CountDownLatch(1);
                var finishRoleChange = new CountDownLatch(1);
                var transaction = new TransactionTemplate(application.getBean(PlatformTransactionManager.class));
                var roleMutation = CompletableFuture.runAsync(() -> {
                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(operatorPrincipal, null, List.of()));
                    try {
                        transaction.executeWithoutResult(status -> {
                            application.getBean(RoleAuthorizationChangeService.class)
                                    .apply(fixture.targetRole,
                                            roleApply);
                            roleChangedInTransaction.countDown();
                            try {
                                if (!finishRoleChange.await(20, TimeUnit.SECONDS)) {
                                    throw new IllegalStateException("角色缩权事务提交屏障超时");
                                }
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException("角色缩权事务等待中断", exception);
                            }
                        });
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                });
                CompletableFuture<AccessDeniedException> racingApply;
                try {
                    assertTrue(roleChangedInTransaction.await(20, TimeUnit.SECONDS));
                    var applyStarted = new CountDownLatch(1);
                    racingApply = CompletableFuture.supplyAsync(() -> {
                        SecurityContextHolder.getContext()
                                .setAuthentication(
                                        new UsernamePasswordAuthenticationToken(targetPrincipal, null, List.of()));
                        applyStarted.countDown();
                        try {
                            return assertThrows(AccessDeniedException.class,
                                    () -> application.getBean(NotificationControlledSendService.class).apply(sendApply));
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    });
                    assertTrue(applyStarted.await(5, TimeUnit.SECONDS));
                    assertThrows(TimeoutException.class, () -> racingApply.get(500, TimeUnit.MILLISECONDS),
                            "角色缩权事务提交前 Apply 不得越过用户行锁");
                } finally {
                    finishRoleChange.countDown();
                }
                roleMutation.get(20, TimeUnit.SECONDS);
                assertTrue(racingApply.get(20, TimeUnit.SECONDS) instanceof AccessDeniedException);
                assertEquals(1L, fixture.roleVersion());
                assertEquals(1L, fixture.targetSecurityVersion());
                assertEquals(0L, fixture.jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_role_permission "
                        + "WHERE role_id = ? AND permission_id = (SELECT id FROM spectra_security.sec_permission "
                        + "WHERE code = 'notification:send:apply') AND deleted IS NULL", Long.class,
                        fixture.targetRole));
                assertEquals("PREVIEWED", fixture.jdbc.queryForObject("SELECT status FROM spectra_notification.ntf_send_preview "
                        + "WHERE id = ?", String.class, sendApply.getPreviewId()));
                assertEquals(0L, fixture.jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key = ?", Long.class, requestKey));
                assertEquals(401, get(client, port, "/api/security/context", targetToken).statusCode());
                String renewedTargetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                assertFalse(hasPermission(context(client, port, mapper, renewedTargetToken),
                        "notification:send:apply"));
            } finally {
                fixture.jdbc.update("DELETE FROM spectra_notification.ntf_send_preview WHERE operator_user_id = ?",
                        fixture.target);
                var revocation = application.getBean(SecuritySessionRevocationPort.class);
                revocation.revokeUserSessions(fixture.operator);
                revocation.revokeUserSessions(fixture.target);
                fixture.clean();
            }
        }
    }

    @Test
    void roleChangeRevokesSessionAcrossProcesses() throws Exception {
        requireIsolatedDependencies();
        Path temporary = Files.createTempDirectory(Path.of("target"), "role-two-process-");
        try (var first = new SpringApplicationBuilder(LaunchApplication.class, TestConfiguration.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.test.role-http=true")) {
            Path ready = temporary.resolve("second-port.txt");
            Process second = startSecond(ready, temporary.resolve("second.log"));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int firstPort = ((WebServerApplicationContext) first).getWebServer().getPort();
                int secondPort = awaitPort(second, ready);
                assertNotEquals(ProcessHandle.current().pid(), second.pid());
                var fixture = new Fixture(new JdbcTemplate(first.getBean(DataSource.class)),
                        first.getBean(PasswordEncoder.class));
                String notificationKey = "b02:role-two-process-send:" + UUID.randomUUID();
                try {
                    fixture.insert();
                    fixture.insertNotificationSendScenario();
                    ObjectMapper mapper = first.getBean(ObjectMapper.class);
                    String operatorToken = login(client, firstPort, mapper, fixture.operatorUsername, fixture.password);
                    String targetToken = login(client, secondPort, mapper, fixture.targetUsername, fixture.password);
                    assertEquals(200, get(client, firstPort, "/api/security/context", targetToken).statusCode(),
                            "首进程应接受第二进程签署的目标用户 Access");

                    UUID templateId = fixture.jdbc.queryForObject("SELECT id FROM spectra_notification.ntf_template "
                            + "WHERE template_group_code = 'system.notice' AND channel = 'IN_APP' "
                            + "AND state = 'PUBLISHED' AND deleted IS NULL", UUID.class);
                    String notificationBody = mapper.writeValueAsString(Map.of("idempotency_key", notificationKey,
                            "purpose", "SYSTEM_NOTICE", "channels", List.of("IN_APP"),
                            "template_version_ids", Map.of("IN_APP", templateId),
                            "audience", Map.of("user_ids", List.of(fixture.target)), "parameters", Map.of()));
                    var sendPreview = post(client, secondPort, "/api/notification/admin/send/preview", targetToken,
                            notificationBody);
                    assertEquals(200, sendPreview.statusCode(), sendPreview.body());
                    var sendData = mapper.readTree(sendPreview.body()).path("data");
                    String sendApplyBody = mapper.writeValueAsString(Map.of(
                            "preview_id", sendData.path("preview_id").asText(),
                            "preview_token", sendData.path("preview_token").asText(),
                            "request_hash", sendData.path("request_hash").asText()));

                    String path = "/api/security/authorization/roles/" + fixture.targetRole + "/impact-preview";
                    String requested = mapper.writeValueAsString(Map.of("expected_version", 0,
                            "authority_level", 100, "permission_codes", List.of("role:read"),
                            "grantable_permission_codes", List.of("role:read")));
                    var preview = post(client, firstPort, path, operatorToken, requested);
                    assertEquals(200, preview.statusCode());
                    String previewToken = mapper.readTree(preview.body()).path("data").path("preview_token").asText();
                    assertFalse(previewToken.isBlank());
                    var roleApply = new RoleAuthorizationApplyFrom();
                    roleApply.setExpectedVersion(0L);
                    roleApply.setAuthorityLevel(100);
                    roleApply.setPermissionCodes(Set.of("role:read"));
                    roleApply.setGrantablePermissionCodes(Set.of("role:read"));
                    roleApply.setPreviewToken(previewToken);
                    var principal = mock(SecurityPrincipal.class);
                    when(principal.getId()).thenReturn(fixture.operator);
                    var changedInTransaction = new CountDownLatch(1);
                    var finishChange = new CountDownLatch(1);
                    var transaction = new TransactionTemplate(first.getBean(PlatformTransactionManager.class));
                    var roleMutation = CompletableFuture.runAsync(() -> {
                        SecurityContextHolder.getContext()
                                .setAuthentication(
                                        new UsernamePasswordAuthenticationToken(principal, null, List.of()));
                        try {
                            transaction.executeWithoutResult(status -> {
                                first.getBean(RoleAuthorizationChangeService.class).apply(fixture.targetRole, roleApply);
                                changedInTransaction.countDown();
                                try {
                                    if (!finishChange.await(20, TimeUnit.SECONDS)) {
                                        throw new IllegalStateException("跨进程角色缩权提交屏障超时");
                                    }
                                } catch (InterruptedException exception) {
                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException("跨进程角色缩权等待中断", exception);
                                }
                            });
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    });
                    CompletableFuture<HttpResponse<String>> racingApply;
                    try {
                        assertTrue(changedInTransaction.await(20, TimeUnit.SECONDS));
                        racingApply = CompletableFuture.supplyAsync(() -> {
                            try {
                                return post(client, secondPort, "/api/notification/admin/send/apply", targetToken,
                                        sendApplyBody);
                            } catch (Exception exception) {
                                throw new IllegalStateException(exception);
                            }
                        });
                        try {
                            var earlyRejection = racingApply.get(500, TimeUnit.MILLISECONDS);
                            assertNotEquals(200, earlyRejection.statusCode(),
                                    "第二 JVM 可以提前失效会话，但不得在角色缩权提交前完成发送");
                        } catch (TimeoutException waitingForCommittedRole) {
                            assertFalse(racingApply.isDone(), "等待行锁时不得提前完成发送");
                        }
                        assertEquals(0L, fixture.jdbc.queryForObject(
                                "SELECT count(*) FROM spectra_notification.ntf_request WHERE idempotency_key = ?",
                                Long.class, notificationKey));
                    } finally {
                        finishChange.countDown();
                    }
                    roleMutation.get(20, TimeUnit.SECONDS);
                    assertNotEquals(200, racingApply.get(20, TimeUnit.SECONDS).statusCode());
                    assertEquals(0L, fixture.jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                            + "WHERE idempotency_key = ?", Long.class, notificationKey));
                    assertEquals("PREVIEWED", fixture.jdbc.queryForObject("SELECT status "
                            + "FROM spectra_notification.ntf_send_preview WHERE id = ?", String.class,
                            UUID.fromString(sendData.path("preview_id").asText())));

                    assertEquals(401, get(client, secondPort, "/api/security/context", targetToken).statusCode(),
                            "角色缩权后第二进程应拒绝旧 Access");
                    String renewed = login(client, secondPort, mapper, fixture.targetUsername, fixture.password);
                    JsonNode reduced = context(client, secondPort, mapper, renewed);
                    assertTrue(hasPermission(reduced, "role:read"));
                    assertFalse(hasPermission(reduced, "role:grant"), "重新登录快照应反映收缩");
                } finally {
                    fixture.jdbc.update("DELETE FROM spectra_notification.ntf_send_preview WHERE operator_user_id = ?",
                            fixture.target);
                    var revocation = first.getBean(SecuritySessionRevocationPort.class);
                    revocation.revokeUserSessions(fixture.operator);
                    revocation.revokeUserSessions(fixture.target);
                    fixture.clean();
                }
            } finally {
                stop(second);
            }
        }
    }

    @Test
    void departmentMergeAndRoleShrinkReachAudienceOaAndWorkflow() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class, TestConfiguration.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.test.role-http=true")) {
            var fixture = new Fixture(new JdbcTemplate(application.getBean(DataSource.class)),
                    application.getBean(PasswordEncoder.class));
            try (HttpClient client = HttpClient.newHttpClient()) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                ObjectMapper mapper = application.getBean(ObjectMapper.class);
                var audience = application.getBean(NotificationAudienceDirectory.class);
                try {
                    fixture.insert();
                    fixture.insertDepartmentScenario();
                    String operatorToken = login(client, port, mapper, fixture.operatorUsername, fixture.password);
                    String targetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                    assertModuleReads(client, port, targetToken, 200);
                    assertEquals(List.of(fixture.target), audience.resolve(new NotificationAudience(
                            List.of(), List.of(fixture.sourceDepartmentA), List.of())));
                    assertEquals(List.of(fixture.target), audience.resolve(new NotificationAudience(
                            List.of(), List.of(), List.of(fixture.targetRole))));

                    long version = fixture.organizationVersion();
                    String mergePath = "/api/security/authorization/departments/merge/impact-preview";
                    var mergeBody = Map.of("source_department_ids", List.of(fixture.sourceDepartmentA,
                            fixture.sourceDepartmentB), "expected_organization_version", version,
                            "department", Map.of("name", "Synthetic merged department", "type", 1,
                                    "region_id", fixture.region));
                    var preview = post(client, port, mergePath, operatorToken, mapper.writeValueAsString(mergeBody));
                    assertEquals(200, preview.statusCode(), preview.body());
                    String previewToken = mapper.readTree(preview.body()).path("data").path("preview_token").asText();
                    assertFalse(previewToken.isBlank());
                    var applyBody = Map.of("source_department_ids", List.of(fixture.sourceDepartmentA,
                            fixture.sourceDepartmentB), "expected_organization_version", version,
                            "department", mergeBody.get("department"), "preview_token", previewToken);
                    var applied = post(client, port,
                            "/api/security/authorization/departments/merge/impact-apply", operatorToken,
                            mapper.writeValueAsString(applyBody));
                    assertEquals(200, applied.statusCode(), applied.body());
                    fixture.mergedDepartment = UUID.fromString(mapper.readTree(applied.body())
                            .path("data")
                            .path("department_id")
                            .asText());
                    assertEquals(version + 1, fixture.organizationVersion());
                    assertNotEquals(200, post(client, port,
                            "/api/security/authorization/departments/merge/impact-apply", operatorToken,
                            mapper.writeValueAsString(applyBody)).statusCode());
                    assertEquals(version + 1, fixture.organizationVersion());
                    assertEquals(401, get(client, port, "/api/security/context", targetToken).statusCode());
                    targetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                    assertModuleReads(client, port, targetToken, 200);
                    assertEquals(List.of(fixture.target), audience.resolve(new NotificationAudience(
                            List.of(), List.of(fixture.mergedDepartment), List.of())));
                    assertEquals(fixture.mergedDepartment, fixture.activeDepartmentRule());

                    long splitVersion = fixture.organizationVersion();
                    var splitBody = Map.of("source_department_id", fixture.mergedDepartment,
                            "user_ids", List.of(fixture.target), "expected_organization_version", splitVersion,
                            "department", Map.of("name", "Synthetic split department", "type", 1,
                                    "region_id", fixture.region));
                    var splitPreview = post(client, port,
                            "/api/security/authorization/departments/split/impact-preview", operatorToken,
                            mapper.writeValueAsString(splitBody));
                    assertEquals(200, splitPreview.statusCode(), splitPreview.body());
                    String splitToken = mapper.readTree(splitPreview.body()).path("data").path("preview_token").asText();
                    var splitApply = Map.of("source_department_id", fixture.mergedDepartment,
                            "user_ids", List.of(fixture.target), "expected_organization_version", splitVersion,
                            "department", splitBody.get("department"), "preview_token", splitToken);
                    var splitResult = post(client, port,
                            "/api/security/authorization/departments/split/impact-apply", operatorToken,
                            mapper.writeValueAsString(splitApply));
                    assertEquals(200, splitResult.statusCode(), splitResult.body());
                    fixture.splitDepartment = UUID.fromString(mapper.readTree(splitResult.body())
                            .path("data")
                            .path("department_id")
                            .asText());
                    assertEquals(splitVersion + 1, fixture.organizationVersion());
                    assertEquals(401, get(client, port, "/api/security/context", targetToken).statusCode());
                    targetToken = login(client, port, mapper, fixture.targetUsername, fixture.password);
                    assertModuleReads(client, port, targetToken, 200);
                    assertEquals(List.of(fixture.target), audience.resolve(new NotificationAudience(
                            List.of(), List.of(fixture.splitDepartment), List.of())));
                    assertEquals(List.of(), audience.resolve(new NotificationAudience(
                            List.of(), List.of(fixture.mergedDepartment), List.of())));

                    String rolePath = "/api/security/authorization/roles/" + fixture.targetRole + "/impact-preview";
                    String requested = mapper.writeValueAsString(Map.of("expected_version", 0,
                            "authority_level", 100, "permission_codes", List.of("role:read"),
                            "grantable_permission_codes", List.of("role:read")));
                    var rolePreview = post(client, port, rolePath, operatorToken, requested);
                    assertEquals(200, rolePreview.statusCode(), rolePreview.body());
                    String roleToken = mapper.readTree(rolePreview.body()).path("data").path("preview_token").asText();
                    String change = mapper.writeValueAsString(Map.of("expected_version", 0,
                            "authority_level", 100, "permission_codes", List.of("role:read"),
                            "grantable_permission_codes", List.of("role:read"), "preview_token", roleToken));
                    assertEquals(200, post(client, port,
                            "/api/security/authorization/roles/" + fixture.targetRole + "/impact-apply",
                            operatorToken, change).statusCode());
                    assertModuleReads(client, port, targetToken, 401);
                    String renewed = login(client, port, mapper, fixture.targetUsername, fixture.password);
                    assertModuleReads(client, port, renewed, 403);
                    assertFalse(hasPermission(context(client, port, mapper, renewed), "oa:contact:read"));
                    assertFalse(hasPermission(context(client, port, mapper, renewed), "workflow:process:read"));
                    assertEquals(List.of(fixture.target), audience.resolve(new NotificationAudience(
                            List.of(), List.of(), List.of(fixture.targetRole))));
                } finally {
                    var revocation = application.getBean(SecuritySessionRevocationPort.class);
                    revocation.revokeUserSessions(fixture.operator);
                    revocation.revokeUserSessions(fixture.target);
                    fixture.clean();
                }
            }
        }
    }

    private static void assertModuleReads(HttpClient client, int port, String token, int expected) throws Exception {
        assertEquals(expected, get(client, port, "/api/oa/contact/page?pageNum=1&pageSize=5", token).statusCode());
        assertEquals(expected, get(client, port, "/api/workflow/process-definitions", token).statusCode());
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
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
        }
    }

    /** 第二个 JVM 中启动完整应用，仅共享数据库与 Redis。 */
    public static final class SecondApplication {
        private SecondApplication() {
        }

        public static void main(String[] args) throws Exception {
            Path ready = Path.of(args[0]);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                Files.writeString(ready, Integer.toString(port));
                new CountDownLatch(1).await();
            }
        }
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

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = post(client, port, "/api/security/authentication/login", null,
                mapper.writeValueAsString(Map.of("type", "PASSWORD", "username", username, "password", password)));
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        assertFalse(token.isBlank());
        return token;
    }

    private static JsonNode context(HttpClient client, int port, ObjectMapper mapper, String token) throws Exception {
        var response = get(client, port, "/api/security/context", token);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).path("data");
    }

    private static boolean hasPermission(JsonNode context, String code) {
        for (var permission : context.path("permissions")) {
            if (code.equals(permission.asText())) {
                return true;
            }
        }
        return false;
    }

    private static HttpResponse<String> get(HttpClient client, int port, String path, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient client, int port, String path, String token, String body)
            throws Exception {
        return client.send(request(port, path, token, body), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest request(int port, String path, String token, String body) {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "spectra.test.role-http", havingValue = "true")
    static class TestConfiguration {
        @Bean
        @Primary
        AuthorizationChangeTokenService roleChangeTokenService() {
            String syntheticSecret = UUID.randomUUID().toString() + UUID.randomUUID();
            return new HmacAuthorizationChangeTokenService(syntheticSecret, Clock.systemUTC());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID operator = UUID.randomUUID();
        private final UUID target = UUID.randomUUID();
        private final UUID operatorRole = UUID.randomUUID();
        private final UUID targetRole = UUID.randomUUID();
        private final UUID operatorAssignment = UUID.randomUUID();
        private final UUID targetAssignment = UUID.randomUUID();
        private final UUID operatorReadScope = UUID.randomUUID();
        private final UUID operatorGrantScope = UUID.randomUUID();
        private final UUID targetReadScope = UUID.randomUUID();
        private final UUID targetGrantScope = UUID.randomUUID();
        private final UUID targetReadGrantScope = UUID.randomUUID();
        private final UUID targetGrantGrantScope = UUID.randomUUID();
        private final String operatorUsername = "b02op" + UUID.randomUUID().toString().replace("-", "");
        private final String targetUsername = "b02user" + UUID.randomUUID().toString().replace("-", "");
        private final String password = UUID.randomUUID().toString() + UUID.randomUUID();
        private UUID readPermission;
        private UUID grantPermission;
        private final UUID region = UUID.randomUUID();
        private final UUID sourceDepartmentA = UUID.randomUUID();
        private final UUID sourceDepartmentB = UUID.randomUUID();
        private UUID mergedDepartment;
        private UUID splitDepartment;
        private UUID departmentRuleScope;
        private boolean departmentScenario;
        private final List<UUID> extraScopes = new ArrayList<>();

        private Fixture(JdbcTemplate jdbc, PasswordEncoder encoder) {
            this.jdbc = jdbc;
            this.encoder = encoder;
        }

        private void insert() {
            readPermission = jdbc.queryForObject("SELECT id FROM spectra_security.sec_permission WHERE code = ?",
                    UUID.class, "role:read");
            grantPermission = jdbc.queryForObject("SELECT id FROM spectra_security.sec_permission WHERE code = ?",
                    UUID.class, "role:grant");
            insertUser(operator, operatorUsername);
            insertUser(target, targetUsername);
            insertRole(operatorRole, "ROLE_B02_OPERATOR_" + operator.toString().replace("-", "").toUpperCase());
            insertRole(targetRole, "ROLE_B02_TARGET_" + target.toString().replace("-", "").toUpperCase());
            insertAssignment(operatorAssignment, operator, operatorRole);
            insertAssignment(targetAssignment, target, targetRole);
            insertPermission(operatorRole, readPermission);
            insertPermission(operatorRole, grantPermission);
            insertPermission(targetRole, readPermission);
            insertPermission(targetRole, grantPermission);
            insertGrantable(targetRole, readPermission);
            insertGrantable(targetRole, grantPermission);
            insertBoundary(operatorReadScope, operatorAssignment, readPermission);
            insertBoundary(operatorGrantScope, operatorAssignment, grantPermission);
            insertBoundary(targetReadScope, targetAssignment, readPermission);
            insertBoundary(targetGrantScope, targetAssignment, grantPermission);
            insertGrantBoundary(targetReadGrantScope, targetAssignment, readPermission);
            insertGrantBoundary(targetGrantGrantScope, targetAssignment, grantPermission);
        }

        private void insertNotificationSendScenario() {
            insertPermissionWithAllScope(targetRole, targetAssignment, "user:read");
            insertPermissionWithAllScope(targetRole, targetAssignment, "notification:send:preview");
            insertPermissionWithAllScope(targetRole, targetAssignment, "notification:send:apply");
        }

        private void insertUser(UUID id, String username) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", id, username, username, "synthetic role user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    id, AuthenticationIdentifierHash.digest(username));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    id, encoder.encode(password));
        }

        private void insertDepartmentScenario() {
            departmentScenario = true;
            jdbc.update("INSERT INTO spectra_core.sys_region (id, name, code, created_at, updated_at) "
                    + "VALUES (?, 'Synthetic region', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", region,
                    "B02" + region.toString().replace("-", ""));
            insertDepartment(sourceDepartmentA, "A");
            insertDepartment(sourceDepartmentB, "B");
            jdbc.update("UPDATE spectra_core.sys_user SET primary_department_id = ? WHERE id = ?",
                    sourceDepartmentA, target);
            insertPermissionWithAllScope(operatorRole, operatorAssignment, "department:create");
            insertPermissionWithAllScope(operatorRole, operatorAssignment, "department:update");
            insertPermissionWithDepartmentScope(targetRole, targetAssignment, "oa:contact:read",
                    sourceDepartmentA);
            insertPermissionWithAllScope(targetRole, targetAssignment, "workflow:process:read");
        }

        private void insertPermissionWithDepartmentScope(UUID roleId, UUID assignmentId, String code,
                                                         UUID departmentId) {
            UUID permissionId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_permission WHERE code = ?",
                    UUID.class, code);
            departmentRuleScope = UUID.randomUUID();
            extraScopes.add(departmentRuleScope);
            insertPermission(roleId, permissionId);
            jdbc.update("INSERT INTO spectra_security.sec_authorization_scope "
                    + "(id, scope_mode, resource_code) VALUES (?, 'RULES', 'oa:contact')", departmentRuleScope);
            jdbc.update("INSERT INTO spectra_security.sec_scope_rule "
                    + "(scope_id, rule_type, department_id) VALUES (?, 'DEPARTMENT', ?)",
                    departmentRuleScope, departmentId);
            jdbc.update("INSERT INTO spectra_security.sec_assignment_permission_boundary "
                    + "(assignment_id, permission_id, scope_id) VALUES (?, ?, ?)",
                    assignmentId, permissionId, departmentRuleScope);
        }

        private UUID activeDepartmentRule() {
            return jdbc.queryForObject("SELECT department_id FROM spectra_security.sec_scope_rule "
                    + "WHERE scope_id = ? AND deleted IS NULL", UUID.class, departmentRuleScope);
        }

        private void insertDepartment(UUID id, String suffix) {
            jdbc.update("INSERT INTO spectra_core.sys_department "
                    + "(id, name, code, type, region_id, created_at, updated_at) "
                    + "VALUES (?, ?, ?, '1', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    id, "Synthetic department " + suffix, "B02" + id.toString().replace("-", ""), region);
            jdbc.update("INSERT INTO spectra_core.sys_department_closure "
                    + "(ancestor_id, descendant_id, depth) VALUES (?, ?, 0)", id, id);
        }

        private void insertPermissionWithAllScope(UUID roleId, UUID assignmentId, String code) {
            UUID permissionId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_permission WHERE code = ?",
                    UUID.class, code);
            UUID scopeId = UUID.randomUUID();
            extraScopes.add(scopeId);
            insertPermission(roleId, permissionId);
            jdbc.update("INSERT INTO spectra_security.sec_authorization_scope "
                    + "(id, scope_mode, resource_code) VALUES (?, 'ALL', ?)", scopeId, code.split(":")[0]);
            jdbc.update("INSERT INTO spectra_security.sec_assignment_permission_boundary "
                    + "(assignment_id, permission_id, scope_id) VALUES (?, ?, ?)",
                    assignmentId, permissionId, scopeId);
        }

        private long organizationVersion() {
            return jdbc.queryForObject("SELECT organization_version FROM spectra_core.sys_organization_version "
                    + "WHERE singleton_key = 'SYSTEM' AND deleted IS NULL", Long.class);
        }

        private void insertRole(UUID roleId, String code) {
            jdbc.update("INSERT INTO spectra_security.sec_role (id, code, name, authority_level) "
                    + "VALUES (?, ?, ?, 100)", roleId, code, code);
        }

        private void insertAssignment(UUID assignmentId, UUID userId, UUID roleId) {
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    assignmentId, userId, roleId);
        }

        private void insertPermission(UUID roleId, UUID permissionId) {
            jdbc.update("INSERT INTO spectra_security.sec_role_permission (role_id, permission_id) VALUES (?, ?)",
                    roleId, permissionId);
        }

        private void insertGrantable(UUID roleId, UUID permissionId) {
            jdbc.update("INSERT INTO spectra_security.sec_role_grantable_permission "
                    + "(role_id, permission_id) VALUES (?, ?)", roleId, permissionId);
        }

        private void insertBoundary(UUID scopeId, UUID assignmentId, UUID permissionId) {
            jdbc.update("INSERT INTO spectra_security.sec_authorization_scope (id, scope_mode) VALUES (?, 'NONE')",
                    scopeId);
            jdbc.update("INSERT INTO spectra_security.sec_assignment_permission_boundary "
                    + "(assignment_id, permission_id, scope_id) VALUES (?, ?, ?)",
                    assignmentId, permissionId, scopeId);
        }

        private void insertGrantBoundary(UUID scopeId, UUID assignmentId, UUID permissionId) {
            jdbc.update("INSERT INTO spectra_security.sec_authorization_scope (id, scope_mode) VALUES (?, 'NONE')",
                    scopeId);
            jdbc.update("INSERT INTO spectra_security.sec_assignment_grant_boundary "
                    + "(assignment_id, permission_id, scope_id) VALUES (?, ?, ?)",
                    assignmentId, permissionId, scopeId);
        }

        private long roleVersion() {
            return jdbc.queryForObject("SELECT version FROM spectra_security.sec_role WHERE id = ?", Long.class,
                    targetRole);
        }

        private long targetSecurityVersion() {
            return jdbc.queryForObject("SELECT security_version FROM spectra_core.sys_user WHERE id = ?", Long.class,
                    target);
        }

        private long removedPermissionCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_role_permission "
                    + "WHERE role_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetRole, grantPermission);
        }

        private long removedBoundaryCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_assignment_permission_boundary "
                    + "WHERE assignment_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetAssignment, grantPermission);
        }

        private long removedScopeCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_authorization_scope WHERE id = ?",
                    Long.class, targetGrantScope);
        }

        private long retainedGrantableCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_role_grantable_permission "
                    + "WHERE role_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetRole, readPermission);
        }

        private long removedGrantableCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_role_grantable_permission "
                    + "WHERE role_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetRole, grantPermission);
        }

        private long retainedGrantBoundaryCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_assignment_grant_boundary "
                    + "WHERE assignment_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetAssignment, readPermission);
        }

        private long removedGrantBoundaryCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_assignment_grant_boundary "
                    + "WHERE assignment_id = ? AND permission_id = ? AND deleted IS NULL", Long.class,
                    targetAssignment, grantPermission);
        }

        private long removedGrantScopeCount() {
            return jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_authorization_scope WHERE id = ?",
                    Long.class, targetGrantGrantScope);
        }

        private void clean() {
            if (departmentScenario) {
                jdbc.update("DELETE FROM spectra_core.sys_user_department_membership WHERE user_id = ?", target);
                jdbc.update("UPDATE spectra_core.sys_user SET primary_department_id = NULL WHERE id = ?", target);
                jdbc.update("DELETE FROM spectra_core.sys_department_closure WHERE ancestor_id IN (?, ?) "
                        + "OR descendant_id IN (?, ?)", sourceDepartmentA, sourceDepartmentB,
                        sourceDepartmentA, sourceDepartmentB);
                if (splitDepartment != null) {
                    jdbc.update("DELETE FROM spectra_core.sys_department_closure WHERE ancestor_id = ? "
                            + "OR descendant_id = ?", splitDepartment, splitDepartment);
                    jdbc.update("DELETE FROM spectra_core.sys_department WHERE id = ?", splitDepartment);
                }
                if (mergedDepartment != null) {
                    jdbc.update("DELETE FROM spectra_core.sys_department_closure WHERE ancestor_id = ? "
                            + "OR descendant_id = ?", mergedDepartment, mergedDepartment);
                    jdbc.update("DELETE FROM spectra_core.sys_department WHERE id = ?", mergedDepartment);
                }
                jdbc.update("DELETE FROM spectra_core.sys_department WHERE id IN (?, ?)",
                        sourceDepartmentA, sourceDepartmentB);
                jdbc.update("DELETE FROM spectra_core.sys_region WHERE id = ?", region);
            }
            jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary "
                    + "WHERE assignment_id IN (?, ?)", operatorAssignment, targetAssignment);
            if (departmentScenario && departmentRuleScope != null) {
                jdbc.update("DELETE FROM spectra_security.sec_scope_rule WHERE scope_id = ?", departmentRuleScope);
            }
            jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary "
                    + "WHERE assignment_id = ?", targetAssignment);
            jdbc.update("DELETE FROM spectra_security.sec_authorization_scope WHERE id IN (?, ?, ?, ?, ?, ?)",
                    operatorReadScope, operatorGrantScope, targetReadScope, targetGrantScope,
                    targetReadGrantScope, targetGrantGrantScope);
            for (UUID scopeId : extraScopes) {
                jdbc.update("DELETE FROM spectra_security.sec_authorization_scope WHERE id = ?", scopeId);
            }
            jdbc.update("DELETE FROM spectra_security.sec_role_permission WHERE role_id IN (?, ?)",
                    operatorRole, targetRole);
            jdbc.update("DELETE FROM spectra_security.sec_role_grantable_permission WHERE role_id = ?", targetRole);
            jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id IN (?, ?)",
                    operatorAssignment, targetAssignment);
            jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id IN (?, ?)",
                    operator, target);
            jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id IN (?, ?)",
                    operator, target);
            jdbc.update("DELETE FROM spectra_security.sec_role WHERE id IN (?, ?)", operatorRole, targetRole);
            jdbc.update("DELETE FROM spectra_core.sys_user WHERE id IN (?, ?)", operator, target);
        }
    }
}
