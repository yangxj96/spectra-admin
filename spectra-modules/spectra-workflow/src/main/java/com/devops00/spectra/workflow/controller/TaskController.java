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

import com.devops00.spectra.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 工作流-任务相关
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@RestController
@RequestMapping("/workflow/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final WorkflowService workflowService;

    /*
    👉 ⭐ 最核心（用户日常操作）
    职责
    我的待办 / 已办
    任务处理
    转办 / 委派
    接口示例
    GET  /tasks/todo
    GET  /tasks/done

    POST /tasks/{id}/complete
    POST /tasks/{id}/reject
    POST /tasks/{id}/transfer
    POST /tasks/{id}/delegate
     */


}
