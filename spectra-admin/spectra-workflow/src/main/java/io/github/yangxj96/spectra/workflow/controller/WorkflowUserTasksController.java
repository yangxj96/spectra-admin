/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.workflow.javabean.from.TaskCompleteFrom;
import io.github.yangxj96.spectra.workflow.service.WorkflowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流-任务相关
 */
@SaCheckLogin
@RestController
@RequestMapping("/workflow/tasks")
public class WorkflowUserTasksController {

    @Resource
    private WorkflowService workflowService;

    @GetMapping("/pending")
    public void getPendingTasks() {
        // 返回当前用户需要处理的任务
    }

    @GetMapping("/completed")
    public void getCompletedTasks() {
        // 返回当前用户已经完成的任务
    }

    @GetMapping("/initiated")
    public void getInitiatedProcesses() {
        // 返回当前用户启动的流程
    }

    @GetMapping("/summary")
    public void getTaskSummary() {
        // 返回待办、已办、超时等数量
    }

    @GetMapping("/{taskId}")
    public void getTaskDetail(@PathVariable String taskId) {
        // 返回任务详细信息
    }

    @PostMapping("/{taskId}/claim")
    public void claimTask(@PathVariable String taskId) {
        // 将任务分配给当前用户
    }

    @PostMapping("/{taskId}/unclaim")
    public void unclaimTask(@PathVariable String taskId) {
        // 释放任务，回到公共池
    }

    @PostMapping("/{taskId}/complete")
    public void completeTask(@PathVariable String taskId, @RequestBody TaskCompleteFrom request) {
        // 提交任务
    }

    @PostMapping("/{taskId}/delegate")
    public void delegateTask(@PathVariable String taskId, @RequestParam String assignee) {
        // 将任务委派给他人
    }

    @PostMapping("/{taskId}/transfer")
    public void transferTask(@PathVariable String taskId, @RequestParam String assignee) {
        // 直接将任务转交给他人
    }

}
