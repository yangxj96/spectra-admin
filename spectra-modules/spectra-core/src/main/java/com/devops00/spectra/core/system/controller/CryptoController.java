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
import com.devops00.spectra.core.system.javabean.vo.CryptoClientKeyVO;
import com.devops00.spectra.core.system.javabean.vo.CryptoConfigVO;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import com.devops00.spectra.common.audit.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加解密密钥管理接口
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@RestController
@RequestMapping("/system/crypto")
@RequiredArgsConstructor
public class CryptoController {

    /**
     * CryptoKey管理服务
     */
    private final CryptoKeyManager cryptoKeyManager;

    /**
     * 获取加解密配置（前端初始化调用）
     */
    @Audit("'获取加解密配置'")
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @GetMapping(value = "/config", version = "1.0.0")
    public CryptoConfigVO getConfig() {
        return new CryptoConfigVO(cryptoKeyManager.isEnabled(), cryptoKeyManager.getServerPublicKeyBase64());
    }

    /**
     * 获取客户端私钥（需登录）
     *
     * <p>客户端私钥用于浏览器端解密响应和签名请求，是加密通信初始化数据，不属于密钥管理操作。</p>
     */
    @Audit("'获取客户端私钥'")
    @Encrypt(response = false)
    @GetMapping(value = "/keypair/client-private", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public CryptoClientKeyVO getClientPrivateKey() {
        return new CryptoClientKeyVO(cryptoKeyManager.getClientPrivateKeyBase64());
    }

}
