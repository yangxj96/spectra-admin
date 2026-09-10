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

package com.devops00.spectra.common.port.security;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数据库存储密钥值的加密端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
public interface SecretValueCipher {

    /**
     * 加密密钥值。
     *
     * @param code      已注册密钥编码，作为附加认证数据
     * @param plaintext 待加密明文
     * @return 加密结果
     */
    EncryptedValue encrypt(String code, String plaintext);

    /**
     * 解密密钥值并校验密钥编码绑定关系。
     *
     * @param code      已注册密钥编码
     * @param encrypted 加密结果
     * @return 解密后的明文
     */
    String decrypt(String code, EncryptedValue encrypted);

    /**
     * AES-GCM 加密结果。
     *
     * @param algorithm  算法标识
     * @param nonce      GCM nonce
     * @param ciphertext 认证密文
     */
    record EncryptedValue(String algorithm, byte[] nonce, byte[] ciphertext) {

        /**
         * 防止调用方修改内部数组。
         */
        public EncryptedValue {
            Objects.requireNonNull(algorithm, "加密算法不能为空");
            Objects.requireNonNull(nonce, "加密 nonce 不能为空");
            Objects.requireNonNull(ciphertext, "加密密文不能为空");
            nonce = nonce.clone();
            ciphertext = ciphertext.clone();
        }

        @Override
        public byte[] nonce() {
            return nonce.clone();
        }

        @Override
        public byte[] ciphertext() {
            return ciphertext.clone();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof EncryptedValue that))
                return false;
            return Objects.equals(algorithm, that.algorithm)
                    && Arrays.equals(nonce, that.nonce)
                    && Arrays.equals(ciphertext, that.ciphertext);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(algorithm);
            result = 31 * result + Arrays.hashCode(nonce);
            return 31 * result + Arrays.hashCode(ciphertext);
        }
    }
}
