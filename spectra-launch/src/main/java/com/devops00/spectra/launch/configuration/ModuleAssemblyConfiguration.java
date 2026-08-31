/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.launch.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 单体应用组合根的模块装配校验。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/31
 */
@Configuration(proxyBeanMethods = false)
public class ModuleAssemblyConfiguration {

    /**
     * 创建并校验模块装配描述。
     *
     * @param environment 启动环境
     * @return 已校验的模块装配描述
     */
    @Bean
    ModuleAssembly moduleAssembly(Environment environment) {
        var assembly = ModuleAssembly.from(environment);
        assembly.validate();
        return assembly;
    }
}
