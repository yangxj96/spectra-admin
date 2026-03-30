package com.devops00.spectra.workflow.service.impl;


import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

/// 流程实例Service实现
///
/// @author Jack Young
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
