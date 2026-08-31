/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 对业务模块公开的不可变文件资产快照。 */
public record FileAssetSnapshot(UUID fileAssetId,
                                String originalName,
                                long size,
                                String contentType,
                                String contentSha256,
                                String status,
                                String fileTypeCode) {
}
