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

import com.devops00.spectra.common.response.R;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.framework.configure.mvc.properties.SMProperties;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
/// 仅 ROLE_DEV_OPS 角色可访问。
/// 仅在 spectra.system.sm.enabled=true 时注册。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/11
@Slf4j
@RestController
@RequestMapping("/system/crypto")
@ConditionalOnProperty(prefix = "spectra.system.sm", name = "enabled", havingValue = "true")
public class CryptoController {

    private final SMProperties properties;

    public CryptoController(SMProperties properties) {
        this.properties = properties;
    }

    /// 生成新的 RSA 密钥对
    ///
    /// 新密钥对生成后，需要将公钥和私钥更新到配置文件中并重启服务才能生效。
    /// 此接口仅返回生成的密钥信息供管理员手动配置。
    ///
    /// @return 生成的密钥对信息（公钥 + 私钥 Base64）
    @ULog("'生成RSA密钥对'")
    @PostMapping(value = "/keypair/generate", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public R<Map<String, String>> generateKeyPair() {
        try {
            KeyPair keyPair = RSAUtils.generateKeyPair();
            String publicKey = RSAUtils.getPublicKeyBase64(keyPair.getPublic());
            String privateKey = RSAUtils.getPrivateKeyBase64(keyPair.getPrivate());

            Map<String, String> result = new HashMap<>();
            result.put("publicKey", publicKey);
            result.put("privateKey", privateKey);

            log.info("已生成新的 RSA 密钥对（2048位），请将密钥配置到 spectra.system.sm 中并重启服务");
            return R.success(result);
        } catch (Exception e) {
            log.error("生成RSA密钥对失败: {}", e.getMessage(), e);
            throw new RuntimeException("密钥生成失败: " + e.getMessage(), e);
        }
    }

    /// 获取当前 RSA 公钥
    ///
    /// 前端可通过此接口获取公钥，无需在环境变量中硬编码。
    ///
    /// @return 当前配置的公钥
    @GetMapping(value = "/keypair/public", version = "1.0.0+")
    public R<Map<String, String>> getPublicKey() {
        Map<String, String> result = new HashMap<>();
        result.put("publicKey", properties.getPublicKey());
        return R.success(result);
    }
}
