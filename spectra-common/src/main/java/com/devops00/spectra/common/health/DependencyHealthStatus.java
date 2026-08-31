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

package com.devops00.spectra.common.health;

import java.util.Locale;

/**
 * 系统依赖和模块能力统一健康状态。
 *
 * <p>所有健康探针、Actuator 适配器和 Core 聚合器必须使用此状态，不再定义同义枚举。UNKNOWN
 * 不得被解释为 UP；它表示尚未检查或无法可靠判断。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public enum DependencyHealthStatus {

    /** 依赖可用且检查完整。 */
    UP,

    /** 依赖可访问，但存在部分能力不可用、延迟过高或检查结果过期。 */
    DEGRADED,

    /** 已确认依赖不可用，或检查超时/关键检查失败。 */
    DOWN,

    /** 未检查或无法可靠判断依赖状态。 */
    UNKNOWN;

    /**
     * 将传统的 Actuator 状态文本映射为统一状态。
     *
     * @param value Actuator 状态文本
     * @return 统一健康状态
     */
    public static DependencyHealthStatus fromActuator(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "UP" -> UP;
            case "DOWN", "OUT_OF_SERVICE" -> DOWN;
            case "UNKNOWN" -> UNKNOWN;
            default -> DEGRADED;
        };
    }

    /**
     * 将简单的可用性判断转换为统一状态。
     *
     * @param available 是否可用
     * @return UP 或 DOWN
     */
    public static DependencyHealthStatus fromAvailability(boolean available) {
        return available ? UP : DOWN;
    }
}
