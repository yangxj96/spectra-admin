package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerConcurrencyPolicy;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerMisfirePolicy;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** 调度任务定义视图。 */
@Builder
public record SchedulerJobVO(UUID id, String jobKey, String name, String module, String description,
                             String handlerKey, ScheduledJobType jobType, ScheduledRunScope runScope,
                             SchedulerDefinitionStatus definitionStatus, SchedulerDesiredState desiredState,
                             ScheduledScheduleKind scheduleKind, String cronExpression, Long fixedDelayMs,
                             Long initialDelayMs, Instant nextFireAt, SchedulerMisfirePolicy misfirePolicy,
                             SchedulerConcurrencyPolicy concurrencyPolicy, Map<String, Object> executionPolicy,
                             Map<String, Object> parameters, Long revision, Long version) {

    public SchedulerJobVO {
        executionPolicy = executionPolicy == null ? Map.of() : Map.copyOf(executionPolicy);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
