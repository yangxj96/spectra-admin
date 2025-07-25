package com.yangxj96.spectra.launch;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Properties;

/**
 * 启动类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/23
 */
@Slf4j
@SpringBootApplication
@ComponentScan("com.yangxj96.spectra")
public class LaunchApplication {

    public static void main(String[] args) {
        // 从.env文件中读取环境变量
        SpringApplication app = new SpringApplication(LaunchApplication.class);
        Dotenv dotenv = Dotenv.configure().load();
        String jasyptPassword = dotenv.get("JASYPT_ENCRYPTOR_PASSWORD");
        Properties defaultProperties = new Properties();
        defaultProperties.put("jasypt.encryptor.password", jasyptPassword);
        app.setDefaultProperties(defaultProperties);
        app.run(args);
    }

}
