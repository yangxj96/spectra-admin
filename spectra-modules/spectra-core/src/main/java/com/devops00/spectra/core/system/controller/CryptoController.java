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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.constant.ConfiguredValueType;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

/// 加解密密钥管理接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
@RestController
@RequestMapping("/system/crypto")
public class CryptoController {

    /// CryptoKey管理服务
    private final CryptoKeyManager cryptoKeyManager;

    /// 系统配置服务
    private final ConfiguredService configuredService;

    public CryptoController(CryptoKeyManager cryptoKeyManager, ConfiguredService configuredService) {
        this.cryptoKeyManager = cryptoKeyManager;
        this.configuredService = configuredService;
    }

    /// 获取加解密配置（前端初始化调用）
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/config", version = "1.0.0+")
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", cryptoKeyManager.isEnabled());
        result.put("serverPublicKey", cryptoKeyManager.getServerPublicKeyBase64());
        return result;
    }

    /// 获取客户端私钥（需登录）
    @Encrypt(response = false)
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/keypair/client-private", version = "1.0.0+")
    public Map<String, String> getClientPrivateKey() {
        String privateKey = cryptoKeyManager.getClientPrivateKeyBase64();
        Map<String, String> result = new HashMap<>();
        result.put("privateKey", privateKey);
        return result;
    }

    /// 生成新的 RSA 密钥对
    @ULog("'生成RSA密钥对'")
    @PostMapping(value = "/keypair/generate", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public Map<String, String> generateKeyPair() {
        try {
            KeyPair serverPair = RSAUtils.generateKeyPair();
            KeyPair clientPair = RSAUtils.generateKeyPair();

            Map<String, String> configs = new HashMap<>();
            configs.put("crypto.server.public-key", RSAUtils.getPublicKeyBase64(serverPair.getPublic()));
            configs.put("crypto.server.private-key", RSAUtils.getPrivateKeyBase64(serverPair.getPrivate()));
            configs.put("crypto.client.public-key", RSAUtils.getPublicKeyBase64(clientPair.getPublic()));
            configs.put("crypto.client.private-key", RSAUtils.getPrivateKeyBase64(clientPair.getPrivate()));
            configs.put("crypto.enabled", "true");

            String remarks = "RSA密钥对自动生成";
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                saveOrUpdateConfig(entry.getKey(), entry.getValue(), remarks);
            }

            cryptoKeyManager.refresh();

            Map<String, String> result = new HashMap<>();
            result.put("serverPublicKey", configs.get("crypto.server.public-key"));
            result.put("serverPrivateKey", configs.get("crypto.server.private-key"));
            result.put("clientPublicKey", configs.get("crypto.client.public-key"));
            result.put("clientPrivateKey", configs.get("crypto.client.private-key"));

            log.info("已生成并保存新的 RSA 密钥对（2048位 × 2）");
            return result;
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

    /// 保存或更新配置（按 key 去重）
    private void saveOrUpdateConfig(String key, String value, String remarks) {
        var existing = configuredService.getOne(
                new LambdaQueryWrapper<Configured>().eq(Configured::getKey, key));
        if (existing != null) {
            existing.setValue(value);
            existing.setRemarks(remarks);
            configuredService.updateById(existing);
        } else {
            var entity = new Configured();
            entity.setKey(key);
            entity.setValue(value);
            entity.setType(ConfiguredValueType.TEXT);
            entity.setRemarks(remarks);
            configuredService.save(entity);
        }
    }
}
