package io.github.yangxj96.spectra.license.generator;

import io.github.yangxj96.spectra.license.utils.RSAUtils;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;

/**
 * 生成key
 */
@Slf4j
public class KeyGenerator {

    private KeyGenerator() {
    }

    static void main() throws Exception {
        // 1. 生成 RSA 密钥对（2048位）
        KeyPair keyPair = RSAUtils.generateKeyPair();

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
