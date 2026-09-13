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
 * 定义文件引用相关的跨模块调用契约。
 *
 * @param fileAssetId   文件资产标识
 * @param referenceType 关联业务对象的类型
 * @param referenceId   关联业务对象的标识
 * @param purpose       文件资产关联的业务用途
 * @param displayName   文件资产的展示名称
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record FileReferenceCommand(UUID fileAssetId,
                                   String referenceType,
                                   UUID referenceId,
                                   String purpose,
                                   String displayName) {
}
