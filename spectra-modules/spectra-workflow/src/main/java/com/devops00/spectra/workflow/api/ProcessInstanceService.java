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

    String start(String processDefinitionKey, String businessKey);

    String start(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    ProcessInstanceVO getStatus(String processInstanceId);

    Map<String, Object> getVariables(String processInstanceId);

    void terminate(String processInstanceId, String reason);

    byte[] getDiagram(String processInstanceId);
}
