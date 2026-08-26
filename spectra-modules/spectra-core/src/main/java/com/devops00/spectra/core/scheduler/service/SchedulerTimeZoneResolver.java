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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

/** 从系统配置解析调度使用的 IANA 时区。 */
@Service
public class SchedulerTimeZoneResolver {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private final ConfiguredService configuredService;

    public SchedulerTimeZoneResolver(ConfiguredService configuredService) {
        this.configuredService = configuredService;
    }

    /**
     * 读取系统默认时区；缺失或非法配置回退到 UTC。
     * <p>读取数据库失败会向上抛出，不能伪装成缺失配置。</p>
     */
    public ZoneId resolve() {
        return resolve(configuredService.findValue(SystemConfigKeys.SYSTEM_DEFAULT_TIMEZONE).orElse(null));
    }

    /** 按规则解析一项时区配置。 */
    public ZoneId resolve(String configuredValue) {
        if (configuredValue == null || configuredValue.isBlank()) {
            return UTC;
        }
        var candidate = configuredValue.trim();
        if (!ZoneId.getAvailableZoneIds().contains(candidate) && !"UTC".equals(candidate)) {
            return UTC;
        }
        return ZoneId.of(candidate);
    }
}
