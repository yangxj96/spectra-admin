package com.devops00.spectra.core.scheduler.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 调度定义状态变更参数。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerOperationFrom {

    @NotNull(message = "版本不能为空")
    @PositiveOrZero(message = "版本不能小于 0")
    private Long version;

    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    @NotBlank(message = "操作原因不能为空")
    private String reason;
}
