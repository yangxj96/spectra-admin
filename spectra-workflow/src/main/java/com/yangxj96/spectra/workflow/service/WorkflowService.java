package com.yangxj96.spectra.workflow.service;

import org.flowable.engine.repository.ProcessDefinition;

import java.util.List;

/**
 * 工作流业务层
 */
public interface WorkflowService {


    /**
     * 获取所有可执行的工作流
     *
     * @return 流程定义列表
     */
    List<ProcessDefinition> getWorkflows();

}
