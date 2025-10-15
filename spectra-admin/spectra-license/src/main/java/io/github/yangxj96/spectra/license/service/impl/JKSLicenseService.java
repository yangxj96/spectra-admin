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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.spectra.license.javabean.bean.License;
import io.github.yangxj96.spectra.license.properties.LicenseProperties;
import io.github.yangxj96.spectra.license.service.LicenseService;
import io.github.yangxj96.spectra.license.utils.HardwareIdUtil;
import io.github.yangxj96.spectra.license.utils.LicenseUtils;
import io.github.yangxj96.spectra.license.utils.RSAUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;

/**
 * JKS方式实现
 */
@Slf4j
@Service("jksLicenseService")
public class JKSLicenseService implements LicenseService {

    @Resource
    private ObjectMapper om;

    @Resource
    private ResourceLoader resourceLoader;

    @Resource
    private LicenseProperties properties;

    @Override
    public void generateLicense(License license) {
        try {
            log.debug("开始生成许可证");

            var resource = resourceLoader.getResource("classpath:jks/privateKeys.p12");
            if (!resource.exists()) {
                throw new FileNotFoundException("私钥未找到:classpath:jks/privateKeys.p12");
            }

            var keyStore = KeyStore.getInstance("PKCS12");
            try (var is = resource.getInputStream()) {
                keyStore.load(is, properties.getPassword().toCharArray());
            }
            var privateKey = (PrivateKey) keyStore.getKey("privatekey", properties.getPassword().toCharArray());

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
        } catch (Exception e) {
            log.error("生成许可失败,{}", e.getMessage(), e);
        }
    }

    @Override
    public void verifyLicense() {
        try {
            log.debug("开始验证许可证...");

            // 1. 加载公钥库
            var keyStoreResource = resourceLoader.getResource("classpath:jks/publicCerts.p12");
            if (!keyStoreResource.exists()) {
                log.error("公钥库文件未找到: classpath:jks/publicCerts.p12");
                System.exit(1);
            }

            var keyStore = KeyStore.getInstance("PKCS12");
            try (var is = keyStoreResource.getInputStream()) {
                keyStore.load(is, properties.getPassword().toCharArray());
            }

            // 2. 获取公钥证书
            if (!keyStore.containsAlias("publicCert")) {
                log.error("公钥别名 'publicCert' 不存在");
                System.exit(1);
            }

            var cert = keyStore.getCertificate("publicCert");
            if (!(cert instanceof X509Certificate)) {
                log.error("证书不是 X.509 格式");
                System.exit(1);
            }

            var publicCert = (X509Certificate) cert;

            log.debug("公钥证书加载成功: {}", publicCert.getSubjectX500Principal().getName());

            // 3. 加载 License 文件
            var licenseResource = Paths.get(properties.getLicensePath());
            if (!Files.exists(licenseResource)) {
                log.error("❌ 许可证文件不存在: {}", licenseResource.toAbsolutePath());
                System.exit(1);
            }

            var content = Files.readString(licenseResource);
            var license = om.readValue(content, License.class);

            if (license == null) {
                log.error("License 内容为空");
                System.exit(1);
            }

            var contentToVerify = LicenseUtils.toJsonWithoutSignature(license, om);
            log.debug("📝 用于签名验证的原文: \n{}", contentToVerify);
            if (!RSAUtils.verify(contentToVerify, license.getSignature(), publicCert.getPublicKey())) {
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
