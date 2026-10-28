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

package com.devops00.spectra.launch.notification;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.dispatch.NotificationTaskResultRecorder;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.service.NotificationAdminService;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import com.devops00.spectra.core.quartz.mapper.QuartzJobExecutionHistoryMapper;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 在隔离 PostgreSQL 上验证通知结果事务、租约 fencing、过期收敛和人工重试语义。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class B06NotificationCorePersistenceTest {

    private ConfigurableApplicationContext application;
    private JdbcTemplate jdbc;
    private NotificationTaskMapper taskMapper;
    private NotificationTaskResultRecorder recorder;
    private NotificationAdminService adminService;
    private QuartzJobExecutionHistoryMapper historyMapper;

    @BeforeAll
    void startApplication() {
        requireIsolatedDependencies();
        application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=false", "--spring.quartz.auto-startup=false",
                        "--flowable.async-executor-activate=false");
        jdbc = new JdbcTemplate(application.getBean(DataSource.class));
        taskMapper = application.getBean(NotificationTaskMapper.class);
        recorder = application.getBean(NotificationTaskResultRecorder.class);
        adminService = application.getBean(NotificationAdminService.class);
        historyMapper = application.getBean(QuartzJobExecutionHistoryMapper.class);
        purgeB06Rows();
    }

    @AfterAll
    void stopApplication() {
        if (application != null) {
            purgeB06Rows();
            application.close();
        }
    }

    @Test
    void recordsDeliveryAndAdvancesTaskInOneTransaction() {
        var requestId = insertRequest();
        var taskId = insertTask(requestId, "PROCESSING", 0, Instant.now().minusSeconds(1), "worker-a");
        var task = taskMapper.selectById(taskId);

        recorder.record(task, ChannelSendResult.sent("MOCK", "message-" + taskId, "MOCK_ACCEPTED"));

        assertEquals("SENT", taskMapper.selectById(taskId).getStatus());
        assertEquals(1, taskMapper.selectById(taskId).getAttemptCount());
        assertNull(taskMapper.selectById(taskId).getLockedBy());
        assertEquals(1, deliveryCount(taskId));
        assertEquals("SENT", jdbc.queryForObject(
                "SELECT result_status FROM spectra_notification.ntf_delivery WHERE notification_task_id = ?",
                String.class, taskId));
        cleanup(requestId, taskId);
    }

    @Test
    void staleLeaseCannotWriteDeliveryOrOverwriteTask() {
        var requestId = insertRequest();
        var claimedAt = Instant.now().minusSeconds(10);
        var taskId = insertTask(requestId, "PROCESSING", 0, claimedAt, "worker-old");
        var stale = taskMapper.selectById(taskId);

        jdbc.update("UPDATE spectra_notification.ntf_task SET locked_by = 'worker-new', locked_at = ? "
                + "WHERE id = ?", Timestamp.from(Instant.now()), taskId);

        assertThrows(DataSaveException.class,
                () -> recorder.record(stale, ChannelSendResult.sent("MOCK", "stale-" + taskId, "MOCK_ACCEPTED")));

        assertEquals(0, deliveryCount(taskId));
        assertEquals("PROCESSING", taskMapper.selectById(taskId).getStatus());
        assertEquals("worker-new", taskMapper.selectById(taskId).getLockedBy());
        cleanup(requestId, taskId);
    }

    @Test
    void overdueAndLeaseExpiredTasksConvergeWithoutAutomaticResend() {
        var requestId = insertRequest();
        var overdueId = insertTask(requestId, "PENDING", 0, null, null);
        jdbc.update("UPDATE spectra_notification.ntf_task SET expires_at = ? WHERE id = ?",
                Timestamp.from(Instant.now().minusSeconds(1)), overdueId);
        var leasedId = insertTask(requestId, "PROCESSING", 0, Instant.now().minusSeconds(600), "worker-dead");

        taskMapper.expireOverdueTasks(Instant.now(), 10);
        taskMapper.expireLeases(Instant.now().minusSeconds(300), 10);

        assertEquals("EXPIRED", taskMapper.selectById(overdueId).getStatus());
        var leased = taskMapper.selectById(leasedId);
        assertEquals("UNKNOWN", leased.getStatus());
        assertEquals("WORKER_LEASE_EXPIRED", leased.getLastErrorCode());
        assertNull(leased.getLockedBy());
        cleanup(requestId, overdueId, leasedId);
    }

    @Test
    void manualRetryKeepsAttemptHistoryAndExtendsBudget() {
        var requestId = insertRequest();
        var taskId = insertTask(requestId, "FAILED", 2, null, null);

        adminService.retry(taskId);

        var retried = taskMapper.selectById(taskId);
        assertEquals("RETRYING", retried.getStatus());
        assertEquals(2, retried.getAttemptCount());
        assertEquals(6, retried.getMaxAttempts());
        assertNull(retried.getLastErrorCode());
        cleanup(requestId, taskId);
    }

    @Test
    void abandonDoesNotOverwriteFinishedExecution() {
        var historyId = UUID.randomUUID();
        var startedAt = Instant.now().minusSeconds(600);
        jdbc.update("INSERT INTO spectra_core.quartz_job_execution_history "
                + "(id, fire_instance_id, job_key, trigger_key, job_type, job_class_name, trigger_type, "
                + "status, actual_fire_at, started_at) "
                + "VALUES (?, ?, 'job', 'trigger', 'type', 'class', 'CRON', 'RUNNING', ?, ?)",
                historyId, "fire-" + historyId, Timestamp.from(startedAt), Timestamp.from(startedAt));

        var finishedAt = Instant.now();
        assertEquals(1, historyMapper.finish(new QuartzJobExecutionHistoryMapper.FinishResult(
                historyId, QuartzExecutionHistoryStatus.SUCCEEDED, finishedAt, 1000L, "ok", null, null)));
        historyMapper.abandonExpired(startedAt.plusSeconds(1), finishedAt, "ABANDONED", "expired", 10);

        assertEquals("SUCCEEDED", jdbc.queryForObject(
                "SELECT status FROM spectra_core.quartz_job_execution_history WHERE id = ?", String.class, historyId));
        jdbc.update("DELETE FROM spectra_core.quartz_job_execution_history WHERE id = ?", historyId);
    }

    private UUID insertRequest() {
        var requestId = UUID.randomUUID();
        jdbc.update("INSERT INTO spectra_notification.ntf_request "
                + "(id, external_request_id, idempotency_key, purpose, template_group_code, source_module, "
                + "business_type, business_id, initiator_type, status) "
                + "VALUES (?, ?, ?, 'SYSTEM_NOTICE', 'system.notice', 'test', 'test', ?, 'SYSTEM', 'ACCEPTED')",
                requestId, "b06-" + requestId, "b06:" + requestId, requestId.toString());
        return requestId;
    }

    private UUID insertTask(UUID requestId, String status, int attemptCount, Instant lockedAt, String lockedBy) {
        var taskId = UUID.randomUUID();
        jdbc.update("INSERT INTO spectra_notification.ntf_task "
                + "(id, notification_request_id, channel, receiver_user_id, recipient_key_hash, purpose, "
                + "title, content, attempt_count, max_attempts, status, locked_by, locked_at) "
                + "VALUES (?, ?, 'IN_APP', ?, ?, 'SYSTEM_NOTICE', 'title', 'content', ?, 3, ?, ?, ?)",
                taskId, requestId, UUID.randomUUID(), "recipient-" + taskId, attemptCount, status, lockedBy,
                lockedAt == null ? null : Timestamp.from(lockedAt));
        return taskId;
    }

    private int deliveryCount(UUID taskId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery "
                + "WHERE notification_task_id = ?", Integer.class, taskId);
    }

    private void cleanup(UUID requestId, UUID... taskIds) {
        for (var taskId : taskIds) {
            jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_task_id = ?", taskId);
            jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id = ?", taskId);
            jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE id = ?", taskId);
        }
        jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
    }

    private void purgeB06Rows() {
        jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_task_id IN "
                + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id IN "
                + "(SELECT id FROM spectra_notification.ntf_request WHERE idempotency_key LIKE 'b06:%'))");
        jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id IN "
                + "(SELECT id FROM spectra_notification.ntf_request WHERE idempotency_key LIKE 'b06:%'))");
        jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id IN "
                + "(SELECT id FROM spectra_notification.ntf_request WHERE idempotency_key LIKE 'b06:%')");
        jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE idempotency_key LIKE 'b06:%'");
    }

    private static void requireIsolatedDependencies() {
        var url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
    }
}
