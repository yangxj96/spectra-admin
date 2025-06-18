package com.yangxj96.spectra.launch;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 启动类单元测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@SpringBootTest
class SpectraApplicationTest {

    @Resource(name = "lazyJasyptStringEncryptor")
    private StringEncryptor stringEncryptor;

    @Test
    void encryptor() {
        log.info("数据库密码:{}", stringEncryptor.encrypt("postgres"));
        log.info("用户默认密码:{}", stringEncryptor.encrypt("123456"));
    }

}