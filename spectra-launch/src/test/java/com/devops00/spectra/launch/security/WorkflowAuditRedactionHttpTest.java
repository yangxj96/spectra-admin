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
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
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
                Integer audited = fixture.jdbc.queryForObject(
                        "SELECT count(*) FROM spectra_core.sys_audit_event WHERE operator_id = ?",
                        Integer.class, user.id());
                assertTrue(audited >= 1, "失败的 Workflow 写操作也应产生审计记录");
            } finally {
                fixture.clean();
            }
        }
    }

    @Test
    void taskHttpActionsRequireLivePermissionAndOwnedAssignee() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            var user = new TestUser(UUID.randomUUID(),
                    "b02wfhttp" + UUID.randomUUID().toString().replace("-", ""), "Wf!" + UUID.randomUUID());
            var repository = application.getBean(RepositoryService.class);
            var runtime = application.getBean(RuntimeService.class);
            var flowableTasks = application.getBean(org.flowable.engine.TaskService.class);
            String processKey = "b02Http" + UUID.randomUUID().toString().replace("-", "");
            String deploymentId = null;
            String taskId = null;
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());
                deploymentId = repository.createDeployment()
                        .addString(processKey + ".bpmn20.xml",
                                oneStepProcess(processKey, user.username()))
                        .deploy()
                        .getId();
                String processId = runtime.startProcessInstanceByKey(processKey).getId();
                taskId = flowableTasks.createTaskQuery().processInstanceId(processId).singleResult().getId();
                String path = "/api/workflow/tasks/" + taskId;
                assertEquals(200, get(client, port, "/api/workflow/tasks/todo?page_num=1&page_size=10", token).statusCode());
                assertEquals(200, get(client, port, "/api/workflow/tasks/done?page_num=1&page_size=10", token).statusCode());
                for (String action : new String[]{"complete", "reject", "transfer", "delegate"}) {
                    assertDenied(postTask(client, port, mapper, path, action, null).statusCode());
                }
                assertEquals(1, flowableTasks.createTaskQuery().taskId(taskId).count());
                UUID assignmentId = fixture.jdbc.queryForObject(
                        "SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?", UUID.class, user.id());
                fixture.jdbc.update("UPDATE spectra_security.sec_role_assignment "
                        + "SET valid_until = statement_timestamp() - INTERVAL '1 second' WHERE id = ?", assignmentId);
                for (String action : new String[]{"complete", "reject", "transfer", "delegate"}) {
                    assertDenied(postTask(client, port, mapper, path, action, token).statusCode());
                }
                assertDenied(get(client, port, "/api/workflow/tasks/todo?page_num=1&page_size=10", token).statusCode());
                assertEquals(1, flowableTasks.createTaskQuery().taskId(taskId).count());
                fixture.jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_until = NULL WHERE id = ?",
                        assignmentId);
                var approved = postTask(client, port, mapper, path, "complete", token);
                assertEquals(200, approved.statusCode(), approved.body());
                assertEquals(0, flowableTasks.createTaskQuery().taskId(taskId).count());
            } finally {
                if (taskId != null) {
                    fixture.cleanNotification("workflow:result:" + taskId + ":true");
                }
                if (deploymentId != null) {
                    repository.deleteDeployment(deploymentId, true);
                }
                fixture.clean();
            }
        }
    }

    @Test
    void rejectTransferAndDelegateSucceedForCurrentAssignee() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var fixture = new Fixture(application);
            var owner = new TestUser(UUID.randomUUID(),
                    "b02wfowner" + UUID.randomUUID().toString().replace("-", ""), "Wf!" + UUID.randomUUID());
            UUID targetId = UUID.randomUUID();
            String targetName = "b02wftarget" + UUID.randomUUID().toString().replace("-", "");
            var repository = application.getBean(RepositoryService.class);
            var runtime = application.getBean(RuntimeService.class);
            var flowableTasks = application.getBean(org.flowable.engine.TaskService.class);
            String processKey = "b02Actions" + UUID.randomUUID().toString().replace("-", "");
            String deploymentId = null;
            String rejectedTaskId = null;
            try (HttpClient client = HttpClient.newHttpClient()) {
                fixture.insert(owner);
                fixture.jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", targetId, targetName, targetName, "synthetic target");
                String token = login(client, port, mapper, owner.username(), owner.password());
                deploymentId = repository.createDeployment()
                        .addString(processKey + ".bpmn20.xml", oneStepProcess(processKey, owner.username()))
                        .deploy()
                        .getId();

                String transferProcessId = runtime.startProcessInstanceByKey(processKey).getId();
                String transferTaskId = flowableTasks.createTaskQuery()
                        .processInstanceId(transferProcessId)
                        .singleResult()
                        .getId();
                var transferred = postTaskTo(client, port, mapper, transferTaskId, "transfer", token, targetName);
                assertEquals(200, transferred.statusCode(), transferred.body());
                assertEquals(targetName, flowableTasks.createTaskQuery().taskId(transferTaskId).singleResult().getAssignee());

                String delegateProcessId = runtime.startProcessInstanceByKey(processKey).getId();
                String delegateTaskId = flowableTasks.createTaskQuery()
                        .processInstanceId(delegateProcessId)
                        .singleResult()
                        .getId();
                var delegated = postTaskTo(client, port, mapper, delegateTaskId, "delegate", token, targetName);
                assertEquals(200, delegated.statusCode(), delegated.body());
                var delegatedTask = flowableTasks.createTaskQuery().taskId(delegateTaskId).singleResult();
                assertEquals(targetName, delegatedTask.getAssignee());
                assertEquals(org.flowable.task.api.DelegationState.PENDING, delegatedTask.getDelegationState());

                String rejectProcessId = runtime.startProcessInstanceByKey(processKey).getId();
                rejectedTaskId = flowableTasks.createTaskQuery()
                        .processInstanceId(rejectProcessId)
                        .singleResult()
                        .getId();
                var rejected = postTask(client, port, mapper, "/api/workflow/tasks/" + rejectedTaskId, "reject", token);
                assertEquals(200, rejected.statusCode(), rejected.body());
                assertEquals(0, flowableTasks.createTaskQuery().taskId(rejectedTaskId).count());
                assertEquals(0, runtime.createProcessInstanceQuery().processInstanceId(rejectProcessId).count());
            } finally {
                if (rejectedTaskId != null) {
                    fixture.cleanNotification("workflow:result:" + rejectedTaskId + ":false");
                }
                if (deploymentId != null) {
                    repository.deleteDeployment(deploymentId, true);
                }
                fixture.jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", targetId);
                fixture.clean();
            }
        }
    }

    private static HttpResponse<String> postTaskTo(HttpClient client, int port, ObjectMapper mapper,
                                                   String taskId, String action, String token, String target)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/workflow/tasks/" + taskId + "/" + action))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("target_user_id", target))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertDenied(int status) {
        assertTrue(status == 401 || status == 403, "授权拒绝应为 401/403，实际为 " + status);
    }

    private static HttpResponse<String> get(HttpClient client, int port, String path, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> postTask(HttpClient client, int port, ObjectMapper mapper,
                                                 String path, String action, String token)
            throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path + "/" + action))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        Map<String, String> body = action.equals("transfer") || action.equals("delegate")
                ? Map.of("target_user_id", UUID.randomUUID().toString())
                : Map.of("comment", "synthetic review");
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static String oneStepProcess(String key, String assignee) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                + "xmlns:flowable=\"http://flowable.org/bpmn\" targetNamespace=\"http://spectra.test/b02\">"
                + "<process id=\"" + key + "\" isExecutable=\"true\"><startEvent id=\"start\"/>"
                + "<userTask id=\"review\" flowable:assignee=\"" + assignee + "\"/>"
                + "<endEvent id=\"end\"/><sequenceFlow id=\"s1\" sourceRef=\"start\" targetRef=\"review\"/>"
                + "<sequenceFlow id=\"s2\" sourceRef=\"review\" targetRef=\"end\"/>"
                + "</process></definitions>";
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

        private void cleanNotification(String key) {
            var requestIds = jdbc.queryForList("SELECT id FROM spectra_notification.ntf_request "
                    + "WHERE idempotency_key = ?", UUID.class, key);
            for (UUID requestId : requestIds) {
                jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_request_id = ?", requestId);
                jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                        + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
                jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
                jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
            }
        }
    }
}
