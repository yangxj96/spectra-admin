/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 对业务模块返回的引用快照。 */
public record FileReferenceView(UUID referenceId,
                                UUID fileAssetId,
                                String referenceType,
                                UUID businessReferenceId,
                                String purpose,
                                String displayName) {
}
