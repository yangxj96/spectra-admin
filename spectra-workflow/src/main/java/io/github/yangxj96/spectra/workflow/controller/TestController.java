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

import cn.dev33.satoken.annotation.SaIgnore;
import io.github.yangxj96.spectra.workflow.service.WorkflowService;
import jakarta.annotation.Resource;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 */
@SaIgnore
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private WorkflowService workflowService;

    @GetMapping("/getWorkflows")
    public List<Map<String, Object>> getWorkflows() {
        var result = new ArrayList<Map<String, Object>>();
        var workflows = workflowService.getWorkflows();
        // 打印结果
        for (ProcessDefinition pd : workflows) {
            var map = new HashMap<String, Object>();
            map.put("流程ID: ", pd.getId());
            map.put("流程Key: ", pd.getKey());
            map.put("流程名称: ", pd.getName());
            map.put("流程版本: ", pd.getVersion());
            map.put("部署ID: ", pd.getDeploymentId());
            map.put("资源名称: ", pd.getResourceName()); // 如 holiday-request.bpmn20.xml
            map.put("是否挂起: ", (pd.isSuspended() ? "是" : "否"));
            result.add(map);
        }
        return result;
    }

}
