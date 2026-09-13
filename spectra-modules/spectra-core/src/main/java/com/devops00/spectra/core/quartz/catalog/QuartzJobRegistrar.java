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

package com.devops00.spectra.core.quartz.catalog;

import com.devops00.spectra.common.port.quartz.QuartzJobDefinition;
import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

/**
 * 启动时只补注册缺失的内置 JobDetail 和一对一 Trigger。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
@RequiredArgsConstructor
public class QuartzJobRegistrar {

    private static final String BUILTIN_GROUP = "SPECTRA_BUILTIN";

    private final QuartzJobCatalog catalog;

    /**
     * 仅创建缺失的内置 Quartz 资源，不覆盖管理员已有修改。
     *
     * @param scheduler 已进入 READY 前的 JDBC Quartz Scheduler
     * @throws SchedulerException Quartz 资源读写失败
     */
    public void register(Scheduler scheduler) throws SchedulerException {
        for (var definition : catalog.definitions()) {
            if (!definition.builtIn()) {
                continue;
            }
            var jobKey = new JobKey(definition.builtInJobKey().orElseThrow(), BUILTIN_GROUP);
            if (!scheduler.checkExists(jobKey)) {
                scheduler.addJob(jobDetail(definition, jobKey), false);
            }
            definition.defaultTrigger().ifPresent(template -> registerTrigger(scheduler, template, jobKey));
        }
    }

    /** 创建只含白名单元数据和参数 schema 版本的 JobDetail。 */
    private org.quartz.JobDetail jobDetail(QuartzJobDefinition definition, JobKey jobKey) {
        var data = new JobDataMap();
        data.put("spectra.job.type", definition.typeKey());
        data.put("spectra.parameter.version", definition.parameterSchema().version());
        data.put("spectra.job.built-in", true);
        return JobBuilder.newJob(definition.jobClass())
                .withIdentity(jobKey)
                .withDescription(definition.displayName())
                .usingJobData(data)
                .storeDurably()
                .requestRecovery(false)
                .build();
    }

    /** 只在固定 TriggerKey 缺失时创建默认 Trigger。 */
    private void registerTrigger(Scheduler scheduler, QuartzTriggerTemplate template, JobKey jobKey) {
        var triggerKey = new TriggerKey(jobKey.getName() + ".trigger", BUILTIN_GROUP);
        try {
            if (!scheduler.checkExists(triggerKey)) {
                scheduler.scheduleJob(template.build(triggerKey, jobKey));
            }
        } catch (SchedulerException exception) {
            throw new IllegalStateException("内置 Quartz Trigger 注册失败: " + jobKey.getName(), exception);
        }
    }
}
