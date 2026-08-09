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

package com.devops00.spectra.framework.configure.mvc.crypto;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.RSAUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * 加解密密钥管理器
 *
 * 从 sys_config 表读取 RSA 密钥配置，缓存到内存中供请求处理使用。
 * 密钥变更后调用 refresh() 重新加载，无需重启服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/13
 */
@Slf4j
@Component
@NullMarked
public class CryptoKeyManager {

    private static final String CONFIG_ENABLED = "crypto.enabled";
    private static final String CONFIG_SERVER_PUBLIC_KEY = "crypto.server.public-key";
    private static final String CONFIG_SERVER_PRIVATE_KEY = "crypto.server.private-key";
    private static final String CONFIG_CLIENT_PUBLIC_KEY = "crypto.client.public-key";
    private static final String CONFIG_CLIENT_PRIVATE_KEY = "crypto.client.private-key";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 不可变密钥容器，volatile 原子替换保证线程安全
     */
    private record CryptoKeys(boolean enabled, @Nullable PublicKey serverPublicKey, @Nullable PrivateKey serverPrivateKey,
            @Nullable PublicKey clientPublicKey, @Nullable PrivateKey clientPrivateKey) {

        /**
         * 检查密钥完整性（启用时四个密钥必须全部存在）
         */
        boolean isComplete() {
            return enabled && serverPublicKey != null && serverPrivateKey != null && clientPublicKey != null && clientPrivateKey != null;
        }
    }

    /**
     * 当前密钥缓存（volatile 原子替换）
     */
    private volatile CryptoKeys keys = new CryptoKeys(false, null, null, null, null);

    public CryptoKeyManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        log.info(LogPrefix.WEB.f("初始化密钥管理器，从 sys_config 加载配置"));
        refresh();
    }

    /**
     * 从 sys_config 重新加载密钥到内存
     */
    public synchronized void refresh() {
        try {
            boolean enabled = Boolean.parseBoolean(getConfigValue(CONFIG_ENABLED).orElse("false"));

            if (!enabled) {
                this.keys = new CryptoKeys(false, null, null, null, null);
                log.info(LogPrefix.WEB.f("加解密已关闭 (crypto.enabled=false)"));
                return;
            }

            String serverPubBase64 = getConfigValue(CONFIG_SERVER_PUBLIC_KEY).orElse(null);
            String serverPriBase64 = getConfigValue(CONFIG_SERVER_PRIVATE_KEY).orElse(null);
            String clientPubBase64 = getConfigValue(CONFIG_CLIENT_PUBLIC_KEY).orElse(null);
            String clientPriBase64 = getConfigValue(CONFIG_CLIENT_PRIVATE_KEY).orElse(null);

            PublicKey serverPub = serverPubBase64 != null ? RSAUtils.restorePublicKey(serverPubBase64) : null;
            PrivateKey serverPri = serverPriBase64 != null ? RSAUtils.restorePrivateKey(serverPriBase64) : null;
            PublicKey clientPub = clientPubBase64 != null ? RSAUtils.restorePublicKey(clientPubBase64) : null;
            PrivateKey clientPri = clientPriBase64 != null ? RSAUtils.restorePrivateKey(clientPriBase64) : null;

            this.keys = new CryptoKeys(true, serverPub, serverPri, clientPub, clientPri);

            if (keys.isComplete()) {
                log.info(LogPrefix.WEB.f("密钥加载完成，加解密已就绪"));
            } else {
                log.warn(LogPrefix.WEB.f("密钥不完整，加解密将跳过（缺少密钥配置）"));
            }
        } catch (Exception e) {
            log.error(LogPrefix.WEB.f("密钥加载失败: {}"), e.getMessage(), e);
            this.keys = new CryptoKeys(false, null, null, null, null);
        }
    }

    /**
     * 是否启用接口加解密（需 enabled=true 且密钥完整）
     */
    public boolean isEnabled() {
        return keys.isComplete();
    }

    /**
     * 获取服务端公钥
     */
    public @Nullable PublicKey getServerPublicKey() {
        return keys.serverPublicKey();
    }

    /**
     * 获取服务端私钥
     */
    public @Nullable PrivateKey getServerPrivateKey() {
        return keys.serverPrivateKey();
    }

    /**
     * 获取客户端公钥
     */
    public @Nullable PublicKey getClientPublicKey() {
        return keys.clientPublicKey();
    }

    /**
     * 获取客户端私钥
     */
    public @Nullable PrivateKey getClientPrivateKey() {
        return keys.clientPrivateKey();
    }

    /**
     * 获取服务端公钥 Base64 字符串
     */
    public @Nullable String getServerPublicKeyBase64() {
        return getConfigValue(CONFIG_SERVER_PUBLIC_KEY).orElse(null);
    }

    /**
     * 获取客户端私钥 Base64 字符串
     */
    public @Nullable String getClientPrivateKeyBase64() {
        return getConfigValue(CONFIG_CLIENT_PRIVATE_KEY).orElse(null);
    }

    /**
     * 从 sys_config 读取单个配置值
     */
    private java.util.Optional<String> getConfigValue(String key) {
        try {
            List<String> results = jdbcTemplate.queryForList("SELECT value FROM spectra_core.sys_config WHERE key = ? AND deleted IS NULL",
                    String.class, key);
            return results.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(results.getFirst());
        } catch (Exception e) {
            log.warn(LogPrefix.WEB.f("读取配置失败: {}"), key, e);
            return java.util.Optional.empty();
        }
    }
}
