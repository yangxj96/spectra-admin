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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// 工作流-流程定义
///
/// 面向“设计器 + 运维”
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


    /*
    职责
    流程定义 CRUD
    发布 / 版本管理
    导入导出（BPMN / JSON）
    激活 / 挂起
    典型接口
    POST   /process-definitions
    PUT    /process-definitions/{id}
    GET    /process-definitions/{id}
    GET    /process-definitions
    DELETE /process-definitions/{id}

    POST   /process-definitions/{id}/deploy
    POST   /process-definitions/{id}/activate
    POST   /process-definitions/{id}/suspend
     */

    /// 获取所有的流程定义
    ///
    /// @return 流程定义列表
    @GetMapping("/definitions", version = "1.0.0+")
    public List<Map<String, Object>> definitions() {
        var definitions = repositoryService
                .createProcessDefinitionQuery()
                .list();
        var result = new ArrayList<Map<String, Object>>();
        for (var definition : definitions) {
            var m = new HashMap<String, Object>();
            m.put("id", definition.getId());
            m.put("deploymentId", definition.getDeploymentId());
            m.put("description", definition.getDescription());
            m.put("key", definition.getKey());
            m.put("name", definition.getName());
            m.put("ver", definition.getVersion());
            result.add(m);
        }
        return result;
    }


}
