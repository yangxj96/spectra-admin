package com.devops00.spectra.core.scheduler.javabean.from;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 高频循环控制命令参数。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerLoopCommandFrom {

    @NotNull(message = "命令类型不能为空")
    private SchedulerCommandType commandType;

    private UUID targetRuntimeId;

    private String targetSessionKey;

    @PositiveOrZero(message = "目标版本不能小于 0")
    private Long expectedRuntimeVersion;

    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    @NotBlank(message = "命令原因不能为空")
    private String reason;

    private Instant deadlineAt;
}
