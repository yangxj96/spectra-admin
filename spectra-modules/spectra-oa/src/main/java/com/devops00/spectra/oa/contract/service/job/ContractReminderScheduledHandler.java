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

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** 合同履约节点提醒的统一调度适配器。 */
@Component
public class ContractReminderScheduledHandler implements ScheduledJobHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("oa.contract.milestone-reminder")
            .handlerKey("oa.contract.milestone-reminder")
            .name("合同里程碑提醒")
            .module("oa")
            .jobType(ScheduledJobType.OPS)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.CRON)
            .effectType(ScheduledEffectType.OUTBOX)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "TRIGGER", "RETRY"))
            .executionPolicy(Map.of("timeoutMs", 300000L, "leaseDurationMs", 600000L, "maxAttempts", 3))
            .build();

    private final ContractReminderJob reminderJob;

    public ContractReminderScheduledHandler(ContractReminderJob reminderJob) {
        this.reminderJob = reminderJob;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledJobResult execute(ScheduledJobContext context) {
        var sent = reminderJob.sendDueMilestoneReminders();
        return ScheduledJobResult.builder()
                .status(ScheduledJobResult.Status.SUCCEEDED)
                .resultSummary(Map.of("sent", sent))
                .build();
    }
}
