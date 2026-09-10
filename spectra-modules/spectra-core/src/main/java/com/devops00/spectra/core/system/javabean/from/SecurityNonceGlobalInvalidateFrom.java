/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 全局失效当前 Web 加密 nonce 窗口的请求。 */
public record SecurityNonceGlobalInvalidateFrom(String reason, String confirmationPhrase) {
}
