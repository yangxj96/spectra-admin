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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.core.system.javabean.vo.CryptoClientKeyVO;
import com.devops00.spectra.core.system.javabean.vo.CryptoConfigVO;
import com.devops00.spectra.core.system.javabean.vo.CryptoKeyPairVO;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;

/// 加解密密钥管理接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
@RestController
@RequestMapping("/system/crypto")
@RequiredArgsConstructor
public class CryptoController {

    /// CryptoKey管理服务
    private final CryptoKeyManager cryptoKeyManager;

    /// 系统配置服务
    private final ConfiguredService configuredService;

    /// 获取加解密配置（前端初始化调用）
    @ULog("'获取加解密配置'")
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/config", version = "1.0.0+")
    public CryptoConfigVO getConfig() {
        return new CryptoConfigVO(
                cryptoKeyManager.isEnabled(),
                cryptoKeyManager.getServerPublicKeyBase64());
    }

    /// 获取客户端私钥（需登录）
    @ULog("'获取客户端私钥'")
    @Encrypt(response = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/keypair/client-private", version = "1.0.0+")
    public CryptoClientKeyVO getClientPrivateKey() {
        return new CryptoClientKeyVO(cryptoKeyManager.getClientPrivateKeyBase64());
    }

    /// 生成新的 RSA 密钥对
    @ULog("'生成RSA密钥对'")
    @PostMapping(value = "/keypair/generate", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public CryptoKeyPairVO generateKeyPair() {
        try {
            KeyPair serverPair = RSAUtils.generateKeyPair();
            KeyPair clientPair = RSAUtils.generateKeyPair();

            String serverPublicKey = RSAUtils.getPublicKeyBase64(serverPair.getPublic());
            String serverPrivateKey = RSAUtils.getPrivateKeyBase64(serverPair.getPrivate());
            String clientPublicKey = RSAUtils.getPublicKeyBase64(clientPair.getPublic());
            String clientPrivateKey = RSAUtils.getPrivateKeyBase64(clientPair.getPrivate());

            String remarks = "RSA密钥对自动生成";
            configuredService.upsert("crypto.server.public-key", serverPublicKey, remarks);
            configuredService.upsert("crypto.server.private-key", serverPrivateKey, remarks);
            configuredService.upsert("crypto.client.public-key", clientPublicKey, remarks);
            configuredService.upsert("crypto.client.private-key", clientPrivateKey, remarks);
            configuredService.upsert("crypto.enabled", "true", remarks);

            cryptoKeyManager.refresh();

            log.info("已生成并保存新的 RSA 密钥对（2048位 × 2）");
            return new CryptoKeyPairVO(serverPublicKey, serverPrivateKey, clientPublicKey, clientPrivateKey);
        } catch (Exception e) {
            log.error("生成RSA密钥对失败: {}", e.getMessage(), e);
            throw new RuntimeException("密钥生成失败: " + e.getMessage(), e);
        }
    }

    /// 手动重新加载密钥
    @ULog("'重新加载加解密密钥'")
    @PostMapping(value = "/keypair/refresh", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void refreshKeys() {
        cryptoKeyManager.refresh();
        log.info("密钥已手动刷新");
    }

}
