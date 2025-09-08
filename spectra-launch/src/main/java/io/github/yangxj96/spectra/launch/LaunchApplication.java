package io.github.yangxj96.spectra.launch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/23
 */
@Slf4j
@SpringBootApplication
@ComponentScan("io.github.yangxj96.spectra")
public class LaunchApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaunchApplication.class, args);
    }

}
