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


import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

/// 流程实例Service实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 15:15
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final RuntimeService runtimeService;

    @Override
    public String start(String processDefinitionKey, String businessKey) {
        try {
            // 1. 防重复启动
            long count = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey)
                    .count();

            if (count > 0) {
                throw new IllegalStateException("流程已存在: " + businessKey);
            }

            // 2. 启动流程
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey,
                    businessKey
            );

            return instance.getId();

        } catch (Exception e) {
            // 3. 统一异常
            throw new RuntimeException("启动流程失败: " + e.getMessage(), e);
        }
    }
}
