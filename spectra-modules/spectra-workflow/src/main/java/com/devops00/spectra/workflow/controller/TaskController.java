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

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.workflow.javabean.from.TaskCompleteFrom;
import com.devops00.spectra.workflow.javabean.from.TaskDelegateFrom;
import com.devops00.spectra.workflow.javabean.from.TaskTransferFrom;
import com.devops00.spectra.workflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/// 工作流-任务相关
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@RestController
@RequestMapping("/workflow/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /// 查询待办任务
    ///
    /// @param page 分页参数
    /// @return 待办任务列表
    @GetMapping(value = "/todo", version = "1.0.0+")
    public Object todo(PageFrom page) {
        String username = SecUtil.getCurrentUsername();
        return taskService.todo(page, username);
    }

    /// 查询已办任务
    ///
    /// @param page 分页参数
    /// @return 已办任务列表
    @GetMapping(value = "/done", version = "1.0.0+")
    public Object done(PageFrom page) {
        String username = SecUtil.getCurrentUsername();
        return taskService.done(page, username);
    }

    /// 完成任务（审批通过）
    ///
    /// @param id   任务ID
    /// @param from 完成参数
    @PostMapping(value = "/{id}/complete", version = "1.0.0+")
    public void complete(@PathVariable String id, @RequestBody TaskCompleteFrom from) {
        taskService.complete(id, from.getComment());
    }

    /// 驳回任务
    ///
    /// @param id   任务ID
    /// @param from 完成参数
    @PostMapping(value = "/{id}/reject", version = "1.0.0+")
    public void reject(@PathVariable String id, @RequestBody TaskCompleteFrom from) {
        taskService.reject(id, from.getComment());
    }

    /// 转办任务
    ///
    /// @param id   任务ID
    /// @param from 转办参数
    @PostMapping(value = "/{id}/transfer", version = "1.0.0+")
    public void transfer(@PathVariable String id, @RequestBody TaskTransferFrom from) {
        taskService.transfer(id, from.getTargetUserId());
    }

    /// 委派任务
    ///
    /// @param id   任务ID
    /// @param from 委派参数
    @PostMapping(value = "/{id}/delegate", version = "1.0.0+")
    public void delegate(@PathVariable String id, @RequestBody TaskDelegateFrom from) {
        taskService.delegate(id, from.getTargetUserId());
    }
}
