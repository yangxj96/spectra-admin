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
import com.devops00.spectra.oa.calendar.javabean.from.CalendarPageFrom;
import com.devops00.spectra.oa.calendar.service.CalendarService;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import com.devops00.spectra.oa.notice.javabean.from.NoticePageFrom;
import com.devops00.spectra.oa.notice.service.NoticeService;
import com.devops00.spectra.oa.workbench.javabean.vo.WorkbenchSummaryVO;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.workflow.service.TaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;

/// OA 工作台摘要接口。复用现有 Dashboard，不新增工作台页面路由。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Slf4j
@RestController
@RequestMapping("/oa/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final ApplicationService applicationService;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final NoticeService noticeService;
    private final CalendarService calendarService;
    private final MeetingService meetingService;

    /// 查询 OA 工作台摘要。
    @ULog("'查询 OA 工作台摘要'")
    @GetMapping(value = "/summary", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_WORKBENCH:QUERY')")
    public WorkbenchSummaryVO summary() {
        var result = new WorkbenchSummaryVO();
        try {
            var page = new PageFrom();
            page.setPageSize(1L);
            page.setPageNum(1L);
            result.setTodoCount(taskService.todo(page, SecUtil.getCurrentUsername(), null).getTotal());
        } catch (Exception exception) {
            log.warn("OA 工作台待办卡片加载失败", exception);
        }

        try {
            var userId = SecUtil.getCurrentUserId();
            result.setUnreadNotificationCount(userId == null ? 0 : notificationService.getUnreadCount(userId));
        } catch (Exception exception) {
            log.warn("OA 工作台消息卡片加载失败", exception);
        }

        try {
            result.setDraftCount(applicationService.countMine(ApplicationStatus.DRAFT.name()));
            result.setInReviewCount(applicationService.countMine(ApplicationStatus.IN_REVIEW.name()));
            result.setApprovedCount(applicationService.countMine(ApplicationStatus.APPROVED.name()));
            result.setRejectedCount(applicationService.countMine(ApplicationStatus.REJECTED.name()));
        } catch (Exception exception) {
            log.warn("OA 工作台申请统计卡片加载失败", exception);
        }
        try {
            var noticePage = new PageFrom();
            noticePage.setPageSize(20L);
            var noticeParams = new NoticePageFrom();
            noticeParams.setStatus("PUBLISHED");
            var notices = noticeService.page(noticePage, noticeParams);
            result.setNotices(notices.getRecords());
            result.setUnreadNoticeCount(notices.getRecords().stream().filter(notice -> !Boolean.TRUE.equals(notice.getRead())).count());
        } catch (Exception exception) {
            log.warn("OA 工作台公告卡片加载失败", exception);
        }

        try {
            var calendarPage = new PageFrom();
            calendarPage.setPageSize(20L);
            var today = LocalDate.now(ZoneOffset.UTC);
            var calendarParams = new CalendarPageFrom();
            calendarParams.setStartTime(today.atStartOfDay(ZoneOffset.UTC).toInstant().toString());
            calendarParams.setEndTime(today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString());
            var calendar = calendarService.page(calendarPage, calendarParams);
            result.setCalendarItems(calendar.getRecords());
            result.setTodayCalendarCount(calendar.getTotal());
        } catch (Exception exception) {
            log.warn("OA 工作台日程卡片加载失败", exception);
        }

        try {
            var meetingPage = new PageFrom();
            meetingPage.setPageSize(20L);
            var meeting = meetingService.page(meetingPage, new MeetingPageFrom());
            result.setMeetings(meeting.getRecords());
            result.setUpcomingMeetingCount(meeting.getTotal());
        } catch (Exception exception) {
            log.warn("OA 工作台会议卡片加载失败", exception);
        }
        return result;
    }
}
