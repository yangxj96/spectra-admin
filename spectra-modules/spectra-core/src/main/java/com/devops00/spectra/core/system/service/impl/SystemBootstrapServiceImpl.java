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

import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.javabean.vo.CryptoConfigVO;
import com.devops00.spectra.core.system.javabean.vo.SystemBootstrapVO;
import com.devops00.spectra.core.system.javabean.vo.SystemPublicConfigVO;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.system.service.SystemBootstrapService;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

/** Web 端启动配置聚合服务默认实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemBootstrapServiceImpl implements SystemBootstrapService {

    private static final String DEFAULT_SYSTEM_NAME = "Spectra";
    private static final String DEFAULT_COPYRIGHT_NAME = "devops00";
    private static final String DEFAULT_COPYRIGHT_URL = "https://www.devops00.com";
    private static final String DEFAULT_LOCALE = "zh-CN";
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    private final ConfiguredService configuredService;
    private final CryptoKeyManager cryptoKeyManager;
    private final SystemInitializationService initializationService;

    @Override
    public SystemBootstrapVO get() {
        String systemName = value(SystemConfigKeys.SYSTEM_NAME, DEFAULT_SYSTEM_NAME);
        String systemShortName = value(SystemConfigKeys.SYSTEM_SHORT_NAME, systemName);
        SystemPublicConfigVO system = new SystemPublicConfigVO(
                systemName,
                systemShortName,
                value(SystemConfigKeys.SYSTEM_LOGO, ""),
                value(SystemConfigKeys.SYSTEM_DEFAULT_LOCALE, DEFAULT_LOCALE),
                value(SystemConfigKeys.SYSTEM_DEFAULT_TIMEZONE, DEFAULT_TIMEZONE),
                booleanValue(SystemConfigKeys.COPYRIGHT_ENABLED, true),
                value(SystemConfigKeys.COPYRIGHT_NAME, DEFAULT_COPYRIGHT_NAME),
                copyrightUrl());
        CryptoConfigVO crypto = new CryptoConfigVO(
                cryptoKeyManager.isEnabled(), cryptoKeyManager.getServerPublicKeyBase64());
        return new SystemBootstrapVO(system, crypto, initializationService.status());
    }

    private String value(String key, String fallback) {
        return configuredService.findValue(key).orElse(fallback);
    }

    private boolean booleanValue(String key, boolean fallback) {
        return configuredService.findValue(key).map(Boolean::parseBoolean).orElse(fallback);
    }

    private String copyrightUrl() {
        String value = value(SystemConfigKeys.COPYRIGHT_URL, DEFAULT_COPYRIGHT_URL);
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank() ? value : "";
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }
}
