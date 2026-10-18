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

import com.devops00.spectra.common.notification.NotificationSystemActor;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import com.devops00.spectra.launch.LaunchApplication;
import com.devops00.spectra.oa.contract.service.ContractService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用隔离 PostgreSQL、Redis 和正式 Quartz 验证 OA 合同提醒的运行身份与并发领取。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class OaContractReminderQuartzFullHttpTest {

    @Test
    void contractQuartzUsesScopedSystemReadAndReminderIsClaimedOnce() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=true", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var service = application.getBean(ContractService.class);
            var scheduler = application.getBean(QuartzSchedulerLifecycle.class).scheduler();
            var jobKey = new JobKey("oa.contract.milestone-reminder", "SPECTRA_BUILTIN");
            var triggerKey = new TriggerKey("oa.contract.milestone-reminder.trigger", "SPECTRA_BUILTIN");
            var workerTrigger = new TriggerKey("notification.task-worker.trigger", "SPECTRA_BUILTIN");
            var userId = UUID.randomUUID();
            var contractId = UUID.randomUUID();
            var firstMilestone = UUID.randomUUID();
            var raceMilestone = UUID.randomUUID();
            var username = "b02contract" + UUID.randomUUID().toString().replace("-", "");
            var historyStart = Instant.now();
            assertTrue(scheduler.checkExists(jobKey));
            scheduler.pauseTrigger(triggerKey);
            scheduler.pauseTrigger(workerTrigger);
            try {
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", userId, username, username, "synthetic contract recipient");
                jdbc.update("INSERT INTO spectra_oa.oa_contract "
                        + "(id, contract_no, title, owner_id, status, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, 'ACTIVE', now(), now())",
                        contractId, "B02-" + contractId, "synthetic contract", userId);
                insertMilestone(jdbc, firstMilestone, contractId);
                SecurityContextHolder.clearContext();

                assertThrows(AccessDeniedException.class, service::sendDueMilestoneReminders,
                        "手动入口无登录身份必须拒绝");
                assertThrows(AccessDeniedException.class,
                        () -> service.sendDueMilestoneRemindersAsSystem(NotificationSystemActor.SERVICE_MONITOR_ALERT),
                        "错误系统身份不得读取合同");
                assertEquals(0L, requestCount(jdbc, firstMilestone));

                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> successCount(jdbc, historyStart) >= 1L
                        && requestCount(jdbc, firstMilestone) == 1L));
                assertEquals(1L, markedCount(jdbc, firstMilestone));
                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> successCount(jdbc, historyStart) >= 2L));
                assertEquals(1L, requestCount(jdbc, firstMilestone), "重复 Quartz 触发不得重复入队");

                insertMilestone(jdbc, raceMilestone, contractId);
                jdbc.update("UPDATE spectra_oa.oa_contract SET status = 'ARCHIVED' WHERE id = ?", contractId);
                assertEquals(0, service.sendDueMilestoneRemindersAsSystem(NotificationSystemActor.OA_CONTRACT_REMINDER));
                assertEquals(0L, requestCount(jdbc, raceMilestone), "归档合同不得发送提醒");
                assertEquals(0L, markedCount(jdbc, raceMilestone));
                jdbc.update("UPDATE spectra_oa.oa_contract SET status = 'ACTIVE' WHERE id = ?", contractId);
                try (var executor = Executors.newFixedThreadPool(2)) {
                    var first = executor.submit(() -> service.sendDueMilestoneRemindersAsSystem(
                            NotificationSystemActor.OA_CONTRACT_REMINDER));
                    var second = executor.submit(() -> service.sendDueMilestoneRemindersAsSystem(
                            NotificationSystemActor.OA_CONTRACT_REMINDER));
                    assertEquals(1, first.get(30, TimeUnit.SECONDS) + second.get(30, TimeUnit.SECONDS));
                }
                assertEquals(1L, requestCount(jdbc, raceMilestone), "并发扫描只允许一个通知请求");
                assertEquals(1L, markedCount(jdbc, raceMilestone));
            } finally {
                deleteNotification(jdbc, raceMilestone);
                deleteNotification(jdbc, firstMilestone);
                jdbc.update("DELETE FROM spectra_oa.oa_contract_milestone WHERE contract_id = ?", contractId);
                jdbc.update("DELETE FROM spectra_oa.oa_contract WHERE id = ?", contractId);
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", userId);
                jdbc.update("DELETE FROM spectra_core.quartz_job_execution_history "
                        + "WHERE job_type = 'oa.contract.milestone-reminder' AND started_at >= ?",
                        Timestamp.from(historyStart));
                scheduler.resumeTrigger(workerTrigger);
                scheduler.resumeTrigger(triggerKey);
                SecurityContextHolder.clearContext();
            }
        }
    }

    private static void insertMilestone(JdbcTemplate jdbc, UUID milestoneId, UUID contractId) {
        jdbc.update("INSERT INTO spectra_oa.oa_contract_milestone "
                + "(id, contract_id, name, due_date, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, 'PENDING', now(), now())",
                milestoneId, contractId, "synthetic milestone", Timestamp.from(Instant.now().plus(Duration.ofDays(1))));
    }

    private static long requestCount(JdbcTemplate jdbc, UUID milestoneId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request WHERE idempotency_key = ?",
                Long.class, "oa:contract-milestone:" + milestoneId);
    }

    private static long markedCount(JdbcTemplate jdbc, UUID milestoneId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_oa.oa_contract_milestone "
                + "WHERE id = ? AND reminder_sent_at IS NOT NULL", Long.class, milestoneId);
    }

    private static long successCount(JdbcTemplate jdbc, Instant since) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_core.quartz_job_execution_history "
                + "WHERE job_type = 'oa.contract.milestone-reminder' AND status = 'SUCCEEDED' "
                + "AND started_at >= ?", Long.class, Timestamp.from(since));
    }

    private static void deleteNotification(JdbcTemplate jdbc, UUID milestoneId) {
        var requestIds = jdbc.queryForList("SELECT id FROM spectra_notification.ntf_request "
                + "WHERE idempotency_key = ?", UUID.class, "oa:contract-milestone:" + milestoneId);
        for (var requestId : requestIds) {
            jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_request_id = ?", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                    + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
            jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
        }
    }

    private static boolean await(Duration timeout, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(100);
        }
        return condition.getAsBoolean();
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
