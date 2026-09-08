/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.workflow.api;

import com.devops00.spectra.workflow.javabean.vo.ProcessInstanceVO;

import java.util.Map;

/** 流程实例公共调用端口。 */
public interface ProcessInstanceService {

    /**
     * 启动指定流程并返回流程实例标识。
     *
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @param businessKey          流程关联的业务键，用于定位业务单据。
     * @return 返回新建流程实例的唯一标识字符串；流程定义或启动参数无效时抛出业务异常，不返回 null 或空字符串。
     */
    String start(String processDefinitionKey, String businessKey);

    /**
     * 启动指定流程并返回流程实例标识。
     *
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @param businessKey          流程关联的业务键，用于定位业务单据。
     * @param variables            流程启动或任务处理时使用的流程变量。
     * @return 返回新建流程实例的唯一标识字符串；流程定义或启动参数无效时抛出业务异常，不返回 null 或空字符串。
     */
    String start(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * 查询流程实例当前状态。
     *
     * @param processInstanceId 流程实例的唯一标识。
     * @return 返回流程实例当前的状态、业务键和结束信息；实例不存在时抛出业务异常，不返回 null。
     */
    ProcessInstanceVO getStatus(String processInstanceId);

    /**
     * 读取流程实例变量。
     *
     * @param processInstanceId 流程实例的唯一标识。
     * @return 返回流程实例的变量名和值映射；实例没有流程变量时返回空 Map，不返回 null。
     */
    Map<String, Object> getVariables(String processInstanceId);

    /**
     * 终止指定流程实例。
     *
     * @param processInstanceId 流程实例的唯一标识。
     * @param reason            本次状态变更或撤销的业务原因。
     */
    void terminate(String processInstanceId, String reason);

    /**
     * 读取流程实例对应的流程图。
     *
     * @param processInstanceId 流程实例的唯一标识。
     * @return 返回流程实例当前 BPMN 图的 PNG 字节数组；实例不存在或图生成失败时抛出业务异常，不返回 null 或空数组。
     */
    byte[] getDiagram(String processInstanceId);
}
