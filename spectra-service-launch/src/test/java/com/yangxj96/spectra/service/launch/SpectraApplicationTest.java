package com.yangxj96.spectra.service.launch;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 启动类单元测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@SpringBootTest(classes = SpectraApplication.class)
class SpectraApplicationTest {

    @DynamicPropertySource
    static void jasyptProperties(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure().directory("../.env").load();
        String jasyptPassword = dotenv.get("JASYPT_ENCRYPTOR_PASSWORD");
        registry.add("jasypt.encryptor.password", () -> jasyptPassword);
    }

    @Test
    void test01() {
        Assertions.assertTrue(true);
    }

}