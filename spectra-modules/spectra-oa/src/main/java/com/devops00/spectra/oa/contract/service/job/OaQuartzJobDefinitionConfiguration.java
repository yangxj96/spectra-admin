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

package com.devops00.spectra.oa.contract.service.job;

import com.devops00.spectra.common.port.scheduler.quartz.QuartzBuiltInJobDefinition;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzParameterSchema;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzTriggerTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneOffset;
import java.util.Optional;

/** 注册 OA 模块拥有的 Quartz 内置任务。 */
@Configuration(proxyBeanMethods = false)
public class OaQuartzJobDefinitionConfiguration {

    /** @return 合同里程碑提醒内置任务定义 */
    @Bean
    public QuartzBuiltInJobDefinition contractReminderQuartzJobDefinition() {
        return new QuartzBuiltInJobDefinition(
                "oa.contract.milestone-reminder",
                "合同里程碑提醒",
                ContractReminderQuartzJob.class,
                "oa.contract.milestone-reminder",
                QuartzParameterSchema.empty(),
                Optional.of(QuartzTriggerTemplate.cron("0 0 1 * * ?", ZoneOffset.UTC,
                        QuartzTriggerTemplate.MisfirePolicy.DO_NOTHING)));
    }
}
