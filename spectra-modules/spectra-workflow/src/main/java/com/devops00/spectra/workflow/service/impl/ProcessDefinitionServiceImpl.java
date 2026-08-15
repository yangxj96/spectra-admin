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
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.workflow.javabean.converter.ProcessConverter;
import com.devops00.spectra.workflow.javabean.from.DeployProcessFrom;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionResourceVO;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionVO;
import com.devops00.spectra.workflow.service.ProcessDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 流程定义Service实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private final RepositoryService repositoryService;
    private final ProcessDiagramGenerator processDiagramGenerator;
    private final ProcessConverter processConverter;
    private final TimeMapper timeMapper;

    @Override
    public List<ProcessDefinitionVO> listAll() {
        var definitions = repositoryService.createProcessDefinitionQuery().list();
        var result = new ArrayList<ProcessDefinitionVO>();
        for (var definition : definitions) {
            result.add(assembleView(definition));
        }
        return result;
    }

    @Override
    public ProcessDefinitionVO getDetail(String id) {
        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (definition == null) {
            throw new DataNotExistException("流程定义不存在: " + id);
        }
        return assembleView(definition);
    }

    @Override
    public byte[] getDiagram(String id) {
        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (definition == null) {
            throw new DataNotExistException("流程定义不存在: " + id);
        }

        var model = repositoryService.getBpmnModel(id);
        if (model == null) {
            throw new DataNotExistException("无法获取流程模型: " + id);
        }

        BpmnDiagramSupport.ensureGraphicInfo(model);

        try (var diagramStream = processDiagramGenerator.generateDiagram(model, "png", Collections.emptyList(), Collections.emptyList(), "宋体", "宋体",
                "宋体", this.getClass().getClassLoader(), 1.0, false)) {
            if (diagramStream == null) {
                throw new DataException("无法生成流程图: " + id);
            }
            return diagramStream.readAllBytes();
        } catch (Exception e) {
            throw new DataException("读取流程图失败: " + e.getMessage());
        }
    }

    @Override
    public void suspend(String id) {
        repositoryService.suspendProcessDefinitionById(id);
        log.info("流程定义已挂起: id={}", id);
    }

    @Override
    public void activate(String id) {
        repositoryService.activateProcessDefinitionById(id);
        log.info("流程定义已激活: id={}", id);
    }

    @Override
    public ProcessDefinitionResourceVO getResource(String id) {
        var definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        if (definition == null) {
            throw new DataNotExistException("流程定义不存在: " + id);
        }
        try (var resource = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName())) {
            if (resource == null) {
                throw new DataNotExistException("无法获取流程资源: " + id);
            }
            return new ProcessDefinitionResourceVO(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new DataException("读取流程资源失败: " + e.getMessage());
        }
    }

    @Override
    public ProcessDefinitionVO deploy(DeployProcessFrom from) {
        var deploymentBuilder = repositoryService.createDeployment();
        if (from.getKey() != null) {
            deploymentBuilder.key(from.getKey());
        }
        if (from.getName() != null) {
            deploymentBuilder.name(from.getName());
        }
        if (from.getCategory() != null) {
            deploymentBuilder.category(from.getCategory());
        }
        String bpmnXml = from.getBpmnXml();
        if (bpmnXml.startsWith("\uFEFF")) {
            bpmnXml = bpmnXml.substring(1);
        }
        bpmnXml = bpmnXml.strip();
        var deployment = deploymentBuilder.addString("process.bpmn20.xml", bpmnXml).deploy();

        var definition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).latestVersion().singleResult();
        if (definition == null) {
            throw new DataException("部署成功但无法查询到流程定义");
        }
        log.info("流程定义部署成功: id={}, key={}, version={}", definition.getId(), definition.getKey(), definition.getVersion());
        return assembleView(definition);
    }

    /**
     * 实体转VO（含部署时间填充）
     */
    private ProcessDefinitionVO assembleView(org.flowable.engine.repository.ProcessDefinition definition) {
        var vo = processConverter.toVO(definition);
        // 填充部署时间
        if (definition.getDeploymentId() != null) {
            var deployment = repositoryService.createDeploymentQuery().deploymentId(definition.getDeploymentId()).singleResult();
            if (deployment != null && deployment.getDeploymentTime() != null) {
                vo.setDeploymentTime(timeMapper.toLocalDateTime(deployment.getDeploymentTime()));
            }
        }
        return vo;
    }
}
