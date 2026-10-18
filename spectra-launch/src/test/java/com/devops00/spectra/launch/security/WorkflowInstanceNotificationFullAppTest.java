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
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.launch.LaunchApplication;
import com.devops00.spectra.workflow.api.ProcessInstanceService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            var firstUserId = UUID.randomUUID();
            var secondUserId = UUID.randomUUID();
            var firstUsername = "b02wf" + UUID.randomUUID().toString().replace("-", "");
            var secondUsername = "b02wf" + UUID.randomUUID().toString().replace("-", "");
            var processKey = "b02Workflow" + UUID.randomUUID().toString().replace("-", "");
            var deploymentId = (String) null;
            var firstTaskId = (String) null;
            var secondTaskId = (String) null;
            try {
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", firstUserId, firstUsername, firstUsername, "synthetic workflow user one");
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", secondUserId, secondUsername, secondUsername, "synthetic workflow user two");
                deploymentId = repository.createDeployment()
                        .addString(processKey + ".bpmn20.xml", twoStepProcess(processKey, firstUsername, secondUsername))
                        .deploy()
                        .getId();
                String processInstanceId = processService.start(processKey, "b02:" + UUID.randomUUID(), Map.of());
                var firstTask = flowableTasks.createTaskQuery().processInstanceId(processInstanceId).singleResult();
                assertNotNull(firstTask);
                firstTaskId = firstTask.getId();
                assertEquals(firstUsername, firstTask.getAssignee());
                String firstTaskKey = "workflow:result:" + firstTaskId + ":true";
                assertEquals(0L, requestCount(jdbc, firstTaskKey));
                String wrongOperatorTaskId = firstTaskId;
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
                assertEquals(1L, requestCount(jdbc, nextTaskKey));

                String finalTaskId = secondTaskId;
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
            } finally {
                SecurityContextHolder.clearContext();
                deleteNotification(jdbc, firstTaskId == null ? null : "workflow:result:" + firstTaskId + ":true");
                deleteNotification(jdbc, secondTaskId == null ? null : "workflow:todo:" + secondTaskId);
                deleteNotification(jdbc, secondTaskId == null ? null : "workflow:result:" + secondTaskId + ":true");
                if (deploymentId != null) {
                    repository.deleteDeployment(deploymentId, true);
                }
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
