/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.workflow.api;

/** 工作流回调注册公共端口。 */
public interface WorkflowService {

    void registerCallback(String processDefinitionKey, ApprovalCallback callback);

    ApprovalCallback getCallback(String processDefinitionKey);
}
