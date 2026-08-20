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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.core.system.service.ConfiguredService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 基于 {@code spectra_core.sys_config} 的公共运行时配置提供者。
 */
@Component
@RequiredArgsConstructor
public class DatabaseSystemConfigValueProvider implements SystemConfigValueProvider {

    private final ConfiguredService configuredService;

    @Override
    public Optional<String> find(String key) {
        return configuredService.findValue(key);
    }
}
