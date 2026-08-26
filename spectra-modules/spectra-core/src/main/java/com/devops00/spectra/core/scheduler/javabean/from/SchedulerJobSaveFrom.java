package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerConcurrencyPolicy;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerMisfirePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** OPS 任务定义保存参数。任务类型、处理器和运行范围由代码注册表决定。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerJobSaveFrom {

    @NotBlank(message = "任务键不能为空")
    private String jobKey;

    @NotBlank(message = "任务名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "调度类型不能为空")
    private ScheduledScheduleKind scheduleKind;

    private String cronExpression;

    @Positive(message = "固定延迟必须大于 0")
    private Long fixedDelayMs;

    @PositiveOrZero(message = "初始延迟不能小于 0")
    private Long initialDelayMs;

    @NotNull(message = "错过策略不能为空")
    private SchedulerMisfirePolicy misfirePolicy;

    @NotNull(message = "并发策略不能为空")
    private SchedulerConcurrencyPolicy concurrencyPolicy;

    private Map<String, Object> executionPolicy = Map.of();

    private Map<String, Object> parameters = Map.of();

    /** 更新时必须携带当前版本；创建时为空。 */
    private Long version;

    /** 用于操作审计；不参与任务执行参数。 */
    private String idempotencyKey;

    /** 用于操作审计；不参与任务执行参数。 */
    private String reason;
}
