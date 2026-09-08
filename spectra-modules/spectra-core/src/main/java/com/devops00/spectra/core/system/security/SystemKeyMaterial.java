/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.system.security;

import com.devops00.spectra.common.security.crypto.asymmetric.RSAUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 系统加解密配置的 RSA 密钥材料适配器。
 *
 * <p>系统配置流程只负责生成和持久化密钥材料，不直接依赖 RSA 算法实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public final class SystemKeyMaterial {

    private SystemKeyMaterial() {
    }

    /**
     * 生成系统接口加解密使用的 2048 位 RSA 密钥对。
     *
     * @return 新生成的 RSA 公私钥对；JCE 不支持算法时抛出异常
     * @throws Exception 密钥生成失败时抛出
     */
    public static KeyPair generateKeyPair() throws Exception {
        return RSAUtils.generateKeyPair();
    }

    /**
     * 将系统公钥编码为配置可保存的 Base64 文本。
     *
     * @param publicKey 系统 RSA 公钥
     * @return 不含换行的 Base64 公钥文本
     */
    public static String publicKeyBase64(PublicKey publicKey) {
        return RSAUtils.getPublicKeyBase64(publicKey);
    }

    /**
     * 将系统私钥编码为配置可保存的 Base64 文本。
     *
     * @param privateKey 系统 RSA 私钥
     * @return 不含换行的 Base64 私钥文本
     */
    public static String privateKeyBase64(PrivateKey privateKey) {
        return RSAUtils.getPrivateKeyBase64(privateKey);
    }
}
