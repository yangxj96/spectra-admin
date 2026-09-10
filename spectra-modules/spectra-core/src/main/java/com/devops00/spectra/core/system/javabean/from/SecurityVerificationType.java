/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

/** 后端允许维护的验证码类型。 */
public enum SecurityVerificationType {
    KAPTCHA,
    LOGIN_SMS,
    LOGIN_EMAIL,
    BIND_PHONE,
    BIND_EMAIL
}
