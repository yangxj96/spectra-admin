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

import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationSystemActor;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import com.devops00.spectra.launch.LaunchApplication;
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
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用正式 PostgreSQL、Redis 与 Quartz 验证无登录身份通知拒绝和显式系统身份的后台投递。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class NotificationSystemActorQuartzFullHttpTest {

    @Test
    void onlyRegisteredSystemActorCanEnqueueAndQuartzDeliversOnce() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=true", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var notifications = application.getBean(NotificationService.class);
            var lifecycle = application.getBean(QuartzSchedulerLifecycle.class);
            var scheduler = lifecycle.scheduler();
            var jobKey = new JobKey("notification.task-worker", "SPECTRA_BUILTIN");
            var triggerKey = new TriggerKey("notification.task-worker.trigger", "SPECTRA_BUILTIN");
            UUID userId = UUID.randomUUID();
            String username = "b02actor" + UUID.randomUUID().toString().replace("-", "");
            String idempotencyKey = "b02:system-alert:" + UUID.randomUUID();
            UUID requestId = null;
            Instant historyStart = null;
            assertTrue(lifecycle.isReady());
            assertTrue(scheduler.checkExists(jobKey));
            scheduler.pauseTrigger(triggerKey);
            try {
                jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                        + "VALUES (?, ?, ?, ?)", userId, username, username, "synthetic system actor recipient");
                var request = NotificationSendRequest.inApp(idempotencyKey, NotificationPurpose.SYSTEM_NOTICE,
                        List.of(userId), NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT)
                        .parameter("rule_name", "synthetic rule")
                        .parameter("message", "synthetic alert")
                        .businessReference("SERVICE_MONITOR_ALERT", userId.toString())
                        .sourceModule("spectra-core")
                        .build();
                SecurityContextHolder.clearContext();
                assertThrows(AccessDeniedException.class, () -> notifications.send(request),
                        "普通通知入口缺少认证身份必须拒绝");
                assertThrows(AccessDeniedException.class,
                        () -> notifications.sendAsSystem(request, NotificationSystemActor.OA_CONTRACT_REMINDER),
                        "不匹配的系统身份不得借用后台投递权限");
                assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_request "
                        + "WHERE idempotency_key = ?", Long.class, idempotencyKey));

                var receipt = notifications.sendAsSystem(request, NotificationSystemActor.SERVICE_MONITOR_ALERT);
                requestId = receipt.requestId();
                assertEquals(1, receipt.taskCount());
                assertEquals(1L, taskStatusCount(jdbc, requestId, "PENDING"));
                historyStart = Instant.now();
                scheduler.triggerJob(jobKey);
                UUID deliveredRequestId = requestId;
                Instant startedAt = historyStart;
                assertTrue(await(Duration.ofSeconds(30), () -> taskStatusCount(jdbc, deliveredRequestId, "SENT") == 1L
                        && quartzSuccessCount(jdbc, startedAt) >= 1L));
                assertEquals(1L, inboxCount(jdbc, requestId));
                assertEquals(1L, deliveryCount(jdbc, requestId));
                scheduler.triggerJob(jobKey);
                assertTrue(await(Duration.ofSeconds(30), () -> quartzSuccessCount(jdbc, startedAt) >= 2L));
                assertEquals(1L, inboxCount(jdbc, requestId), "重复 Quartz 触发不得重复写入收件箱");
                assertEquals(1L, deliveryCount(jdbc, requestId), "重复 Quartz 触发不得重复写入投递记录");
            } finally {
                scheduler.resumeTrigger(triggerKey);
                if (requestId != null) {
                    jdbc.update("DELETE FROM spectra_notification.ntf_inbox_message WHERE notification_request_id = ?", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_delivery WHERE notification_task_id IN "
                            + "(SELECT id FROM spectra_notification.ntf_task WHERE notification_request_id = ?)", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_task WHERE notification_request_id = ?", requestId);
                    jdbc.update("DELETE FROM spectra_notification.ntf_request WHERE id = ?", requestId);
                }
                if (historyStart != null) {
                    jdbc.update("DELETE FROM spectra_core.quartz_job_execution_history "
                            + "WHERE job_type = 'notification.task-worker' AND started_at >= ?",
                            Timestamp.from(historyStart));
                }
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", userId);
                SecurityContextHolder.clearContext();
            }
        }
    }

    private static long taskStatusCount(JdbcTemplate jdbc, UUID requestId, String status) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_task "
                + "WHERE notification_request_id = ? AND status = ?", Long.class, requestId, status);
    }

    private static long inboxCount(JdbcTemplate jdbc, UUID requestId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_inbox_message "
                + "WHERE notification_request_id = ?", Long.class, requestId);
    }

    private static long deliveryCount(JdbcTemplate jdbc, UUID requestId) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_notification.ntf_delivery d "
                + "JOIN spectra_notification.ntf_task t ON t.id = d.notification_task_id "
                + "WHERE t.notification_request_id = ?", Long.class, requestId);
    }

    private static long quartzSuccessCount(JdbcTemplate jdbc, Instant since) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_core.quartz_job_execution_history "
                + "WHERE job_type = 'notification.task-worker' AND status = 'SUCCEEDED' "
                + "AND started_at >= ?", Long.class, Timestamp.from(since));
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
