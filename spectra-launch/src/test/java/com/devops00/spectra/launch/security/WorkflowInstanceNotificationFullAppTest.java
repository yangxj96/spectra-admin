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

import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.launch.LaunchApplication;
import com.devops00.spectra.workflow.api.ProcessInstanceService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 在正式 Flowable 实例中验证审批结果与下一待办使用登记的通知系统身份。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class WorkflowInstanceNotificationFullAppTest {

    @Test
    void rejectingReimbursementTaskUpdatesOaApplicationAndSendsOneNotice() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var repository = application.getBean(RepositoryService.class);
            var flowableTasks = application.getBean(org.flowable.engine.TaskService.class);
            var processService = application.getBean(ProcessInstanceService.class);
            var workflowTasks = application.getBean(com.devops00.spectra.workflow.api.TaskService.class);
            UUID userId = UUID.randomUUID();
            UUID assignmentId = UUID.randomUUID();
            UUID applicationId = UUID.randomUUID();
            UUID businessId = UUID.randomUUID();
            String username = "b02oacallback" + UUID.randomUUID().toString().replace("-", "");
            String deploymentId = null;
            String taskId = null;
            String noticeKey = "oa:reimbursement:" + businessId + ":报销申请已驳回";
            try {
                UUID roleId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                        UUID.class);
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", userId, username, username, "synthetic OA callback user");
                jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                        assignmentId, userId, roleId);
                jdbc.update("INSERT INTO spectra_oa.oa_application "
                        + "(id, application_no, type_code, biz_id, applicant_id, title, status, created_at, updated_at) "
                        + "VALUES (?, ?, 'reimbursement', ?, ?, ?, 'IN_REVIEW', statement_timestamp(), statement_timestamp())",
                        applicationId, "B02-" + UUID.randomUUID(), businessId, userId, "synthetic reimbursement");
                deploymentId = repository.createDeployment()
                        .addClasspathResource("processes/oa-reimbursement-approval.bpmn20.xml")
                        .deploy()
                        .getId();
                String processId = processService.start("oa_reimbursement_approval", applicationId.toString(),
                        Map.of("approver", username));
                jdbc.update("UPDATE spectra_oa.oa_application SET process_instance_id = ? WHERE id = ?",
                        processId, applicationId);
                var task = flowableTasks.createTaskQuery().processInstanceId(processId).singleResult();
                assertNotNull(task);
                taskId = task.getId();
                assertEquals(userId, recipientFor(jdbc, "workflow:todo:" + taskId));
                authenticate(userId, username);
                workflowTasks.reject(taskId, "synthetic rejection", username);
                assertEquals("REJECTED", jdbc.queryForObject("SELECT status FROM spectra_oa.oa_application WHERE id = ?",
                        String.class, applicationId));
                assertEquals("synthetic rejection", jdbc.queryForObject(
                        "SELECT reject_reason FROM spectra_oa.oa_application WHERE id = ?", String.class, applicationId));
                assertEquals(1L, requestCount(jdbc, noticeKey));
                assertEquals(userId, recipientFor(jdbc, noticeKey));
                String completedTaskId = taskId;
                assertThrows(DataNotExistException.class,
                        () -> workflowTasks.reject(completedTaskId, "replay", username));
                assertEquals(1L, requestCount(jdbc, noticeKey));
            } finally {
                SecurityContextHolder.clearContext();
                deleteNotification(jdbc, noticeKey);
                deleteNotification(jdbc, taskId == null ? null : "workflow:result:" + taskId + ":false");
                deleteNotification(jdbc, taskId == null ? null : "workflow:todo:" + taskId);
                if (deploymentId != null) {
                    repository.deleteDeployment(deploymentId, true);
                }
                jdbc.update("DELETE FROM spectra_oa.oa_application WHERE id = ?", applicationId);
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id = ?", assignmentId);
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", userId);
            }
        }
    }

    @Test
    void completingRealTwoStepInstanceSendsResultAndNextTodoOnlyOnce() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var repository = application.getBean(RepositoryService.class);
            var runtime = application.getBean(RuntimeService.class);
            var flowableTasks = application.getBean(org.flowable.engine.TaskService.class);
            var workflowTasks = application.getBean(com.devops00.spectra.workflow.api.TaskService.class);
            var processService = application.getBean(ProcessInstanceService.class);
            var notificationProperties = application.getBean(NotificationModuleProperties.class);
            var firstUserId = UUID.randomUUID();
            var secondUserId = UUID.randomUUID();
            var firstAssignmentId = UUID.randomUUID();
            var secondAssignmentId = UUID.randomUUID();
            var roleId = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                    UUID.class);
            var firstUsername = "b02wf" + UUID.randomUUID().toString().replace("-", "");
            var secondUsername = "b02wf" + UUID.randomUUID().toString().replace("-", "");
            var processKey = "b02Workflow" + UUID.randomUUID().toString().replace("-", "");
            var deploymentId = (String) null;
            var missingRecipientDeploymentId = (String) null;
            var unassignedDeploymentId = (String) null;
            var firstTaskId = (String) null;
            var secondTaskId = (String) null;
            var recoveryTaskId = (String) null;
            try {
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", firstUserId, firstUsername, firstUsername, "synthetic workflow user one");
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", secondUserId, secondUsername, secondUsername, "synthetic workflow user two");
                jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                        firstAssignmentId, firstUserId, roleId);
                jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                        secondAssignmentId, secondUserId, roleId);
                deploymentId = repository.createDeployment()
                        .addString(processKey + ".bpmn20.xml", twoStepProcess(processKey, firstUsername, secondUsername))
                        .deploy()
                        .getId();
                String businessKey = "b02:" + UUID.randomUUID();
                String processInstanceId = processService.start(processKey, businessKey, Map.of());
                var firstTask = flowableTasks.createTaskQuery().processInstanceId(processInstanceId).singleResult();
                assertNotNull(firstTask);
                firstTaskId = firstTask.getId();
                assertEquals(firstUsername, firstTask.getAssignee());
                String initialTaskKey = "workflow:todo:" + firstTaskId;
                assertEquals(1L, requestCount(jdbc, initialTaskKey));
                assertEquals(firstUserId, recipientFor(jdbc, initialTaskKey));
                assertThrows(com.devops00.spectra.common.exception.DataException.class,
                        () -> processService.start(processKey, businessKey, Map.of()));
                assertEquals(1L, requestCount(jdbc, initialTaskKey));
                String firstTaskKey = "workflow:result:" + firstTaskId + ":true";
                assertEquals(0L, requestCount(jdbc, firstTaskKey));
                String wrongOperatorTaskId = firstTaskId;
                assertThrows(AccessDeniedException.class,
                        () -> workflowTasks.complete(wrongOperatorTaskId, "synthetic", secondUsername));
                assertEquals(0L, requestCount(jdbc, firstTaskKey));
                authenticate(secondUserId, secondUsername);
                assertThrows(AccessDeniedException.class,
                        () -> workflowTasks.complete(wrongOperatorTaskId, "synthetic", firstUsername));
                assertThrows(DataNotExistException.class,
                        () -> workflowTasks.complete(wrongOperatorTaskId, "synthetic", secondUsername));
                assertEquals(0L, requestCount(jdbc, firstTaskKey));

                authenticate(firstUserId, firstUsername);
                workflowTasks.complete(firstTaskId, "synthetic approval", firstUsername);
                assertEquals(1L, requestCount(jdbc, firstTaskKey));
                assertEquals(firstUserId, recipientFor(jdbc, firstTaskKey));
                var secondTask = flowableTasks.createTaskQuery().processInstanceId(processInstanceId).singleResult();
                assertNotNull(secondTask);
                secondTaskId = secondTask.getId();
                assertEquals(secondUsername, secondTask.getAssignee());
                String nextTaskKey = "workflow:todo:" + secondTaskId;
                assertEquals(1L, requestCount(jdbc, nextTaskKey));
                assertEquals(secondUserId, recipientFor(jdbc, nextTaskKey));
                String completedTaskId = firstTaskId;
                assertThrows(DataNotExistException.class,
                        () -> workflowTasks.complete(completedTaskId, "replay", firstUsername));
                assertEquals(1L, requestCount(jdbc, firstTaskKey));
                assertEquals(1L, requestCount(jdbc, initialTaskKey));
                assertEquals(1L, requestCount(jdbc, nextTaskKey));

                String finalTaskId = secondTaskId;
                authenticate(secondUserId, secondUsername);
                jdbc.update("UPDATE spectra_security.sec_role_assignment "
                        + "SET valid_until = statement_timestamp() - INTERVAL '1 second' WHERE id = ?", secondAssignmentId);
                assertThrows(AccessDeniedException.class,
                        () -> workflowTasks.complete(finalTaskId, "expired role", secondUsername));
                assertEquals(0L, requestCount(jdbc, "workflow:result:" + finalTaskId + ":true"));
                jdbc.update("UPDATE spectra_security.sec_role_assignment SET valid_until = NULL WHERE id = ?",
                        secondAssignmentId);
                var start = new CountDownLatch(1);
                try (var executor = Executors.newFixedThreadPool(2)) {
                    var first = executor.submit(() -> completeOnce(start, secondUserId, secondUsername, finalTaskId, workflowTasks));
                    var second = executor.submit(() -> completeOnce(start, secondUserId, secondUsername, finalTaskId, workflowTasks));
                    start.countDown();
                    assertEquals(1, (first.get(30, TimeUnit.SECONDS) ? 1 : 0)
                            + (second.get(30, TimeUnit.SECONDS) ? 1 : 0), "竞争同一任务只有一次完成成功");
                }
                assertEquals(1L, requestCount(jdbc, "workflow:result:" + secondTaskId + ":true"));
                assertTrue(runtime.createProcessInstanceQuery().processInstanceId(processInstanceId).list().isEmpty());

                String recoveryBusinessKey = "b02:recovery:" + UUID.randomUUID();
                long beforeFailedStart = workflowTodoCount(jdbc);
                notificationProperties.setEnabled(false);
                try {
                    assertFalse(notificationProperties.enabled());
                    assertThrows(DataSaveException.class,
                            () -> processService.start(processKey, recoveryBusinessKey, Map.of()));
                    assertEquals(0L, runtime.createProcessInstanceQuery()
                            .processInstanceBusinessKey(recoveryBusinessKey)
                            .count());
                    assertEquals(beforeFailedStart, workflowTodoCount(jdbc));
                } finally {
                    notificationProperties.setEnabled(true);
                }
                String recoveryProcessId = processService.start(processKey, recoveryBusinessKey, Map.of());
                var recoveryTask = flowableTasks.createTaskQuery().processInstanceId(recoveryProcessId).singleResult();
                assertNotNull(recoveryTask);
                recoveryTaskId = recoveryTask.getId();
                assertEquals(1L, requestCount(jdbc, "workflow:todo:" + recoveryTaskId));
                assertEquals(firstUserId, recipientFor(jdbc, "workflow:todo:" + recoveryTaskId));

                String missingRecipientProcessKey = processKey + "Missing";
                String missingRecipientName = "b02missing" + UUID.randomUUID().toString().replace("-", "");
                missingRecipientDeploymentId = repository.createDeployment()
                        .addString(missingRecipientProcessKey + ".bpmn20.xml",
                                twoStepProcess(missingRecipientProcessKey, missingRecipientName, secondUsername))
                        .deploy()
                        .getId();
                long beforeMissingRecipient = workflowTodoCount(jdbc);
                String missingRecipientProcessId = processService.start(missingRecipientProcessKey,
                        "b02:missing:" + UUID.randomUUID(), Map.of());
                var missingRecipientTask = flowableTasks.createTaskQuery()
                        .processInstanceId(missingRecipientProcessId)
                        .singleResult();
                assertNotNull(missingRecipientTask);
                assertEquals(missingRecipientName, missingRecipientTask.getAssignee());
                assertEquals(0L, requestCount(jdbc, "workflow:todo:" + missingRecipientTask.getId()));
                assertEquals(beforeMissingRecipient, workflowTodoCount(jdbc));

                String unassignedProcessKey = processKey + "Unassigned";
                unassignedDeploymentId = repository.createDeployment()
                        .addString(unassignedProcessKey + ".bpmn20.xml", unassignedProcess(unassignedProcessKey))
                        .deploy()
                        .getId();
                long beforeUnassigned = workflowTodoCount(jdbc);
                String unassignedProcessId = processService.start(unassignedProcessKey,
                        "b02:unassigned:" + UUID.randomUUID(), Map.of());
                var unassignedTask = flowableTasks.createTaskQuery()
                        .processInstanceId(unassignedProcessId)
                        .singleResult();
                assertNotNull(unassignedTask);
                assertTrue(unassignedTask.getAssignee() == null || unassignedTask.getAssignee().isBlank());
                assertEquals(0L, requestCount(jdbc, "workflow:todo:" + unassignedTask.getId()));
                assertEquals(beforeUnassigned, workflowTodoCount(jdbc));

                verifyTwoProcessStart(processService, runtime, flowableTasks, jdbc, processKey, firstUserId);
            } finally {
                notificationProperties.setEnabled(true);
                SecurityContextHolder.clearContext();
                deleteNotification(jdbc, recoveryTaskId == null ? null : "workflow:todo:" + recoveryTaskId);
                deleteNotification(jdbc, firstTaskId == null ? null : "workflow:todo:" + firstTaskId);
                deleteNotification(jdbc, firstTaskId == null ? null : "workflow:result:" + firstTaskId + ":true");
                deleteNotification(jdbc, secondTaskId == null ? null : "workflow:todo:" + secondTaskId);
                deleteNotification(jdbc, secondTaskId == null ? null : "workflow:result:" + secondTaskId + ":true");
                if (deploymentId != null) {
                    repository.deleteDeployment(deploymentId, true);
                }
                if (missingRecipientDeploymentId != null) {
                    repository.deleteDeployment(missingRecipientDeploymentId, true);
                }
                if (unassignedDeploymentId != null) {
                    repository.deleteDeployment(unassignedDeploymentId, true);
                }
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE id IN (?, ?)",
                        firstAssignmentId, secondAssignmentId);
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id IN (?, ?)", firstUserId, secondUserId);
            }
        }
    }

    private static String twoStepProcess(String processKey, String firstUser, String secondUser) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                + "xmlns:flowable=\"http://flowable.org/bpmn\" targetNamespace=\"http://spectra.test/b02\">"
                + "<process id=\"" + processKey + "\" name=\"Synthetic B02 approval\" isExecutable=\"true\">"
                + "<startEvent id=\"start\"/><userTask id=\"first\" name=\"First review\" flowable:assignee=\""
                + firstUser + "\"/><userTask id=\"second\" name=\"Second review\" flowable:assignee=\""
                + secondUser + "\"/><endEvent id=\"end\"/>"
                + "<sequenceFlow id=\"s1\" sourceRef=\"start\" targetRef=\"first\"/>"
                + "<sequenceFlow id=\"s2\" sourceRef=\"first\" targetRef=\"second\"/>"
                + "<sequenceFlow id=\"s3\" sourceRef=\"second\" targetRef=\"end\"/>"
                + "</process></definitions>";
    }

    private static String unassignedProcess(String processKey) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                + "targetNamespace=\"http://spectra.test/b02\">"
                + "<process id=\"" + processKey + "\" name=\"Synthetic unassigned B02 approval\" isExecutable=\"true\">"
                + "<startEvent id=\"start\"/><userTask id=\"unassigned\" name=\"Unassigned review\"/>"
                + "<endEvent id=\"end\"/>"
                + "<sequenceFlow id=\"s1\" sourceRef=\"start\" targetRef=\"unassigned\"/>"
                + "<sequenceFlow id=\"s2\" sourceRef=\"unassigned\" targetRef=\"end\"/>"
                + "</process></definitions>";
    }

    private static void verifyTwoProcessStart(ProcessInstanceService processService, RuntimeService runtime,
                                              org.flowable.engine.TaskService tasks, JdbcTemplate jdbc,
                                              String processKey, UUID expectedRecipient)
            throws Exception {
        Path directory = Files.createTempDirectory(Path.of("target"), "b02-workflow-business-key-");
        Path ready = directory.resolve("ready");
        Path release = directory.resolve("release");
        Path armed = directory.resolve("armed");
        Path result = directory.resolve("result");
        Path childLog = directory.resolve("child.log");
        String businessKey = "b02:two-process:" + UUID.randomUUID();
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        Process child = new ProcessBuilder(java, "-Duser.timezone=UTC", "-cp",
                System.getProperty("java.class.path"), SecondWorkflowApplication.class.getName(),
                ready.toString(), release.toString(), armed.toString(), result.toString(), processKey, businessKey)
                .redirectErrorStream(true)
                .redirectOutput(childLog.toFile())
                .start();
        try {
            awaitFile(ready, child);
            Files.writeString(release, "go");
            awaitFile(armed, child);
            boolean parentSucceeded;
            try {
                processService.start(processKey, businessKey, Map.of());
                parentSucceeded = true;
            } catch (com.devops00.spectra.common.exception.DataException exception) {
                parentSucceeded = false;
            }
            awaitFile(result, child);
            String childResult = Files.readString(result);
            assertEquals(1, (parentSucceeded ? 1 : 0) + (childResult.startsWith("success:") ? 1 : 0));
            assertTrue(childResult.startsWith("success:") || childResult.startsWith("failure:"));
            var instances = runtime.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
            assertEquals(1, instances.size());
            var activeTasks = tasks.createTaskQuery().processInstanceId(instances.getFirst().getId()).list();
            assertEquals(1, activeTasks.size());
            String notificationKey = "workflow:todo:" + activeTasks.getFirst().getId();
            assertEquals(1L, requestCount(jdbc, notificationKey));
            assertEquals(expectedRecipient, recipientFor(jdbc, notificationKey));
        } finally {
            child.destroyForcibly();
            child.waitFor(30, TimeUnit.SECONDS);
            for (var instance : runtime.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list()) {
                for (var task : tasks.createTaskQuery().processInstanceId(instance.getId()).list()) {
                    deleteNotification(jdbc, "workflow:todo:" + task.getId());
                }
            }
        }
    }

    private static void awaitFile(Path path, Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        while (!Files.exists(path) && process.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(Files.exists(path), "第二个应用未就绪或未返回结果");
    }

    /** 第二个独立 JVM 共享正式测试库，并在文件屏障放行后启动相同业务键。 */
    public static final class SecondWorkflowApplication {
        private SecondWorkflowApplication() {
        }

        public static void main(String[] args) throws Exception {
            Path ready = Path.of(args[0]);
            Path release = Path.of(args[1]);
            Path armed = Path.of(args[2]);
            Path result = Path.of(args[3]);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spectra.scheduler.quartz.enabled=false", "--flowable.async-executor-activate=false",
                            "--spectra.security.secret.master-key=" + randomSecret())) {
                Files.writeString(ready, "ready");
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
                while (!Files.exists(release) && System.nanoTime() < deadline) {
                    Thread.sleep(20);
                }
                if (!Files.exists(release)) {
                    Files.writeString(result, "failure:timeout");
                    return;
                }
                Files.writeString(armed, "starting");
                try {
                    String instanceId = application.getBean(ProcessInstanceService.class)
                            .start(args[4], args[5], Map.of());
                    Files.writeString(result, "success:" + instanceId);
                } catch (RuntimeException exception) {
                    Files.writeString(result, "failure:" + exception.getClass().getSimpleName());
                }
            }
        }
    }

    private static void authenticate(UUID userId, String username) {
        var principal = mock(SecurityPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        when(principal.getUsername()).thenReturn(username);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private static boolean completeOnce(CountDownLatch start, UUID userId, String username, String taskId,
                                        com.devops00.spectra.workflow.api.TaskService workflowTasks)
            throws InterruptedException {
        start.await();
        authenticate(userId, username);
        try {
            workflowTasks.complete(taskId, "synthetic approval", username);
            return true;
        } catch (RuntimeException exception) {
            return false;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static long requestCount(JdbcTemplate jdbc, String idempotencyKey) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                + "WHERE idempotency_key = ?", Long.class, idempotencyKey);
    }

    private static long workflowTodoCount(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                + "WHERE source_module = 'WORKFLOW' AND purpose = 'WORKFLOW_TODO'", Long.class);
    }

    private static UUID recipientFor(JdbcTemplate jdbc, String idempotencyKey) {
        return jdbc.queryForObject("SELECT t.receiver_user_id FROM spectra_notification.ntf_task t "
                + "JOIN spectra_notification.ntf_request r ON r.id = t.notification_request_id "
                + "WHERE r.idempotency_key = ? AND t.channel = 'IN_APP'", UUID.class, idempotencyKey);
    }

    private static void deleteNotification(JdbcTemplate jdbc, String idempotencyKey) {
        if (idempotencyKey == null) {
            return;
        }
        var requestIds = jdbc.queryForList("SELECT id FROM spectra_notification.ntf_request "
                + "WHERE idempotency_key = ?", UUID.class, idempotencyKey);
        for (var requestId : requestIds) {
            jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_request_id = ?", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                    + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
        }
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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
}
