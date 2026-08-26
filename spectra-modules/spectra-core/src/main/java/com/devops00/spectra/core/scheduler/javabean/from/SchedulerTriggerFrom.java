package com.devops00.spectra.core.scheduler.javabean.from;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 离散任务手工触发参数。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerTriggerFrom {

    private Map<String, Object> parameters = Map.of();

    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    @NotBlank(message = "触发原因不能为空")
    private String reason;
}
