package com.yangxj96.spectra;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.yangxj96.spectra.*.mapper")
public class SpectraApplication {

    private SpectraApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(SpectraApplication.class, args);
    }

}
