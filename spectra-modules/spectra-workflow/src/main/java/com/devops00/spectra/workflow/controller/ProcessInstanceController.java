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

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.workflow.javabean.from.ProcessInstanceTerminateFrom;
import com.devops00.spectra.workflow.javabean.from.ProcessInstanceStartFrom;
import com.devops00.spectra.workflow.javabean.vo.ProcessInstanceVO;
import com.devops00.spectra.workflow.service.ProcessInstanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// 工作流-流程实例
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Slf4j
@RestController
@RequestMapping("/workflow/process-instances")
@RequiredArgsConstructor
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    /// 启动流程（带变量）
    ///
    /// @param from 启动参数
    /// @return 流程实例ID
    @ULog("'启动流程'")
    @PostMapping(value = "/start", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_INSTANCE:INSERT')")
    public String start(@Validated @RequestBody ProcessInstanceStartFrom from) {
        return processInstanceService.start(from.getProcessDefinitionKey(), from.getBusinessKey(), from.getVariables());
    }

    /// 查询流程状态
    ///
    /// @param id 流程实例ID
    /// @return 流程实例信息
    @ULog("'查询流程实例状态'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_INSTANCE:QUERY')")
    public ProcessInstanceVO getStatus(@PathVariable String id) {
        return processInstanceService.getStatus(id);
    }

    /// 获取流程变量
    ///
    /// @param id 流程实例ID
    /// @return 流程变量
    @ULog("'查询流程变量'")
    @GetMapping(value = "/{id}/variables", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_INSTANCE:QUERY')")
    public Map<String, Object> getVariables(@PathVariable String id) {
        return processInstanceService.getVariables(id);
    }

    /// 终止流程
    ///
    /// @param id   流程实例ID
    /// @param from 终止参数
    @ULog("'终止流程'")
    @PostMapping(value = "/{id}/terminate", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_INSTANCE:UPDATE')")
    public void terminate(@PathVariable String id, @RequestBody ProcessInstanceTerminateFrom from) {
        processInstanceService.terminate(id, from.getReason());
    }

    /// 获取流程图（高亮当前节点）
    ///
    /// @param id 流程实例ID
    /// @return 流程图图片（PNG格式）
    @ULog("'获取流程实例图'")
    @GetMapping(value = "/{id}/diagram", version = "1.0.0+", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasPermission(null, 'WF_INSTANCE:QUERY')")
    public byte[] getDiagram(@PathVariable String id) {
        return processInstanceService.getDiagram(id);
    }
}
