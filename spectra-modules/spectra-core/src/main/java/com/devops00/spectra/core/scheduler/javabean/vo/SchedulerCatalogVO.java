package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/** 代码注册任务能力目录。 */
@Builder
public record SchedulerCatalogVO(String jobKey,
                                 String handlerKey,
                                 String name,
                                 String module,
                                 ScheduledJobType jobType,
                                 ScheduledRunScope runScope,
                                 ScheduledScheduleKind scheduleKind,
                                 ScheduledEffectType effectType,
                                 Map<String, Object> parameterSchema,
                                 List<String> supportedActions,
                                 Map<String, Object> executionPolicy) {

    public SchedulerCatalogVO {
        parameterSchema = parameterSchema == null ? Map.of() : Map.copyOf(parameterSchema);
        supportedActions = supportedActions == null ? List.of() : List.copyOf(supportedActions);
        executionPolicy = executionPolicy == null ? Map.of() : Map.copyOf(executionPolicy);
    }
}
