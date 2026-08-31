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

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.workflow.javabean.converter.ProcessConverter;
import com.devops00.spectra.workflow.javabean.vo.ProcessInstanceVO;
import com.devops00.spectra.workflow.api.ApprovalCallback;
import com.devops00.spectra.workflow.api.ProcessInstanceService;
import com.devops00.spectra.workflow.api.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程实例Service实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 15:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final ProcessDiagramGenerator processDiagramGenerator;
    private final ProcessConverter processConverter;
    private final WorkflowService workflowService;

    @Override
    public String start(String processDefinitionKey, String businessKey) {
        return start(processDefinitionKey, businessKey, new HashMap<>());
    }

    @Override
    public String start(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        try {
            // 1. 防重复启动
            long count = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).count();

            if (count > 0) {
                throw new DataException("流程已存在: " + businessKey);
            }

            // 2. 启动流程
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

            log.info("流程已启动: processInstanceId={}, businessKey={}", instance.getId(), businessKey);
            return instance.getId();
        } catch (Exception e) {
            // 3. 统一异常
            throw new DataException("启动流程失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ProcessInstanceVO getStatus(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (instance == null) {
            throw new DataNotExistException("流程实例不存在: " + processInstanceId);
        }

        return processConverter.toVO(instance);
    }

    @Override
    public Map<String, Object> getVariables(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (instance == null) {
            throw new DataNotExistException("流程实例不存在: " + processInstanceId);
        }

        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    public void terminate(String processInstanceId, String reason) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (instance == null) {
            log.info("流程实例已结束，无需重复终止: processInstanceId={}, reason={}", processInstanceId, reason);
            return;
        }

        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(instance.getProcessDefinitionId()).singleResult();
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        if (definition != null) {
            ApprovalCallback callback = workflowService.getCallback(definition.getKey());
            if (callback != null) {
                callback.onTerminated(instance.getBusinessKey(), reason);
            }
        }
        log.info("流程已终止: processInstanceId={}, reason={}", processInstanceId, reason);
    }

    @Override
    public byte[] getDiagram(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (instance == null) {
            throw new DataNotExistException("流程实例不存在: " + processInstanceId);
        }

        // 获取当前活动ID
        var activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);

        // 获取 BPMN 模型
        var model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
        if (model == null) {
            throw new DataNotExistException("无法获取流程模型: " + processInstanceId);
        }

        BpmnDiagramSupport.ensureGraphicInfo(model);

        // 使用 ProcessDiagramGenerator 生成流程图（高亮当前节点）
        try (var diagramStream = processDiagramGenerator.generateDiagram(model, "png", activeActivityIds, Collections.emptyList(), "宋体", "宋体", "宋体",
                this.getClass().getClassLoader(), 1.0, false)) {
            if (diagramStream == null) {
                throw new DataException("无法生成流程图: " + processInstanceId);
            }
            return diagramStream.readAllBytes();
        } catch (Exception e) {
            throw new DataException("读取流程图失败: " + e.getMessage(), e);
        }
    }
}
