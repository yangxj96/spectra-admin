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

import com.devops00.spectra.workflow.javabean.from.DeployProcessFrom;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionResourceVO;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/// 工作流-流程定义
///
/// 面向"设计器 + 运维"
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Slf4j
@RestController
@RequestMapping("/workflow/process-definitions")
@RequiredArgsConstructor
public class ProcessDefinitionController {

    private final RepositoryService repositoryService;
    private final ProcessDiagramGenerator processDiagramGenerator;

    /// 获取所有的流程定义
    ///
    /// @return 流程定义列表
    @GetMapping(value = "", version = "1.0.0+")
    public List<ProcessDefinitionVO> definitions() {
        var definitions = repositoryService
                .createProcessDefinitionQuery()
                .list();
        var result = new ArrayList<ProcessDefinitionVO>();
        for (var definition : definitions) {
            result.add(convertToVO(definition));
        }
        return result;
    }

    /// 获取流程定义详情
    ///
    /// @param id 流程定义ID
    /// @return 流程定义详情
    @GetMapping(value = "/{id}", version = "1.0.0+")
    public ProcessDefinitionVO definitionDetail(@PathVariable String id) {
        var definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + id);
        }
        return convertToVO(definition);
    }

    /// 获取流程定义图
    ///
    /// @param id 流程定义ID
    /// @return 流程图图片（PNG格式）
    @GetMapping(value = "/{id}/diagram", version = "1.0.0+", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getDiagram(@PathVariable String id) {
        var definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + id);
        }

        // 获取 BPMN 模型
        var model = repositoryService.getBpmnModel(id);
        if (model == null) {
            throw new RuntimeException("无法获取流程模型: " + id);
        }

        // 使用 ProcessDiagramGenerator 生成流程图
        try (var diagramStream = processDiagramGenerator.generateDiagram(
                model,                           // BpmnModel
                "png",                           // imageType
                java.util.Collections.emptyList(), // highLightedActivities
                java.util.Collections.emptyList(), // highLightedFlows
                "宋体",                          // activityFontName
                "宋体",                          // labelFontName
                "宋体",                          // annotationFontName
                this.getClass().getClassLoader(), // customClassLoader
                1.0,                             // scaleFactor
                false                            // drawSequenceFlowNameWithNoLabelDI
        )) {
            if (diagramStream == null) {
                throw new RuntimeException("无法生成流程图: " + id);
            }
            return diagramStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("读取流程图失败: " + e.getMessage(), e);
        }
    }

    /// 挂起流程定义
    ///
    /// @param id 流程定义ID
    @PostMapping(value = "/{id}/suspend", version = "1.0.0+")
    public void suspend(@PathVariable String id) {
        repositoryService.suspendProcessDefinitionById(id);
        log.info("流程定义已挂起: id={}", id);
    }

    /// 激活流程定义
    ///
    /// @param id 流程定义ID
    @PostMapping(value = "/{id}/activate", version = "1.0.0+")
    public void activate(@PathVariable String id) {
        repositoryService.activateProcessDefinitionById(id);
        log.info("流程定义已激活: id={}", id);
    }

    /// 获取流程定义的 BPMN XML 源码
    ///
    /// @param id 流程定义ID
    /// @return 流程定义资源VO
    @GetMapping(value = "/{id}/resource", version = "1.0.0+")
    public ProcessDefinitionResourceVO getResource(@PathVariable String id) {
        var definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();
        if (definition == null) {
            throw new RuntimeException("流程定义不存在: " + id);
        }
        try (var resource = repositoryService.getResourceAsStream(
                definition.getDeploymentId(), definition.getResourceName())) {
            if (resource == null) {
                throw new RuntimeException("无法获取流程资源: " + id);
            }
            return new ProcessDefinitionResourceVO(
                    new String(resource.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("读取流程资源失败: " + e.getMessage(), e);
        }
    }

    /// 部署流程定义（新增或更新版本）
    ///
    /// @param from 部署参数
    /// @return 部署后的流程定义
    @PostMapping(value = "/deploy", version = "1.0.0+")
    public ProcessDefinitionVO deploy(@RequestBody @Valid DeployProcessFrom from) {
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
        var deployment = deploymentBuilder
                .addString("process.bpmn20.xml", from.getBpmnXml())
                .deploy();

        // 查询刚部署的流程定义（取最新版本）
        var definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .latestVersion()
                .singleResult();
        if (definition == null) {
            throw new RuntimeException("部署成功但无法查询到流程定义");
        }
        log.info("流程定义部署成功: id={}, key={}, version={}", definition.getId(), definition.getKey(), definition.getVersion());
        return convertToVO(definition);
    }

    /// 将 ProcessDefinition 转换为 ProcessDefinitionVO
    private ProcessDefinitionVO convertToVO(ProcessDefinition definition) {
        ProcessDefinitionVO vo = new ProcessDefinitionVO();
        vo.setId(definition.getId());
        vo.setKey(definition.getKey());
        vo.setName(definition.getName());
        vo.setVersion(definition.getVersion());
        vo.setDeploymentId(definition.getDeploymentId());
        vo.setResourceName(definition.getResourceName());
        vo.setSuspended(definition.isSuspended());
        vo.setDescription(definition.getDescription());
        vo.setCategory(definition.getCategory());

        // 获取部署时间
        if (definition.getDeploymentId() != null) {
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId())
                    .singleResult();
            if (deployment != null && deployment.getDeploymentTime() != null) {
                vo.setDeploymentTime(deployment.getDeploymentTime().toString());
            }
        }

        return vo;
    }
}
