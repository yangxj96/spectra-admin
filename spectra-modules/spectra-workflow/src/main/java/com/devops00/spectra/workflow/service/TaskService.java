/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.workflow.javabean.vo.TaskVO;

/// 任务管理Service
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
public interface TaskService {

    /// 查询待办任务
    ///
    /// @param page     分页参数
    /// @param assignee 任务处理人用户名
    /// @return 待办任务分页列表
    IPage<TaskVO> todo(PageFrom page, String assignee);

    /// 查询已办任务
    ///
    /// @param page     分页参数
    /// @param assignee 任务处理人用户名
    /// @return 已办任务分页列表
    IPage<TaskVO> done(PageFrom page, String assignee);

    /// 完成任务（审批通过）
    ///
    /// @param taskId  任务ID
    /// @param comment 审批意见
    void complete(String taskId, String comment);

    /// 驳回任务
    ///
    /// @param taskId  任务ID
    /// @param comment 驳回意见
    void reject(String taskId, String comment);

    /// 转办任务
    ///
    /// @param taskId       任务ID
    /// @param targetUserId 目标用户用户名
    void transfer(String taskId, String targetUserId);

    /// 委派任务
    ///
    /// @param taskId       任务ID
    /// @param targetUserId 目标用户用户名
    void delegate(String taskId, String targetUserId);
}
