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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 许可服务实现
 */
@Slf4j
@Service
public class LicenseServiceImpl implements LicenseService {

    @Resource
    private ObjectMapper om;

    @Resource
    private ResourceLoader resourceLoader;

    @Resource
    private LicenseProperties properties;

    @Override
    public void generateLicense(License license) {
        try {
            log.atDebug().log("开始生成许可证");

            var resource = resourceLoader.getResource(properties.getPrivateKey());
            if (!resource.exists()) {
                throw new FileNotFoundException("私钥未找到:" + properties.getPrivateKey());
            }

            try (var is = resource.getInputStream()) {
                var privateKey = RSAUtils.loadPrivateKey(is);

                //生成签名原文（不含 signature 字段）
                String contentToSign = LicenseUtils.toJsonWithoutSignature(license, om);
                log.atDebug().log("签名原文: \n{}", contentToSign);

                // 签名
                String signature = RSAUtils.sign(contentToSign, privateKey);
                license.setSignature(signature);

                // 保存许可证
                String outputPathStr = properties.getLicensePath();
                if (outputPathStr.startsWith("classpath:")) {
                    throw new IllegalArgumentException("license-path 不能以 classpath: 开头");
                }

                Path outputPath = Paths.get(outputPathStr);
                Files.createDirectories(outputPath.getParent());

                String fullJson = LicenseUtils.toJson(license, om);
                Files.write(outputPath, fullJson.getBytes());

                log.atDebug().log("✅ 许可证已生成: {}", outputPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.atError().log("生成许可失败,{}", e.getMessage(), e);
        }
    }

    @Override
    public void verifyLicense() {
        log.atDebug().log("🔍 正在验证许可证...");

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
            Path licensePath = Paths.get(properties.getLicensePath());
            if (!Files.exists(licensePath)) {
                log.atError().log("❌ 许可证文件不存在: {}", licensePath.toAbsolutePath());
                System.exit(1);
            }

            String content = Files.readString(licensePath);
            License license = om.readValue(content, License.class);

            if (license.getSignature() == null || license.getSignature().trim().isEmpty()) {
                log.atError().log("❌ 许可证缺少有效的签名字段！");
                System.exit(1);
            }

            // ✅ 复用生成时的方法：构造不含 signature 的 JSON 字符串
            String contentToVerify = LicenseUtils.toJsonWithoutSignature(license, om);
            log.debug("📝 用于签名验证的原文: \n{}", contentToVerify);

            // 3. 验证数字签名
            if (!RSAUtils.verify(contentToVerify, license.getSignature(), publicKey)) {
                log.atError().log("❌ 许可证已被篡改！签名验证失败。");
                System.exit(1);
            }
            log.atDebug().log("✅ 数字签名验证通过。");

            // 4. 验证硬件 ID
            String expectedHwid = license.getHwid();
            String actualHwid = HardwareIdUtil.generateHWID();

            if (!expectedHwid.equals(actualHwid)) {
                log.atError().log("❌ 硬件不匹配！请在授权机器上运行。");
                log.atError().log("   Expected HWID: {}", expectedHwid);
                log.atError().log("   Actual   HWID: {}", actualHwid);
                System.exit(1);
            }
            log.atDebug().log("✅ 硬件 ID 验证通过。");

            // 5. 验证过期时间
            var expiresAt = license.getExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                log.atError().log("❌ 许可证已过期，请联系供应商续期！");
                log.atError().log("   当前时间: {}", Instant.now());
                log.atError().log("   过期时间: {}", expiresAt);
                System.exit(1);
            }
            Duration remaining = Duration.between(Instant.now(), expiresAt);
            log.atDebug().log("✅ 许可证在有效期内，剩余时间: {}", LicenseUtils.formatDuration(remaining));

            // 🎉 所有验证通过
            log.atDebug().log("🎉 许可证验证全部通过，系统即将启动！");

        } catch (Exception e) {
            log.atError().log("❌ 许可证验证过程中发生严重错误: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

}
