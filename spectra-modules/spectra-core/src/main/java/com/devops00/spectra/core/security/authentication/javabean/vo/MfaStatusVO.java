/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.vo;

/**
 * 当前用户的 MFA 状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/26
 */
public record MfaStatusVO(boolean enabled, String factorType) {
}
