/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 按用户撤销全部 Session/Token 的请求。 */
public record SecuritySessionRevokeAllFrom(java.util.UUID userId, String reason, boolean confirmed) {
}
