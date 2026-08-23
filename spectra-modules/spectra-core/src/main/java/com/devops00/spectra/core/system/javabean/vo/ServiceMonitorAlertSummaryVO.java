/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

/** 服务监控告警摘要。 */
@Data
@Builder
public class ServiceMonitorAlertSummaryVO {

    private long activeCount;
    private long warningCount;
    private long criticalCount;
    private long recoveredTodayCount;
}
