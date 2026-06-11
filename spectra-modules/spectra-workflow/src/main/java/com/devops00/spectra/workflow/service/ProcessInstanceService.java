package com.devops00.spectra.workflow.service;

/// 流程实例Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 15:14
public interface ProcessInstanceService {

    /// 启动一个流程
    ///
    /// @param processDefinitionKey 流程定义的KEY
    /// @param businessKey          业务KEY
    /// @return 流程ID
    String start(String processDefinitionKey, String businessKey);

}
