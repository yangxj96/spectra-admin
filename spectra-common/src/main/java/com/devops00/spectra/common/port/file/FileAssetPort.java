/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/**
 * 文件资产跨模块端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface FileAssetPort {

    /**
     * 校验就绪状态。
     *
     * @param fileAssetId 文件资产标识。
     * @return 符合条件的数据集合。
     */
    FileAssetSnapshot requireReady(UUID fileAssetId);

    /**
     * 校验就绪状态引用。
     *
     * @param fileAssetId 文件资产标识。
     * @param operatorId  数据记录的唯一标识。
     * @return 符合条件的数据集合。
     */
    FileAssetSnapshot requireReadyForReference(UUID fileAssetId, UUID operatorId);

    /**
     * 打开文件资产。
     *
     * @param fileAssetId 文件资产标识。
     * @param context     上下文参数。
     * @return 文件数据。
     */
    FileDownload open(UUID fileAssetId, FileAccessContext context);
}
