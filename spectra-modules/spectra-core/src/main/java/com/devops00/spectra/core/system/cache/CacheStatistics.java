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

/**
 * 承载缓存相关的不可变数据。
 *
 * @param keyCount  缓存键总数
 * @param hitCount  缓存命中次数
 * @param missCount 缓存未命中次数
 * @param hitRate   缓存命中率
 * @param status    业务状态
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record CacheStatistics(
                              Long keyCount,
                              Long hitCount,
                              Long missCount,
                              Double hitRate,
                              String status) {

    /** 当前 Spring Cache 适配器未暴露精确指标时的统计结果。 */
    public static CacheStatistics unsupported() {
        return new CacheStatistics(null, null, null, null, "UNSUPPORTED");
    }
}
