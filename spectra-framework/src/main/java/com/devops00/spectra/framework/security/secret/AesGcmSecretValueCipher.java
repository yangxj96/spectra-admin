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

package com.devops00.spectra.framework.security.secret;

import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.port.security.SecretValueCipher;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 使用单一部署根密钥保护数据库 Secret 的 AES-256-GCM 实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
@Component
public class AesGcmSecretValueCipher implements SecretValueCipher {

    /** 当前数据库密文算法版本。 */
    public static final String ALGORITHM = "AES-256-GCM";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final String encodedRootKey;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 构造根密钥加密器。
     *
     * @param properties 根密钥配置
     */
    public AesGcmSecretValueCipher(SecretMasterKeyProperties properties) {
        this.encodedRootKey = properties == null ? null : properties.getMasterKey();
    }

    @Override
    public EncryptedValue encrypt(String code, String plaintext) {
        validateCode(code);
        if (plaintext == null) {
            throw new EncryptException("密钥明文不能为空");
        }
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, nonce, code);
            return new EncryptedValue(ALGORITHM, nonce, cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new EncryptException("密钥值加密失败", exception);
        }
    }

    @Override
    public String decrypt(String code, EncryptedValue encrypted) {
        validateCode(code);
        if (encrypted == null || !ALGORITHM.equals(encrypted.algorithm())) {
            throw new EncryptException("密钥值加密算法不受支持");
        }
        byte[] nonce = encrypted.nonce();
        if (nonce.length != NONCE_LENGTH_BYTES) {
            throw new EncryptException("密钥值 nonce 长度无效");
        }
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE, nonce, code);
            return new String(cipher.doFinal(encrypted.ciphertext()), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new EncryptException("密钥值解密失败", exception);
        }
    }

    private Cipher createCipher(int mode, byte[] nonce, String code) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, rootKey(), new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
        cipher.updateAAD(code.getBytes(StandardCharsets.UTF_8));
        return cipher;
    }

    private SecretKeySpec rootKey() {
        if (encodedRootKey == null || encodedRootKey.isBlank()) {
            throw new EncryptException("密钥管理根密钥未配置");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedRootKey.trim());
            if (decoded.length != KEY_LENGTH_BYTES) {
                throw new EncryptException("密钥管理根密钥长度必须为 32 字节");
            }
            return new SecretKeySpec(decoded, "AES");
        } catch (IllegalArgumentException exception) {
            throw new EncryptException("密钥管理根密钥格式无效", exception);
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new EncryptException("密钥编码不能为空");
        }
    }
}
