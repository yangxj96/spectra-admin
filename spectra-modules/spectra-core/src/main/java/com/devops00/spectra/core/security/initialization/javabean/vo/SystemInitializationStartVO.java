/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.vo;

import java.util.UUID;

/** 初始化 MFA 登记信息。secret 仅用于首次配置，不会持久化明文。 */
public record SystemInitializationStartVO(UUID initializationId, UUID enrollmentId,
                                          String provisioningUri, String secret, long expiresAt) {
}
