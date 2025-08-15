package com.yangxj96.spectra.workflow.service.impl;

import com.yangxj96.spectra.workflow.service.WorkflowService;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作流业务层实现
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private RepositoryService repositoryService;

    @Override
    public List<ProcessDefinition> getWorkflows() {
        return repositoryService
                .createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey().asc()
                .list();
    }

}

