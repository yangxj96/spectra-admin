/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.oa.workbench.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.notification.service.NotificationService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.application.javabean.constant.ApplicationStatus;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.oa.workbench.javabean.vo.WorkbenchSummaryVO;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.workflow.service.TaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// OA 工作台摘要接口。复用现有 Dashboard，不新增工作台页面路由。
@Slf4j
@RestController
@RequestMapping("/oa/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final ApplicationService applicationService;
    private final TaskService taskService;
    private final NotificationService notificationService;

    @ULog("'查询 OA 工作台摘要'")
    @GetMapping(value = "/summary", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_WORKBENCH:QUERY')")
    public WorkbenchSummaryVO summary() {
        var result = new WorkbenchSummaryVO();
        var page = new PageFrom();
        page.setPageSize(1L);
        page.setPageNum(1L);
        result.setTodoCount(taskService.todo(page, SecUtil.getCurrentUsername()).getTotal());
        var userId = SecUtil.getCurrentUserId();
        result.setUnreadNotificationCount(userId == null ? 0 : notificationService.getUnreadCount(userId));
        result.setDraftCount(applicationService.countMine(ApplicationStatus.DRAFT.name()));
        result.setInReviewCount(applicationService.countMine(ApplicationStatus.IN_REVIEW.name()));
        result.setApprovedCount(applicationService.countMine(ApplicationStatus.APPROVED.name()));
        result.setRejectedCount(applicationService.countMine(ApplicationStatus.REJECTED.name()));
        return result;
    }
}
