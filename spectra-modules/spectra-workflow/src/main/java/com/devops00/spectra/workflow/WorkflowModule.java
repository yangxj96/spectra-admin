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

package com.devops00.spectra.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * 工作流模块
 *
 * <ul>
 * <li><b>作者:</b> yangxj96</li>
 * <li><b>版本:</b> 1.0</li>
 * <li><b>始于:</b> 2025/11/11 00:00</li>
 * </ul>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spectra.modules.workflow", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackageClasses = WorkflowModule.class)
@MapperScan("com.devops00.spectra.workflow.**.mapper")
public class WorkflowModule {
}
