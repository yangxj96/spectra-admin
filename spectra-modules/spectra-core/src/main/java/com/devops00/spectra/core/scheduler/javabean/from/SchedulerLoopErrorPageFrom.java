package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerLoopErrorStatus;
import lombok.Data;

import java.util.UUID;

/** 循环错误聚合查询条件。 */
@Data
public class SchedulerLoopErrorPageFrom {
    private UUID jobId;
    private String instanceId;
    private SchedulerLoopErrorStatus status;
}
