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

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.port.security.RuntimeSecretProvider;
import com.devops00.spectra.common.security.crypto.asymmetric.RSAUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 加解密密钥管理器
 * <p>
 * 从统一密钥运行时端口读取 RSA 密钥配置，缓存到内存中供请求处理使用。
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

    private static final String CONFIG_SERVER_PUBLIC_KEY = "crypto.server.public-key";
    private static final String CONFIG_SERVER_PRIVATE_KEY = "crypto.server.private-key";
    private static final String CONFIG_CLIENT_PUBLIC_KEY = "crypto.client.public-key";
    private static final String CONFIG_CLIENT_PRIVATE_KEY = "crypto.client.private-key";

    private final RuntimeSecretProvider secretProvider;
    private final SystemConfigValueProvider configProvider;

    /**
     * 不可变密钥容器，volatile 原子替换保证线程安全
     */
    /** 加密配置运行态。 */
    public enum State {
        /** 已明确关闭接口加解密。 */
        DISABLED,
        /** 四个 RSA 密钥均已加载，可用于请求和响应加解密。 */
        READY,
        /** 已开启加解密但配置读取或密钥解析失败，必须拒绝加密路径。 */
        UNAVAILABLE
    }

    private record CryptoKeys(State state, @Nullable PublicKey serverPublicKey, @Nullable PrivateKey serverPrivateKey,
                              @Nullable PublicKey clientPublicKey, @Nullable PrivateKey clientPrivateKey,
                              @Nullable String serverPublicKeyBase64, @Nullable String clientPrivateKeyBase64) {

        /**
         * 检查密钥完整性（启用时四个密钥必须全部存在）
         */
        boolean isComplete() {
            return state == State.READY
                    && serverPublicKey != null
                    && serverPrivateKey != null
                    && clientPublicKey != null
                    && clientPrivateKey != null;
        }
    }

    /**
     * 当前密钥缓存（volatile 原子替换）
     */
    private volatile CryptoKeys keys = new CryptoKeys(State.DISABLED, null, null, null, null, null, null);

    public CryptoKeyManager(RuntimeSecretProvider secretProvider, SystemConfigValueProvider configProvider) {
        this.secretProvider = secretProvider;
        this.configProvider = configProvider;
    }

    /**
     * 处理内部业务逻辑（{@code init}）。
     */
    @PostConstruct
    public void init() {
        log.info(LogPrefix.WEB.f("初始化密钥管理器，从统一密钥服务加载配置"));
        refresh();
    }

    /**
     * 从统一密钥服务重新加载密钥到内存
     */
    public synchronized void refresh() {
        try {
            // 未显式配置时保持关闭，允许系统在尚未创建统一密钥版本时完成首次管理；
            // 只有显式配置为 true 后，密钥缺失才进入 UNAVAILABLE 并按安全策略拒绝请求。
            String enabledValue = configProvider.find("crypto.enabled").orElse("false");
            if ("false".equalsIgnoreCase(enabledValue)) {
                this.keys = new CryptoKeys(State.DISABLED, null, null, null, null, null, null);
                log.info(LogPrefix.WEB.f("接口加解密已关闭"));
                return;
            }
            if (!"true".equalsIgnoreCase(enabledValue)) {
                throw new CryptoConfigurationException("接口加解密开关配置值无效");
            }
            this.keys = loadKeyMaterial();

            if (keys.isComplete()) {
                log.info(LogPrefix.WEB.f("密钥加载完成，加解密已就绪"));
            } else {
                log.warn(LogPrefix.WEB.f("密钥不完整，加解密状态为 UNAVAILABLE"));
            }
        } catch (Exception e) {
            log.error(LogPrefix.WEB.f("密钥加载失败，加解密状态为 UNAVAILABLE"), e);
            this.keys = new CryptoKeys(State.UNAVAILABLE, null, null, null, null, null, null);
        }
    }

    /**
     * 检查统一密钥服务中的四个 RSA 密钥是否已就绪。
     *
     * <p>该方法只读取并解析密钥材料，不修改当前运行态快照，供启用开关的事务提交前校验使用。
     * 这样即使后续事务回滚，也不会提前切换正在提供服务的密钥。</p>
     *
     * @return 四个密钥均可读取和解析时返回 true，否则返回 false
     */
    public boolean isKeyMaterialReady() {
        try {
            return loadKeyMaterial().isComplete();
        } catch (Exception e) {
            log.warn(LogPrefix.WEB.f("接口加解密密钥材料校验失败"), e);
            return false;
        }
    }

    /**
     * 是否启用接口加解密（需 enabled=true 且密钥完整）
     *
     * @return 返回当前四个 RSA 密钥是否完整可用；只有开关开启且服务端/客户端公私钥均解析成功时返回 true，否则返回 false。
     */
    public boolean isEnabled() {
        return keys.isComplete();
    }

    /**
     * 判断加密配置是否已开启，包括密钥暂不可用的状态。
     *
     * <p>Advice 使用该状态区分“明确关闭”与“配置故障”，避免在密钥故障时静默返回明文。</p>
     *
     * @return 返回是否存在接口加解密配置；配置读取失败或密钥不完整时仍返回 true，具体可用性由 {@link #isEnabled()} 表示。
     */
    public boolean isConfiguredEnabled() {
        return keys.state() != State.DISABLED;
    }

    /**
     * 获取当前加密状态。
     *
     * @return 返回当前密钥运行态：{@code DISABLED} 表示明确关闭，{@code READY} 表示可用，{@code UNAVAILABLE} 表示开启但配置故障；状态值始终非 null。
     */
    public State getState() {
        return keys.state();
    }

    /**
     * 要求加密密钥已就绪。
     *
     * @throws EncryptException 密钥未就绪
     */
    public void requireReady() {
        if (!keys.isComplete()) {
            throw new EncryptException("加密密钥不可用");
        }
    }

    /**
     * 获取服务端公钥。
     *
     * @return 返回当前已加载的服务端 RSA 公钥；密钥状态不是 READY 时返回 null。
     */
    public @Nullable PublicKey getServerPublicKey() {
        return keys.isComplete() ? keys.serverPublicKey() : null;
    }

    /**
     * 获取服务端私钥。
     *
     * @return 返回当前已加载的服务端 RSA 私钥；密钥状态不是 READY 时返回 null。
     */
    public @Nullable PrivateKey getServerPrivateKey() {
        return keys.isComplete() ? keys.serverPrivateKey() : null;
    }

    /**
     * 获取客户端公钥。
     *
     * @return 返回当前已加载的客户端 RSA 公钥；密钥状态不是 READY 时返回 null。
     */
    public @Nullable PublicKey getClientPublicKey() {
        return keys.isComplete() ? keys.clientPublicKey() : null;
    }

    /**
     * 获取客户端私钥。
     *
     * @return 返回当前已加载的客户端 RSA 私钥；密钥状态不是 READY 时返回 null。
     */
    public @Nullable PrivateKey getClientPrivateKey() {
        return keys.isComplete() ? keys.clientPrivateKey() : null;
    }

    /**
     * 获取服务端公钥 Base64 字符串。
     *
     * @return 返回当前已加载的服务端公钥原始 Base64 配置；密钥状态不是 READY 时返回 null。
     */
    public @Nullable String getServerPublicKeyBase64() {
        return keys.isComplete() ? keys.serverPublicKeyBase64() : null;
    }

    /**
     * 获取客户端私钥 Base64 字符串。
     *
     * @return 返回当前已加载的客户端私钥原始 Base64 配置；密钥状态不是 READY 时返回 null。
     */
    public @Nullable String getClientPrivateKeyBase64() {
        return keys.isComplete() ? keys.clientPrivateKeyBase64() : null;
    }

    /** 从统一密钥服务读取单个密钥值；读取失败时返回 null 并由状态机 fail-closed。 */
    private String getSecret(String key) {
        try {
            return secretProvider.findActive(key).map(secret -> secret.value()).orElse(null);
        } catch (Exception e) {
            throw new CryptoConfigurationException("读取加密配置失败", e);
        }
    }

    /** 读取并解析四个 RSA 密钥，不改变当前缓存。 */
    private CryptoKeys loadKeyMaterial() throws Exception {
        String serverPubBase64 = getSecret(CONFIG_SERVER_PUBLIC_KEY);
        String serverPriBase64 = getSecret(CONFIG_SERVER_PRIVATE_KEY);
        String clientPubBase64 = getSecret(CONFIG_CLIENT_PUBLIC_KEY);
        String clientPriBase64 = getSecret(CONFIG_CLIENT_PRIVATE_KEY);
        if (serverPubBase64 == null
                && serverPriBase64 == null
                && clientPubBase64 == null
                && clientPriBase64 == null) {
            log.warn(LogPrefix.WEB.f("接口加解密已开启但统一密钥服务中没有密钥"));
            return new CryptoKeys(State.UNAVAILABLE, null, null, null, null, null, null);
        }

        PublicKey serverPub = serverPubBase64 != null ? RSAUtils.restorePublicKey(serverPubBase64) : null;
        PrivateKey serverPri = serverPriBase64 != null ? RSAUtils.restorePrivateKey(serverPriBase64) : null;
        PublicKey clientPub = clientPubBase64 != null ? RSAUtils.restorePublicKey(clientPubBase64) : null;
        PrivateKey clientPri = clientPriBase64 != null ? RSAUtils.restorePrivateKey(clientPriBase64) : null;

        State state = serverPub != null && serverPri != null && clientPub != null && clientPri != null
                ? State.READY
                : State.UNAVAILABLE;
        return new CryptoKeys(state, serverPub, serverPri, clientPub, clientPri,
                serverPubBase64, clientPriBase64);
    }
}
