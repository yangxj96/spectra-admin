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

package com.devops00.spectra.core.quartz.listener;

import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Quartz 执行历史监听器；历史写入异常隔离在监听器内，不影响任务执行。 */
@Component
public class QuartzExecutionHistoryJobListener implements JobListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzExecutionHistoryJobListener.class);

    private final QuartzJobExecutionHistoryService historyService;
    private final Counter historyWriteFailureCounter;

    /**
     * 创建使用应用指标注册表的历史监听器。
     *
     * @param historyService 执行历史写入服务
     * @param meterRegistry  应用指标注册表
     */
    @Autowired
    public QuartzExecutionHistoryJobListener(QuartzJobExecutionHistoryService historyService,
                                             MeterRegistry meterRegistry) {
        this.historyService = historyService;
        this.historyWriteFailureCounter = Counter.builder("quartz_execution_history_write_failure_total")
                .description("Quartz 执行历史监听器写入失败次数")
                .register(meterRegistry);
    }

    /**
     * 创建使用本地指标注册表的监听器，供无 Spring 测试场景使用。
     *
     * @param historyService 执行历史写入服务
     */
    public QuartzExecutionHistoryJobListener(QuartzJobExecutionHistoryService historyService) {
        this(historyService, new SimpleMeterRegistry());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "spectraQuartzExecutionHistory";
    }

    /** {@inheritDoc} */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        failOpen("开始", context, () -> historyService.started(context));
    }

    /** {@inheritDoc} */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        failOpen("否决", context, () -> historyService.vetoed(context));
    }

    /** {@inheritDoc} */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        failOpen("完成", context, () -> historyService.completed(context, jobException));
    }

    private void failOpen(String stage, JobExecutionContext context, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            historyWriteFailureCounter.increment();
            LOGGER.warn("Quartz 执行历史记录失败，继续保留任务执行: stage={}, jobKey={}, triggerKey={}, fireInstanceId={}, errorCode={}",
                    stage, safeJobKey(context), safeTriggerKey(context), safeFireInstanceId(context),
                    exception.getClass().getSimpleName());
        }
    }

    private static String safeJobKey(JobExecutionContext context) {
        try {
            return context == null || context.getJobDetail() == null
                    ? "unknown"
                    : context.getJobDetail().getKey().toString();
        } catch (RuntimeException exception) {
            return "unknown";
        }
    }

    private static String safeTriggerKey(JobExecutionContext context) {
        try {
            return context == null || context.getTrigger() == null
                    ? "unknown"
                    : context.getTrigger().getKey().toString();
        } catch (RuntimeException exception) {
            return "unknown";
        }
    }

    private static String safeFireInstanceId(JobExecutionContext context) {
        try {
            return context == null || context.getFireInstanceId() == null
                    ? "unknown"
                    : context.getFireInstanceId();
        } catch (RuntimeException exception) {
            return "unknown";
        }
    }
}
