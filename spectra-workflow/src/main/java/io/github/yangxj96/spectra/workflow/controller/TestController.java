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
import io.github.yangxj96.spectra.workflow.javabean.vo.ProcessDefinitionVO;
import io.github.yangxj96.spectra.workflow.service.WorkflowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<ProcessDefinitionVO> getWorkflows() {
        return workflowService.getWorkflows();
    }

}
