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

package com.devops00.spectra.workflow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.workflow.javabean.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// 任务管理Service实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements com.devops00.spectra.workflow.service.TaskService {

    private final TaskService flowableTaskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public IPage<TaskVO> todo(PageFrom page, String assignee) {
        var taskQuery = flowableTaskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .desc();

        long total = taskQuery.count();
        List<org.flowable.task.api.Task> tasks = taskQuery
                .listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        List<TaskVO> records = new ArrayList<>();
        for (var task : tasks) {
            records.add(convertToVO(task));
        }

        Page<TaskVO> result = new Page<>(page.getPageNum(), page.getPageSize());
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<TaskVO> done(PageFrom page, String assignee) {
        var historicTaskQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc();

        long total = historicTaskQuery.count();
        List<HistoricTaskInstance> tasks = historicTaskQuery
                .listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        List<TaskVO> records = new ArrayList<>();
        for (var task : tasks) {
            records.add(convertHistoricToVO(task));
        }

        Page<TaskVO> result = new Page<>(page.getPageNum(), page.getPageSize());
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(String taskId, String comment) {
        var task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 添加审批意见
        if (comment != null && !comment.isBlank()) {
            flowableTaskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        // 完成任务
        flowableTaskService.complete(taskId, variables);
        log.info("任务审批通过: taskId={}, processInstanceId={}", taskId, task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String taskId, String comment) {
        var task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 添加驳回意见
        if (comment != null && !comment.isBlank()) {
            flowableTaskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);

        // 完成任务（驳回）
        flowableTaskService.complete(taskId, variables);
        log.info("任务已驳回: taskId={}, processInstanceId={}", taskId, task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String taskId, String targetUserId) {
        var task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 转办：设置新的处理人
        flowableTaskService.setAssignee(taskId, targetUserId);
        log.info("任务已转办: taskId={}, from={}, to={}", taskId, task.getAssignee(), targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(String taskId, String targetUserId) {
        var task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 委派任务
        flowableTaskService.delegateTask(taskId, targetUserId);
        log.info("任务已委派: taskId={}, from={}, to={}", taskId, task.getAssignee(), targetUserId);
    }

    /// 将 Flowable Task 转换为 TaskVO
    private TaskVO convertToVO(org.flowable.task.api.Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setAssignee(task.getAssignee());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionId(task.getProcessDefinitionId());
        vo.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().toString() : null);
        vo.setDescription(task.getDescription());
        return vo;
    }

    /// 将 HistoricTaskInstance 转换为 TaskVO
    private TaskVO convertHistoricToVO(HistoricTaskInstance task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setAssignee(task.getAssignee());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setProcessDefinitionId(task.getProcessDefinitionId());
        vo.setCreateTime(task.getStartTime() != null ? task.getStartTime().toString() : null);
        vo.setDescription(task.getDescription());
        return vo;
    }
}
