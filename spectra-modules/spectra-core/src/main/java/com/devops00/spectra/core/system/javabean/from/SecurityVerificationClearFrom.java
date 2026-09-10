/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 按受控验证码类型和目标清理验证码状态的请求。 */
public record SecurityVerificationClearFrom(
        SecurityVerificationType type,
        String target,
        boolean clearAttempts,
        String reason,
        boolean confirmed) {
}
