/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 清理登录失败锁定计数的请求。 */
public record SecurityLoginFailureClearFrom(String username, String reason, boolean confirmed) {
}
