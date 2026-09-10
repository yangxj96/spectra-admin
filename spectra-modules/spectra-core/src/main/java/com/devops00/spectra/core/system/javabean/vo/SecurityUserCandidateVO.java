/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import java.util.UUID;

/** 安全运维用户候选；只返回定位目标所需的最小用户资料。 */
public record SecurityUserCandidateVO(UUID id, String username, String realName, String employeeNo) {
}
