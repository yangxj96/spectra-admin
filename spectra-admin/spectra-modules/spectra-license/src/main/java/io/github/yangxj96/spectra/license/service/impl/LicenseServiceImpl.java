/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.license.service.impl;

import io.github.yangxj96.spectra.license.javabean.bean.License;
import io.github.yangxj96.spectra.license.properties.LicenseProperties;
import io.github.yangxj96.spectra.license.service.LicenseService;
import io.github.yangxj96.spectra.license.utils.HardwareIdUtil;
import io.github.yangxj96.spectra.license.utils.LicenseUtils;
import io.github.yangxj96.spectra.license.utils.RSAUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;

/// 许可服务实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Slf4j
@Service
public class LicenseServiceImpl implements LicenseService {

    private final ObjectMapper om;

    private final ResourceLoader resourceLoader;

    private final LicenseProperties properties;

    public LicenseServiceImpl(ObjectMapper om, ResourceLoader resourceLoader, LicenseProperties properties) {
        this.om = om;
        this.resourceLoader = resourceLoader;
        this.properties = properties;
    }

    @Override
    public void generateLicense(License license) {
        try {
            log.debug("开始生成许可证");

            var resource = resourceLoader.getResource(properties.getPrivateKey());
            if (!resource.exists()) {
                throw new FileNotFoundException("私钥未找到:" + properties.getPrivateKey());
            }

            try (var is = resource.getInputStream()) {
                var privateKey = RSAUtils.loadPrivateKey(is);

                //生成签名原文（不含 signature 字段）
                var contentToSign = LicenseUtils.toJsonWithoutSignature(license, om);
                log.debug("签名原文: \n{}", contentToSign);

                // 签名
                var signature = RSAUtils.sign(contentToSign, privateKey);
                license.setSignature(signature);

                // 保存许可证
                var outputPathStr = properties.getLicensePath();
                if (outputPathStr.startsWith("classpath:")) {
                    throw new IllegalArgumentException("license-path 不能以 classpath: 开头");
                }

                var outputPath = Paths.get(outputPathStr);
                Files.createDirectories(outputPath.getParent());

                var fullJson = LicenseUtils.toJson(license, om);
                Files.write(outputPath, fullJson.getBytes());

                log.debug("✅ 许可证已生成: {}", outputPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("生成许可失败,{}", e.getMessage(), e);
        }
    }

    @Override
    public void verifyLicense() {
        log.debug("🔍 正在验证许可证...");

        try {
            // 1. 加载公钥
            var resource = resourceLoader.getResource(properties.getPublicKey());
            if (!resource.exists()) {
                throw new FileNotFoundException("公钥未找到: " + properties.getPublicKey());
            }

            PublicKey publicKey;
            try (var is = resource.getInputStream()) {
                publicKey = RSAUtils.loadPublicKey(is);
            }

            // 2. 读取 license 文件
            var licensePath = Paths.get(properties.getLicensePath());
            if (!Files.exists(licensePath)) {
                log.error("❌ 许可证文件不存在: {}", licensePath.toAbsolutePath());
                System.exit(1);
            }

            var content = Files.readString(licensePath);
            var license = om.readValue(content, License.class);

            if (license.getSignature() == null || license.getSignature().trim().isEmpty()) {
                log.error("❌ 许可证缺少有效的签名字段！");
                System.exit(1);
            }

            // ✅ 复用生成时的方法：构造不含 signature 的 JSON 字符串
            var contentToVerify = LicenseUtils.toJsonWithoutSignature(license, om);
            log.debug("📝 用于签名验证的原文: \n{}", contentToVerify);

            // 3. 验证数字签名
            if (!RSAUtils.verify(contentToVerify, license.getSignature(), publicKey)) {
                log.error("❌ 许可证已被篡改！签名验证失败。");
                System.exit(1);
            }
            log.debug("✅ 数字签名验证通过。");

            // 4. 验证硬件 ID
            var expectedHwid = license.getHwid();
            var actualHwid = HardwareIdUtil.generateHWID();

            if (!expectedHwid.equals(actualHwid)) {
                log.error("❌ 硬件不匹配！请在授权机器上运行。");
                log.error("   Expected HWID: {}", expectedHwid);
                log.error("   Actual   HWID: {}", actualHwid);
                System.exit(1);
            }
            log.debug("✅ 硬件 ID 验证通过。");

            // 5. 验证过期时间
            var expiresAt = license.getExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                log.error("❌ 许可证已过期，请联系供应商续期！");
                log.error("   当前时间: {}", Instant.now());
                log.error("   过期时间: {}", expiresAt);
                System.exit(1);
            }
            var remaining = Duration.between(Instant.now(), expiresAt);
            log.debug("✅ 许可证在有效期内，剩余时间: {}", LicenseUtils.formatDuration(remaining));

            // 🎉 所有验证通过
            log.debug("🎉 许可证验证全部通过，系统即将启动！");

        } catch (Exception e) {
            log.error("❌ 许可证验证过程中发生严重错误: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

}
