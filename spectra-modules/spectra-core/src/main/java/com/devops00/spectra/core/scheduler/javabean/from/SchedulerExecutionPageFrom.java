package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import lombok.Data;

import java.util.UUID;

/** 离散执行查询条件。 */
@Data
public class SchedulerExecutionPageFrom {
    private UUID jobId;
    private SchedulerExecutionStatus status;
    private String fireKey;
}
