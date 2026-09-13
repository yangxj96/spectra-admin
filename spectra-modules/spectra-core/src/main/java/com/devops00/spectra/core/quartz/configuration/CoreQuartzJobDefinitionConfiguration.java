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

package com.devops00.spectra.core.quartz.configuration;

import com.devops00.spectra.core.notification.job.NotificationCleanupQuartzJob;
import com.devops00.spectra.core.notification.job.NotificationTaskQuartzJob;
import com.devops00.spectra.common.port.quartz.QuartzBuiltInJobDefinition;
import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import com.devops00.spectra.core.quartz.job.QuartzExecutionHistoryCleanupJob;
import com.devops00.spectra.common.port.quartz.QuartzParameterSchema;
import com.devops00.spectra.core.system.job.ServiceMonitorDiagnosticCleanupQuartzJob;
import com.devops00.spectra.core.system.job.ServiceMonitorSnapshotQuartzJob;
import com.devops00.spectra.core.upload.job.FileUploadCleanupQuartzJob;
import org.quartz.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Core 内置 Quartz Job 白名单及默认 Trigger 定义。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Configuration(proxyBeanMethods = false)
public class CoreQuartzJobDefinitionConfiguration {

    private static final QuartzParameterSchema EMPTY_PARAMETERS = QuartzParameterSchema.empty();

    /**
     * 定义每 5 秒批量处理通知任务的内置 Quartz 作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition notificationTaskQuartzJobDefinition() {
        return definition("notification.task-worker", "通知任务批处理", NotificationTaskQuartzJob.class,
                QuartzTriggerTemplate.fixedInterval(Duration.ofSeconds(5), QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
    }

    /**
     * 定义定期清理通知敏感载荷的内置 Quartz 作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition notificationCleanupQuartzJobDefinition() {
        return definition("notification.cleanup-sensitive-payload", "通知敏感载荷清理", NotificationCleanupQuartzJob.class,
                QuartzTriggerTemplate.fixedInterval(Duration.ofHours(1), QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
    }

    /**
     * 定义定期清理过期文件上传数据的内置 Quartz 作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition fileUploadCleanupQuartzJobDefinition() {
        return definition("file.upload.cleanup", "文件上传清理", FileUploadCleanupQuartzJob.class,
                QuartzTriggerTemplate.fixedInterval(Duration.ofMinutes(5), QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
    }

    /**
     * 定义定期采集服务监控快照的内置 Quartz 作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition serviceMonitorSnapshotQuartzJobDefinition() {
        return definition("system.monitor.collect-snapshot", "服务监控采样", ServiceMonitorSnapshotQuartzJob.class,
                QuartzTriggerTemplate.fixedInterval(Duration.ofSeconds(10), QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
    }

    /**
     * 定义清理过期服务监控诊断数据的内置 Quartz 作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition serviceMonitorDiagnosticCleanupQuartzJobDefinition() {
        return definition("system.monitor.diagnostic-cleanup", "监控诊断清理", ServiceMonitorDiagnosticCleanupQuartzJob.class,
                QuartzTriggerTemplate.fixedInterval(Duration.ofHours(1), QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
    }

    /**
     * 定义清理过期 Quartz 执行历史的内置作业。
     *
     * @return Quartz作业数据。
     */
    @Bean
    public QuartzBuiltInJobDefinition quartzExecutionHistoryCleanupJobDefinition() {
        return definition("system.scheduler.execution-history-cleanup", "Quartz 执行历史清理",
                QuartzExecutionHistoryCleanupJob.class,
                QuartzTriggerTemplate.cron("0 0 2 * * ?", ZoneOffset.UTC,
                        QuartzTriggerTemplate.MisfirePolicy.DO_NOTHING));
    }

    /**
     * 处理Quartz作业相关数据。
     */
    private QuartzBuiltInJobDefinition definition(String key, String displayName,
                                                  Class<? extends Job> jobClass,
                                                  QuartzTriggerTemplate trigger) {
        return new QuartzBuiltInJobDefinition(key, displayName, jobClass, key, EMPTY_PARAMETERS,
                Optional.of(trigger));
    }
}
