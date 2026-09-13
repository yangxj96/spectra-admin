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

package com.devops00.spectra.core.notification.security;

import com.devops00.spectra.common.security.crypto.symmetric.AESUtils;

import javax.crypto.SecretKey;

/**
 * 通知敏感载荷的 AES-GCM 适配器。
 *
 * <p>协议版本、密钥来源和载荷格式由通知 feature 管理；底层密码算法集中在 common
 * security crypto 中，避免各业务自行复制加密实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
public final class NotificationPayloadCipher {

    private NotificationPayloadCipher() {
    }

    /**
     * 使用通知密钥和随机 IV 加密载荷。
     *
     * @param plainText 待保护的通知载荷明文
     * @param key       通知配置解析出的 AES 密钥
     * @param iv        本次载荷唯一使用的 GCM IV
     * @return Base64 编码的 AES-GCM 密文；加密失败时抛出底层异常
     * @throws Exception JCE 初始化或加密失败时抛出
     */
    public static String encrypt(String plainText, SecretKey key, byte[] iv) throws Exception {
        return AESUtils.encrypt(plainText, key, iv);
    }

    /**
     * 解密通知敏感载荷。
     *
     * @param encryptedText Base64 编码的 AES-GCM 密文
     * @param key           通知配置解析出的 AES 密钥
     * @param iv            密文对应的 GCM IV
     * @return 解密后的 UTF-8 明文；密文、密钥或认证标签无效时抛出异常
     * @throws Exception JCE 解密或认证失败时抛出
     */
    public static String decrypt(String encryptedText, SecretKey key, byte[] iv) throws Exception {
        return AESUtils.decrypt(encryptedText, key, iv);
    }

    /**
     * 生成通知载荷使用的一次性 GCM IV。
     *
     * @return 12 字节随机 IV；每次调用都会生成新数组
     */
    public static byte[] generateIv() {
        return AESUtils.generateIv();
    }

    /**
     * 将协议中的 IV 转换为十六进制字符串。
     *
     * @param iv GCM IV 字节数组
     * @return 小写十六进制 IV 字符串；不返回 null
     */
    public static String getIvHex(byte[] iv) {
        return AESUtils.getIvHex(iv);
    }

    /**
     * 解析通知协议中的十六进制 IV。
     *
     * @param hex 小写或大写十六进制 IV
     * @return 解析后的 IV 字节数组；格式非法时抛出 {@link IllegalArgumentException}
     */
    public static byte[] hexToIv(String hex) {
        return AESUtils.hexToIv(hex);
    }
}
