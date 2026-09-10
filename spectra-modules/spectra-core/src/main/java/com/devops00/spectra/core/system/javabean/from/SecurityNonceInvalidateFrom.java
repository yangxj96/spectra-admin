/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 定向失效 Web 加密 nonce 的请求；nonce 仅在后端内存中摘要。 */
public record SecurityNonceInvalidateFrom(String nonce, String reason, boolean confirmed) {
}
