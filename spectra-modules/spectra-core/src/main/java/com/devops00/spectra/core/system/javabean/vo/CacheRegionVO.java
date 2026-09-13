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

import com.devops00.spectra.core.system.cache.CacheStatistics;

/**
 * 封装缓存区域相关的响应数据。
 *
 * @param code          业务对象的唯一编码
 * @param displayName   缓存区域的展示名称
 * @param provider      负责访问该缓存区域的提供器
 * @param mode          缓存区域使用的工作模式
 * @param ttlSeconds    缓存条目的有效时长（秒）
 * @param supportsStats 是否支持
 * @param supportsClear 是否支持清理
 * @param statistics    缓存区域的运行统计信息
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheRegionVO(
                            String code,
                            String displayName,
                            String provider,
                            String mode,
                            Long ttlSeconds,
                            boolean supportsStats,
                            boolean supportsClear,
                            CacheStatistics statistics) {
}
