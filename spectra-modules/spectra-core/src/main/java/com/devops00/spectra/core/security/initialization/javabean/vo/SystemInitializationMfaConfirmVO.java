/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.vo;

import java.util.List;

/** MFA 首次确认返回的 Recovery Code。仅返回一次。 */
public record SystemInitializationMfaConfirmVO(List<String> recoveryCodes) {
}
