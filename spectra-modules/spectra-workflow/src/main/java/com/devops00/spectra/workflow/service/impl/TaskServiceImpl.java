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
import com.devops00.spectra.workflow.javabean.converter.TaskConverter;
import com.devops00.spectra.workflow.javabean.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
    private final TaskConverter taskConverter;

    @Override
    public IPage<TaskVO> todo(PageFrom page, String assignee) {
        var taskQuery = flowableTaskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .desc();

        long total = taskQuery.count();
        var tasks = taskQuery
                .listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        var records = tasks.stream().map(taskConverter::toVO).toList();

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
        var tasks = historicTaskQuery
                .listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        var records = tasks.stream().map(taskConverter::fromHistoricTask).toList();

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
}
