/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.workflow.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.workflow.javabean.vo.TaskVO;

/** 工作流待办任务公共调用端口。 */
public interface TaskService {

    IPage<TaskVO> todo(PageFrom page, String assignee, String processDefinitionKey);

    IPage<TaskVO> done(PageFrom page, String assignee, String processDefinitionKey);

    void complete(String taskId, String comment, String operator);

    void reject(String taskId, String comment, String operator);

    void transfer(String taskId, String targetUserId, String operator);

    void delegate(String taskId, String targetUserId, String operator);

    boolean canAccessProcess(String processInstanceId, String username);
}
