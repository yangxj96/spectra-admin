/*
 *  Copyright 2018-2026 yangxj96
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
 */

package com.devops00.spectra.launch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.devops00.spectra.launch.configuration.ModuleAssemblyConfiguration;

/**
 * 启动类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/23 00:00
 */
@Slf4j
@SpringBootApplication
@Import(ModuleAssemblyConfiguration.class)
public class LaunchApplication {

    private LaunchApplication() {
    }

    /**
     * 处理内部业务逻辑（{@code main}）。
     */
    static void main(String[] args) {
        SpringApplication.run(LaunchApplication.class, args);
    }
}
