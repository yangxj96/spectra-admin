package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import lombok.Data;

/** 任务定义查询条件。 */
@Data
public class SchedulerJobPageFrom {
    private String jobKey;
    private ScheduledJobType jobType;
    private SchedulerDefinitionStatus definitionStatus;
    private SchedulerDesiredState desiredState;
}
