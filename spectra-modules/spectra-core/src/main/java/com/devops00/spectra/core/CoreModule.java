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

package com.devops00.spectra.core;

import com.devops00.spectra.framework.FrameworkModule;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 基础设施模块
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/8 23:30
 */
@AutoConfiguration(after = FrameworkModule.class)
@ComponentScan(basePackageClasses = CoreModule.class)
@MapperScan("com.devops00.spectra.core.**.mapper")
public class CoreModule {

}
