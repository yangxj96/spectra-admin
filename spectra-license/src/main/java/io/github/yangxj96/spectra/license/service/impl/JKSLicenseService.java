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

    private static final char[] PASSWORD = "QuVsKppcWvwwX2Vv".toCharArray();

    @Override
    public void generateLicense(License license) {
        try {
            log.atDebug().log("开始生成许可证");

            var resource = resourceLoader.getResource("classpath:jks/privateKeys.p12");
            if (!resource.exists()) {
                throw new FileNotFoundException("私钥未找到:classpath:jks/privateKeys.p12");
            }

            var keyStore = KeyStore.getInstance("PKCS12");
            try (var is = resource.getInputStream()) {
                keyStore.load(is, PASSWORD);
            }
            var privateKey = (PrivateKey) keyStore.getKey("privatekey", PASSWORD);

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
        } catch (Exception e) {
            log.atError().log("生成许可失败,{}", e.getMessage(), e);
        }
    }

    @Override
    public void verifyLicense() throws Exception {
        try {
            log.atDebug().log("开始验证许可证...");

            // 1. 加载公钥库
            var keyStoreResource = resourceLoader.getResource("classpath:jks/publicCerts.p12");
            if (!keyStoreResource.exists()) {
                log.atError().log("公钥库文件未找到: classpath:jks/publicCerts.p12");
                System.exit(1);
            }

            var keyStore = KeyStore.getInstance("PKCS12");
            try (var is = keyStoreResource.getInputStream()) {
                keyStore.load(is, PASSWORD);
            }

            // 2. 获取公钥证书
            if (!keyStore.containsAlias("publicCert")) {
                log.atError().log("公钥别名 'publicCert' 不存在");
                System.exit(1);
            }

            var cert = keyStore.getCertificate("publicCert");
            if (!(cert instanceof X509Certificate)) {
                log.atError().log("证书不是 X.509 格式");
                System.exit(1);
            }

            var publicCert = (X509Certificate) cert;

            log.atDebug().log("公钥证书加载成功: {}", publicCert.getSubjectX500Principal().getName());

            // 3. 加载 License 文件
            var licenseResource = Paths.get(properties.getLicensePath());
            if (!Files.exists(licenseResource)) {
                log.atError().log("❌ 许可证文件不存在: {}", licenseResource.toAbsolutePath());
                System.exit(1);
            }

            String content = Files.readString(licenseResource);
            License license = om.readValue(content, License.class);

            if (license == null) {
                log.atError().log("License 内容为空");
                System.exit(1);
            }

            String contentToVerify = LicenseUtils.toJsonWithoutSignature(license, om);
            log.debug("📝 用于签名验证的原文: \n{}", contentToVerify);
            if (!RSAUtils.verify(contentToVerify, license.getSignature(), publicCert.getPublicKey())) {
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
