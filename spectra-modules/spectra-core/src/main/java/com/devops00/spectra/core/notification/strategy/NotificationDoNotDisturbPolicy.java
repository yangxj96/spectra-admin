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

package com.devops00.spectra.core.notification.strategy;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

/**
 * 用户免打扰时间窗口规则。
 *
 * <p>数据库使用 {@link Instant} 保存边界，但边界的日期不参与每日窗口判断，
 * 只按用户时区提取本地时间。因此开始时间晚于结束时间时表示跨午夜窗口。
 * 缺少任一边界时按整个时间段免打扰处理，避免不完整配置意外发送通知。</p>
 */
public final class NotificationDoNotDisturbPolicy {

    /** 默认时区，保证没有用户时区时行为稳定且可复现。 */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private NotificationDoNotDisturbPolicy() {
    }

    /**
     * 判断指定时刻是否处于用户免打扰窗口。
     *
     * @param enabled  是否启用免打扰
     * @param now      当前时刻
     * @param start    窗口开始边界
     * @param end      窗口结束边界
     * @param userZone 用户时区；为空时使用 UTC
     * @return 是否应阻止可选通知投递
     */
    public static boolean isQuiet(boolean enabled, Instant now, Instant start, Instant end, ZoneId userZone) {
        if (!enabled) {
            return false;
        }
        if (start == null || end == null) {
            return true;
        }
        var zone = userZone == null ? DEFAULT_ZONE : userZone;
        var current = now.atZone(zone).toLocalTime();
        var windowStart = start.atZone(zone).toLocalTime();
        var windowEnd = end.atZone(zone).toLocalTime();
        if (windowStart.equals(windowEnd)) {
            return true;
        }
        if (windowStart.isAfter(windowEnd)) {
            return !current.isBefore(windowStart) || current.isBefore(windowEnd);
        }
        return !current.isBefore(windowStart) && current.isBefore(windowEnd);
    }

    /**
     * 将收件人目录提供的时区文本解析为时区；非法值回退到 UTC。
     *
     * @param timezone 时区 ID
     * @return 可用时区
     */
    public static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            return DEFAULT_ZONE;
        }
    }
}
