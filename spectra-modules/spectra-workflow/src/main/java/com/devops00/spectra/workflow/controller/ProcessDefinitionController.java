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

import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.workflow.javabean.from.DeployProcessFrom;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionResourceVO;
import com.devops00.spectra.workflow.javabean.vo.ProcessDefinitionVO;
import com.devops00.spectra.workflow.service.ProcessDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.xss.core.XssCleanIgnore;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private final ProcessDefinitionService processDefinitionService;

    /// 获取所有的流程定义
    @ULog("'查询流程定义列表'")
    @GetMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:QUERY')")
    public List<ProcessDefinitionVO> definitions() {
        return processDefinitionService.listAll();
    }

    /// 获取流程定义详情
    @ULog("'查询流程定义详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:QUERY')")
    public ProcessDefinitionVO definitionDetail(@PathVariable String id) {
        return processDefinitionService.getDetail(id);
    }

    /// 获取流程定义图
    @ULog("'获取流程定义图'")
    @GetMapping(value = "/{id}/diagram", version = "1.0.0+", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:QUERY')")
    public byte[] getDiagram(@PathVariable String id) {
        return processDefinitionService.getDiagram(id);
    }

    /// 挂起流程定义
    @ULog("'挂起流程定义'")
    @PostMapping(value = "/{id}/suspend", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:UPDATE')")
    public void suspend(@PathVariable String id) {
        processDefinitionService.suspend(id);
    }

    /// 激活流程定义
    @ULog("'激活流程定义'")
    @PostMapping(value = "/{id}/activate", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:UPDATE')")
    public void activate(@PathVariable String id) {
        processDefinitionService.activate(id);
    }

    /// 获取流程定义的 BPMN XML 源码
    @ULog("'获取流程定义BPMN资源'")
    @GetMapping(value = "/{id}/resource", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:QUERY')")
    public ProcessDefinitionResourceVO getResource(@PathVariable String id) {
        return processDefinitionService.getResource(id);
    }

    /// 部署流程定义（新增或更新版本）
    @XssCleanIgnore
    @ULog("'部署流程定义'")
    @PostMapping(value = "/deploy", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_PROCESS:INSERT')")
    public ProcessDefinitionVO deploy(@RequestBody @Valid DeployProcessFrom from) {
        return processDefinitionService.deploy(from);
    }
}
