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

package com.devops00.spectra.workflow.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 工作流-流程实例
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Slf4j
@RestController
@RequestMapping("/workflow/process-instances")
public class ProcessInstanceController {

    /*
    职责
    启动流程
    查询实例
    终止流程
    查看流程图状态
    接口示例
    POST /process-instances/start
    GET  /process-instances/{id}
    GET  /process-instances

    POST /process-instances/{id}/terminate
    GET  /process-instances/{id}/diagram
     */

    /// 启动流程
    @PostMapping(value = "/start", version = "1.0.0+")
    public void start() {

    }

}
