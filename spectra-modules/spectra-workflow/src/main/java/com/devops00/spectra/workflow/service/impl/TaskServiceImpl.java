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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.workflow.javabean.converter.TaskConverter;
import com.devops00.spectra.workflow.javabean.vo.TaskVO;
import com.devops00.spectra.workflow.service.ApprovalCallback;
import com.devops00.spectra.workflow.service.WorkflowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务管理Service实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements com.devops00.spectra.workflow.service.TaskService {

    private final TaskService flowableTaskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    private final WorkflowService workflowService;

    private final TaskConverter taskConverter;

    @Override
    public IPage<TaskVO> todo(PageFrom page, String assignee, String processDefinitionKey) {
        var taskQuery = flowableTaskService.createTaskQuery().taskAssignee(assignee).orderByTaskCreateTime().desc();
        if (StringUtils.hasText(processDefinitionKey)) {
            taskQuery.processDefinitionKey(processDefinitionKey);
        }

        long total = taskQuery.count();
        var tasks = taskQuery.listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        var records = tasks.stream().map(this::assembleView).toList();

        Page<TaskVO> result = new Page<>(page.getPageNum(), page.getPageSize());
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<TaskVO> done(PageFrom page, String assignee, String processDefinitionKey) {
        var historicTaskQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        if (StringUtils.hasText(processDefinitionKey)) {
            historicTaskQuery.processDefinitionKey(processDefinitionKey);
        }

        long total = historicTaskQuery.count();
        var tasks = historicTaskQuery.listPage((int) ((page.getPageNum() - 1) * page.getPageSize()), (int) page.getPageSize().longValue());

        var records = tasks.stream().map(this::assembleView).toList();

        Page<TaskVO> result = new Page<>(page.getPageNum(), page.getPageSize());
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(String taskId, String comment, String operator) {
        var task = requireOwnedTask(taskId, operator);

        // 添加审批意见
        if (comment != null && !comment.isBlank()) {
            flowableTaskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        // 完成任务
        flowableTaskService.complete(taskId, variables);
        dispatchIfFinished(task.getProcessInstanceId(), task.getProcessDefinitionId(), true, null);
        log.info("任务审批通过: taskId={}, processInstanceId={}", taskId, task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String taskId, String comment, String operator) {
        var task = requireOwnedTask(taskId, operator);

        // 添加驳回意见
        if (comment != null && !comment.isBlank()) {
            flowableTaskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);

        // 完成任务（驳回）
        flowableTaskService.complete(taskId, variables);
        dispatchIfFinished(task.getProcessInstanceId(), task.getProcessDefinitionId(), false, comment);
        log.info("任务已驳回: taskId={}, processInstanceId={}", taskId, task.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String taskId, String targetUserId, String operator) {
        var task = requireOwnedTask(taskId, operator);

        // 转办：设置新的处理人
        flowableTaskService.setAssignee(taskId, targetUserId);
        log.info("任务已转办: taskId={}, from={}, to={}", taskId, task.getAssignee(), targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(String taskId, String targetUserId, String operator) {
        var task = requireOwnedTask(taskId, operator);

        // 委派任务
        flowableTaskService.delegateTask(taskId, targetUserId);
        log.info("任务已委派: taskId={}, from={}, to={}", taskId, task.getAssignee(), targetUserId);
    }

    @Override
    public boolean canAccessProcess(String processInstanceId, String username) {
        if (!StringUtils.hasText(processInstanceId) || !StringUtils.hasText(username)) {
            return false;
        }
        if (flowableTaskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(username).count() > 0) {
            return true;
        }
        return historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).taskAssignee(username).count() > 0;
    }

    private org.flowable.task.api.Task requireOwnedTask(String taskId, String operator) {
        if (!StringUtils.hasText(operator)) {
            throw new DataNotExistException("任务不存在或无权处理: " + taskId);
        }
        var task = flowableTaskService.createTaskQuery().taskId(taskId).taskAssignee(operator).singleResult();
        if (task == null) {
            throw new DataNotExistException("任务不存在或无权处理: " + taskId);
        }
        return task;
    }

    private TaskVO assembleView(org.flowable.task.api.Task task) {
        var vo = taskConverter.toVO(task);
        enrich(vo, task.getProcessInstanceId(), task.getProcessDefinitionId(), false);
        return vo;
    }

    private TaskVO assembleView(org.flowable.task.api.history.HistoricTaskInstance task) {
        var vo = taskConverter.fromHistoricTask(task);
        enrich(vo, task.getProcessInstanceId(), task.getProcessDefinitionId(), true);
        return vo;
    }

    private void enrich(TaskVO vo, String processInstanceId, String processDefinitionId, boolean historic) {
        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        if (definition != null) {
            vo.setProcessDefinitionKey(definition.getKey());
        }
        if (historic) {
            var instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (instance != null) {
                vo.setBusinessKey(instance.getBusinessKey());
            }
        } else {
            var instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (instance != null) {
                vo.setBusinessKey(instance.getBusinessKey());
            }
        }
    }

    /**
     * 流程完成后按流程定义分发一次业务回调。回调实现按业务状态保证幂等。
     */
    private void dispatchIfFinished(String processInstanceId, String processDefinitionId, boolean approved, String reason) {
        var historic = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (historic == null || historic.getEndTime() == null) {
            return;
        }
        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        if (definition == null) {
            return;
        }
        ApprovalCallback callback = workflowService.getCallback(definition.getKey());
        if (callback == null) {
            return;
        }
        if (approved) {
            var variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(item -> item.getVariableName(), item -> item.getValue(), (left, right) -> right));
            callback.onApproved(historic.getBusinessKey(), variables);
        } else {
            callback.onRejected(historic.getBusinessKey(), reason);
        }
    }
}
