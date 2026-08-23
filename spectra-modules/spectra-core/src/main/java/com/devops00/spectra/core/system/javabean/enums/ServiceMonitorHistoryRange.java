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

import com.devops00.spectra.common.exception.DataException;

import java.time.Duration;

/**
 * 服务监控历史查询时间范围。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/23
 */
public enum ServiceMonitorHistoryRange {

    /** 最近 30 分钟。 */
    THIRTY_MINUTES("30m", Duration.ofMinutes(30), 180),

    /** 最近 6 小时。 */
    SIX_HOURS("6h", Duration.ofHours(6), 360),

    /** 最近 24 小时。 */
    TWENTY_FOUR_HOURS("24h", Duration.ofHours(24), 480);

    private final String code;
    private final Duration duration;
    private final int maxPoints;

    ServiceMonitorHistoryRange(String code, Duration duration, int maxPoints) {
        this.code = code;
        this.duration = duration;
        this.maxPoints = maxPoints;
    }

    public String getCode() {
        return code;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * 解析前端时间范围，不接受未定义范围，避免绕过查询点数限制。
     *
     * @param code 时间范围编码
     * @return 时间范围
     */
    public static ServiceMonitorHistoryRange fromCode(String code) {
        if (code == null || code.isBlank()) {
            return THIRTY_MINUTES;
        }
        for (var range : values()) {
            if (range.code.equalsIgnoreCase(code.trim())) {
                return range;
            }
        }
        throw new DataException("服务监控历史时间范围无效");
    }
}
