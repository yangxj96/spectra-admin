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

package com.devops00.spectra.framework.web.crypto;

import com.devops00.spectra.framework.health.readiness.ReadinessVerifier;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 依赖恢复后的接口加解密密钥一致性校验。
 *
 * <p>数据库或安全 Redis 故障恢复后，如果统一密钥服务启用了接口加解密但密钥尚未就绪，则拒绝将服务重新置为就绪，
 * 避免在加密路径不可用时对外服务。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Component
@NullMarked
public class CryptoKeyConsistencyVerifier implements ReadinessVerifier {

    private final ObjectProvider<CryptoKeyManager> keyManagerProvider;

    public CryptoKeyConsistencyVerifier(ObjectProvider<CryptoKeyManager> keyManagerProvider) {
        this.keyManagerProvider = keyManagerProvider;
    }

    @Override
    public void verifyRecovery() {
        CryptoKeyManager keyManager = keyManagerProvider.getIfAvailable();
        if (keyManager != null && keyManager.isConfiguredEnabled() && !keyManager.isEnabled()) {
            throw new IllegalStateException("接口加解密已启用但密钥未就绪");
        }
    }
}
