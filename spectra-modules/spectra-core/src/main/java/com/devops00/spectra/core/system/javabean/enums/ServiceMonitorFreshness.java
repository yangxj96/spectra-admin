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

package com.devops00.spectra.core.system.javabean.enums;

/**
 * 服务监控数据新鲜度。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum ServiceMonitorFreshness {

    /** 数据在当前采集窗口内。 */
    CURRENT,
    /** 数据存在短暂延迟。 */
    DELAYED,
    /** 数据已过期。 */
    STALE,
    /** 没有可用数据。 */
    UNAVAILABLE
}
