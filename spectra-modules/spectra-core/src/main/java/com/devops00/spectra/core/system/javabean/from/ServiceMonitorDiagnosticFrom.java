/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 服务监控诊断任务请求。 */
@Data
public class ServiceMonitorDiagnosticFrom {

    @NotBlank(message = "诊断类型不能为空")
    private String taskType;

    /** 堆转储等高风险操作必须显式确认。 */
    private Boolean confirm;
}
