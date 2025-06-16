package com.yangxj96.spectra.launch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

/**
 * 项目启动类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@SpringBootApplication
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@MapperScan("com.yangxj96.spectra.*.mapper")
@ComponentScan("com.yangxj96.spectra")
public class SpectraApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpectraApplication.class, args);
    }

}
