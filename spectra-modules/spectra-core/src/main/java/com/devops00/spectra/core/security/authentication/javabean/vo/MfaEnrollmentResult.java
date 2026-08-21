/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.vo;

import java.util.UUID;

/** TOTP 登记响应；secret 仅用于生成二维码，确认后不再回传。 */
public record MfaEnrollmentResult(UUID enrollmentId, String provisioningUri, String secret) {
}
