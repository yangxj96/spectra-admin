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

    /**
     * 查询当前用户待办理任务。
     *
     * @param page                 分页条件，包含页码、页大小和排序字段。
     * @param assignee             任务的目标办理人。
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @return 返回当前用户待办理任务分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<TaskVO> todo(PageFrom page, String assignee, String processDefinitionKey);

    /**
     * 查询当前用户已办理任务。
     *
     * @param page                 分页条件，包含页码、页大小和排序字段。
     * @param assignee             任务的目标办理人。
     * @param processDefinitionKey 流程定义键，用于选择要启动的流程模型。
     * @return 返回当前用户已办理任务分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<TaskVO> done(PageFrom page, String assignee, String processDefinitionKey);

    /**
     * 提交任务办理结果并推进流程。
     *
     * @param taskId   待完成工作流任务的唯一标识。
     * @param comment  流程处理意见，将作为审批轨迹的一部分保存。
     * @param operator 当前操作人标识，用于记录审计主体。
     */
    void complete(String taskId, String comment, String operator);

    /**
     * 驳回任务并写入审批意见。
     *
     * @param taskId   待驳回工作流任务的唯一标识。
     * @param comment  流程处理意见，将作为审批轨迹的一部分保存。
     * @param operator 当前操作人标识，用于记录审计主体。
     */
    void reject(String taskId, String comment, String operator);

    /**
     * 将任务转交给指定办理人。
     *
     * @param taskId       待转交工作流任务的唯一标识。
     * @param targetUserId 接收转交任务的目标办理人标识。
     * @param operator     当前操作人标识，用于记录审计主体。
     */
    void transfer(String taskId, String targetUserId, String operator);

    /**
     * 将任务委派给指定办理人。
     *
     * @param taskId       待委派工作流任务的唯一标识。
     * @param targetUserId 接收委派任务的目标办理人标识。
     * @param operator     当前操作人标识，用于记录审计主体。
     */
    void delegate(String taskId, String targetUserId, String operator);

    /**
     * 判断当前用户是否可以访问流程实例。
     *
     * @param processInstanceId 流程实例的唯一标识。
     * @param username          登录用户名，用于查询用户身份资料。
     * @return 返回用户是否可以访问指定流程实例；流程实例不存在、用户名为空或用户不在流程参与范围内时返回 false。
     */
    boolean canAccessProcess(String processInstanceId, String username);
}
