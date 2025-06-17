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
