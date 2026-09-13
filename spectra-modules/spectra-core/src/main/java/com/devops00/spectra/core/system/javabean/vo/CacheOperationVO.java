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

package com.devops00.spectra.core.system.javabean.vo;

import java.time.Instant;
import java.util.UUID;

/**
 * 封装缓存操作相关的响应数据。
 *
 * @param operationId       操作标识
 * @param operationType     操作类型
 * @param status            业务状态
 * @param affectedCount     本次操作影响的对象数量
 * @param broadcastAccepted 缓存清理广播是否已被接受
 * @param message           处理结果或异常原因的说明
 * @param completedAt       操作完成时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheOperationVO(
                               UUID operationId,
                               String operationType,
                               String status,
                               long affectedCount,
                               boolean broadcastAccepted,
                               String message,
                               Instant completedAt) {
}
