/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 业务引用的稳定键。 */
public record FileReferenceKey(String referenceType, UUID referenceId, String purpose) {
}
