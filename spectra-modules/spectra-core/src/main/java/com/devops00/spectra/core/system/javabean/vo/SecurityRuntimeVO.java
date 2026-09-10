/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

/** 安全 Redis 运行态摘要；不返回安全 Key 或值。 */
public record SecurityRuntimeVO(
        String status,
        Long onlineSessionCount,
        String sessionStatus,
        String verificationStatus,
        String loginFailureStatus,
        String nonceStatus,
        Long nonceCutoffEpochSecond,
        String refreshReplayStatus) {
}
