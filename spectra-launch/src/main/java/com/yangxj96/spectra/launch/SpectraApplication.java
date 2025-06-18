/*
 *  Copyright 2025 yangxj96.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.yangxj96.spectra.launch;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.util.Properties;

/**
 * 项目启动类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@MapperScan("com.yangxj96.spectra.*.mapper")
@ComponentScan("com.yangxj96.spectra")
public class SpectraApplication {

    public static void main(String[] args) {
        // 从.env文件中读取环境变量
        SpringApplication app = new SpringApplication(SpectraApplication.class);
        Dotenv dotenv = Dotenv.configure().load();
        String jasyptPassword = dotenv.get("JASYPT_ENCRYPTOR_PASSWORD");
        Properties defaultProperties = new Properties();
        defaultProperties.put("jasypt.encryptor.password", jasyptPassword);
        app.setDefaultProperties(defaultProperties);
        app.run(args);
    }

}
