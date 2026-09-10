/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import java.util.UUID;

/** 验证码目标候选；target 仅用于受控提交，页面展示使用脱敏值。 */
public record SecurityVerificationCandidateVO(String target, String maskedTarget, UUID userId, String username,
                                              String realName, String employeeNo) {
}
