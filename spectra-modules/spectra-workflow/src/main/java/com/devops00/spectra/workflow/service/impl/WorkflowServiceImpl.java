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

import com.devops00.spectra.workflow.service.ApprovalCallback;
import com.devops00.spectra.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流业务层实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    /**
     * 回调注册表：流程定义KEY -> 回调实现
     */
    private final Map<String, ApprovalCallback> callbackRegistry = new ConcurrentHashMap<>();

    @Override
    public void registerCallback(String processDefinitionKey, ApprovalCallback callback) {
        callbackRegistry.put(processDefinitionKey, callback);
        log.info("审批回调已注册: processDefinitionKey={}", processDefinitionKey);
    }

    @Override
    public ApprovalCallback getCallback(String processDefinitionKey) {
        return callbackRegistry.get(processDefinitionKey);
    }
}
