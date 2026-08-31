/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 文件资产跨模块端口。 */
public interface FileAssetPort {

    FileAssetSnapshot requireReady(UUID fileAssetId);

    FileAssetSnapshot requireReadyForReference(UUID fileAssetId, UUID operatorId);

    FileDownload open(UUID fileAssetId, FileAccessContext context);
}
