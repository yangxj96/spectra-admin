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

package com.devops00.spectra.core.system.cache;

import java.util.UUID;

/**
 * 承载缓存结果相关的不可变数据。
 *
 * @param operationId       操作标识
 * @param status            业务状态
 * @param affectedRegions   本次操作影响的缓存区域编码集合
 * @param broadcastAccepted 缓存清理广播是否已被接受
 * @param message           处理结果或异常原因的说明
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheManagementResult(
                                    UUID operationId,
                                    String status,
                                    long affectedRegions,
                                    boolean broadcastAccepted,
                                    String message) {
}
