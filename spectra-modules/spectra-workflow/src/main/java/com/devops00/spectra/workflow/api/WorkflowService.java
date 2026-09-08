/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.workflow.api;

/** 工作流回调注册公共端口。 */
public interface WorkflowService {

    /**
     * 登记流程回调配置。
     *
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @param callback             要绑定到流程定义的审批回调实现；流程节点完成后由它接收业务处理通知。
     */
    void registerCallback(String processDefinitionKey, ApprovalCallback callback);

    /**
     * 读取流程回调配置。
     *
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @return 返回指定流程定义登记的审批回调；未登记回调时返回 null。
     */
    ApprovalCallback getCallback(String processDefinitionKey);
}
