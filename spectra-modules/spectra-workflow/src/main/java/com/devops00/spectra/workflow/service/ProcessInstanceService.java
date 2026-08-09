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

package com.devops00.spectra.workflow.service;

import com.devops00.spectra.workflow.javabean.vo.ProcessInstanceVO;

import java.util.Map;

/**
 * 流程实例Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 15:14
 */
public interface ProcessInstanceService {

    /**
     * 启动一个流程
     *
     * @param processDefinitionKey
     *            流程定义的KEY
     * @param businessKey
     *            业务KEY
     * @return 流程ID
     */
    String start(String processDefinitionKey, String businessKey);

    /**
     * 启动一个流程（带变量）
     *
     * @param processDefinitionKey
     *            流程定义的KEY
     * @param businessKey
     *            业务KEY
     * @param variables
     *            流程变量
     * @return 流程ID
     */
    String start(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * 查询流程状态
     *
     * @param processInstanceId
     *            流程实例ID
     * @return 流程实例信息
     */
    ProcessInstanceVO getStatus(String processInstanceId);

    /**
     * 获取流程变量
     *
     * @param processInstanceId
     *            流程实例ID
     * @return 流程变量
     */
    Map<String, Object> getVariables(String processInstanceId);

    /**
     * 终止流程
     *
     * @param processInstanceId
     *            流程实例ID
     * @param reason
     *            终止原因
     */
    void terminate(String processInstanceId, String reason);

    /**
     * 获取流程图（高亮当前节点）
     *
     * @param processInstanceId
     *            流程实例ID
     * @return 流程图图片（PNG格式）
     */
    byte[] getDiagram(String processInstanceId);
}
