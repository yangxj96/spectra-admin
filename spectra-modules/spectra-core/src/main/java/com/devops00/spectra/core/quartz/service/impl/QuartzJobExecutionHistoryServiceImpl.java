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

package com.devops00.spectra.core.quartz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import com.devops00.spectra.core.quartz.mapper.QuartzJobExecutionHistoryMapper;
import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Quartz 执行历史服务；监听写入失败由 listener 隔离，不影响任务执行。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzJobExecutionHistoryServiceImpl implements QuartzJobExecutionHistoryService {

    private final QuartzJobExecutionHistoryMapper mapper;
    private final QuartzSchedulerProperties properties;
    private final Map<String, UUID> historyIds = new ConcurrentHashMap<>();

    @Override
    public void started(JobExecutionContext context) {
        var entity = baseEntity(context, QuartzExecutionHistoryStatus.RUNNING, Instant.now());
        insertIdempotently(entity);
        if (context.getFireInstanceId() != null) {
            historyIds.put(context.getFireInstanceId(), entity.getId());
        }
    }

    @Override
    public void completed(JobExecutionContext context, JobExecutionException exception) {
        var finishedAt = Instant.now();
        var id = context.getFireInstanceId() == null ? null : historyIds.remove(context.getFireInstanceId());
        if (id == null) {
            return;
        }
        var startedAt = context.getJobRunTime() < 0 ? finishedAt : finishedAt.minusMillis(context.getJobRunTime());
        var status = exception == null ? QuartzExecutionHistoryStatus.SUCCEEDED : QuartzExecutionHistoryStatus.FAILED;
        mapper.finish(id, status, finishedAt, Math.max(0L, Duration.between(startedAt, finishedAt).toMillis()),
                exception == null ? resultSummary(context) : null,
                exception == null ? null : safeErrorCode(exception),
                exception == null ? null : safeErrorMessage(exception));
    }

    @Override
    public void vetoed(JobExecutionContext context) {
        var finishedAt = Instant.now();
        var entity = baseEntity(context, QuartzExecutionHistoryStatus.VETOED, finishedAt);
        entity.setFinishedAt(finishedAt);
        entity.setDurationMs(0L);
        entity.setResultSummary("Quartz 在执行前否决任务");
        insertIdempotently(entity);
    }

    @Override
    public int cleanup() {
        var now = Instant.now();
        var batchSize = properties.getHistoryCleanupBatchSize();
        var total = 0;
        int changed;
        do {
            changed = mapper.abandonExpired(now.minus(properties.getHistoryRunningTimeout()), now,
                    "RUNNING_TIMEOUT", "执行超过运行租约，未收到 Quartz 完成回调", batchSize);
            total += changed;
        } while (changed == batchSize);
        do {
            changed = mapper.deleteExpired(now.minus(Duration.ofDays(properties.getHistoryRetentionDays())), batchSize);
            total += changed;
        } while (changed == batchSize);
        return total;
    }

    @Override
    public IPage<QuartzJobExecutionHistoryEntity> page(long current, long size, String jobKey,
                                                       String triggerKey, QuartzExecutionHistoryStatus status,
                                                       Instant startedAtFrom, Instant startedAtTo) {
        var safeCurrent = Math.max(1L, current);
        var safeSize = Math.max(1L, Math.min(size, 200L));
        return mapper.selectHistoryPage(new Page<>(safeCurrent, safeSize), normalize(jobKey), normalize(triggerKey),
                status, startedAtFrom, startedAtTo);
    }

    @Override
    public java.util.Optional<QuartzJobExecutionHistoryEntity> find(UUID id) {
        return java.util.Optional.ofNullable(id == null ? null : mapper.selectById(id));
    }

    /** 插入执行历史并在重复 Quartz 回调时复用已有主键。 */
    private void insertIdempotently(QuartzJobExecutionHistoryEntity entity) {
        try {
            mapper.insert(entity);
        } catch (DuplicateKeyException exception) {
            var existingId = mapper.selectIdByFireInstanceId(entity.getFireInstanceId());
            if (existingId == null) {
                throw exception;
            }
            entity.setId(existingId);
        }
    }

    private QuartzJobExecutionHistoryEntity baseEntity(JobExecutionContext context,
                                                       QuartzExecutionHistoryStatus status,
                                                       Instant startedAt) {
        var entity = new QuartzJobExecutionHistoryEntity();
        entity.setFireInstanceId(context.getFireInstanceId());
        entity.setJobKey(context.getJobDetail().getKey().toString());
        entity.setTriggerKey(context.getTrigger().getKey().toString());
        entity.setJobType(context.getMergedJobDataMap().getString("spectra.job.type"));
        entity.setJobClassName(context.getJobDetail().getJobClass().getName());
        entity.setTriggerType(triggerType(context));
        entity.setStatus(status);
        entity.setScheduledFireAt(toInstant(context.getScheduledFireTime()));
        entity.setActualFireAt(toInstant(context.getFireTime()));
        if (entity.getActualFireAt() == null) {
            entity.setActualFireAt(startedAt);
        }
        entity.setStartedAt(startedAt);
        entity.setSchedulerInstance(schedulerInstance(context));
        entity.setCorrelationId(context.getMergedJobDataMap().getString("spectra.correlation.id"));
        entity.setParameterVersion(context.getMergedJobDataMap().getString("spectra.parameter.version"));
        entity.setParameterSha256(context.getMergedJobDataMap().getString("spectra.parameter.sha256"));
        return entity;
    }

    private String schedulerInstance(JobExecutionContext context) {
        try {
            return context.getScheduler().getSchedulerInstanceId();
        } catch (Exception exception) {
            return null;
        }
    }

    private static String triggerType(JobExecutionContext context) {
        if (context.getTrigger() instanceof CronTrigger) {
            return "CRON";
        }
        if (context.getTrigger() instanceof SimpleTrigger) {
            return "SIMPLE";
        }
        return "UNKNOWN";
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    private static String safeErrorCode(Throwable exception) {
        return exception instanceof JobExecutionException jobException && jobException.getCause() != null
                ? jobException.getCause().getClass().getSimpleName()
                : exception.getClass().getSimpleName();
    }

    private static String safeErrorMessage(Throwable exception) {
        var message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return safeErrorCode(exception);
        }
        var sanitized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }

    /** 将 Job 自己提供的安全结果摘要限制在历史表字段长度内。 */
    private static String resultSummary(JobExecutionContext context) {
        var result = context.getResult();
        if (result == null) {
            return "执行完成";
        }
        var summary = String.valueOf(result).replaceAll("[\\r\\n\\t]+", " ").trim();
        return summary.length() > 500 ? summary.substring(0, 500) : summary;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
