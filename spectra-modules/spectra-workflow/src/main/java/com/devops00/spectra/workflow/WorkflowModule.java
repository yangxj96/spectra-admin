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
import org.springframework.context.annotation.ComponentScan;

/// 工作流模块
///
/// - **作者:** yangxj96
/// - **版本:** 1.0
/// - **始于:** 2025/11/11 00:00
@ComponentScan("com.devops00.spectra.workflow")
@MapperScan("com.devops00.spectra.workflow.**.mapper")
public class WorkflowModule {
}
