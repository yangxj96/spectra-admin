package io.github.yangxj96.spectra.launch;


import io.github.yangxj96.spectra.license.utils.HardwareIdUtil;
import io.github.yangxj96.spectra.license.utils.RSAUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 许可证测试相关
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/11/24 14:32
 */
@Slf4j
@SpringBootTest
class LicenseControllerTest {


    @Test
    void generatorHWID() {
        var string = HardwareIdUtil.generateHWID();
        log.debug("当前硬件ID:{}", string);
    }

    @Test
    void generatorKey() {
        // 1. 生成 RSA 密钥对（2048位）
        var keyPair = RSAUtils.generateKeyPair();

        // 2. 保存私钥到文件（你自己保留！）
        RSAUtils.savePrivateKey(keyPair.getPrivate(), "private-key.pem");
        log.info("✅ 私钥已生成：private-key.pem");

        // 3. 保存公钥到文件（分发给客户）
        RSAUtils.savePublicKey(keyPair.getPublic(), "public-key.pem");
        log.info("✅ 公钥已生成：public-key.pem");

        log.info("\n⚠️  安全提示：");
        log.info("  - private-key.pem 必须严格保密！");
        log.info("  - 不要提交到 Git、不要发给客户！");
        log.info("  - 建议加密备份。");
    }

}
