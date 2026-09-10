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

package com.devops00.spectra.core.security.secret.service.impl;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.secret.service.SecretManagementSettingsService;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 基于系统配置表和 CryptoKeyManager 的密钥运行策略服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
@Service
@RequiredArgsConstructor
public class SecretManagementSettingsServiceImpl implements SecretManagementSettingsService {

    private static final String CRYPTO_REMARKS = "密钥管理页面配置的接口加解密开关";

    private final ConfiguredService configuredService;
    private final CryptoKeyManager cryptoKeyManager;

    @Override
    public CryptoSettings getCryptoSettings() {
        boolean enabled = configuredService.findValue(SystemConfigKeys.CRYPTO_ENABLED)
                .map(value -> "true".equalsIgnoreCase(value))
                .orElse(false);
        return new CryptoSettings(enabled, cryptoKeyManager.isEnabled(), cryptoKeyManager.getState());
    }

    @Override
    @Transactional
    public CryptoSettings setCryptoEnabled(boolean enabled) {
        if (enabled && !cryptoKeyManager.isKeyMaterialReady()) {
            throw new DataSaveException("接口加解密密钥未就绪，无法启用");
        }

        configuredService.upsert(SystemConfigKeys.CRYPTO_ENABLED, Boolean.toString(enabled), ConfiguredValueType.BOOL,
                CRYPTO_REMARKS);
        refreshAfterCommit();
        return getCryptoSettings();
    }

    /** 仅在系统配置成功提交后切换运行态；无事务调用场景保留立即刷新能力。 */
    private void refreshAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cryptoKeyManager.refresh();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cryptoKeyManager.refresh();
            }
        });
    }
}
