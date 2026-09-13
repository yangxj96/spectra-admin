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

package com.devops00.spectra.core.system.javabean.from;

import java.util.List;
import java.util.UUID;

/**
 * 承载缓存业务清理相关的请求参数。
 *
 * @param regionCodes  区域编码
 * @param allRegions   全部区域
 * @param allInstances 是否清理该缓存区域的全部实例
 * @param operationId  操作标识
 * @param reason       本次操作或审计事件对应的原因
 * @param confirmation 确认执行清理操作的输入内容
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheBusinessClearFrom(
                                     List<String> regionCodes,
                                     boolean allRegions,
                                     boolean allInstances,
                                     UUID operationId,
                                     String reason,
                                     String confirmation) {
}
