package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import lombok.Data;

import java.util.UUID;

/** 循环运行会话查询条件。 */
@Data
public class SchedulerLoopPageFrom {
    private UUID jobId;
    private String instanceId;
    private SchedulerRuntimeStatus status;
}
