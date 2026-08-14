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

package com.devops00.spectra.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTaskAdminVO;
import com.devops00.spectra.notification.service.NotificationAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 通知管理端接口；仅开放脱敏查询和受控运维操作。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/admin")
public class NotificationAdminController {

    /**
     * 通知管理端服务。
     */
    private final NotificationAdminService service;

    /**
     * 查询渠道健康状态；仅返回是否可用及脱敏原因。
     */
    @ULog("'查询通知渠道状态'")
    @GetMapping(value = "/channels/{channel}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public NotificationChannelAvailability availability(@PathVariable NotificationChannel channel) {
        return service.availability(channel);
    }

    /**
     * 查询逻辑通知请求。
     */
    @ULog("'查询通知请求'")
    @GetMapping(value = "/requests", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public IPage<NotificationRequestAdminVO> pageRequests(PageFrom page, NotificationAdminQueryFrom params) {
        return service.pageRequests(page, params);
    }

    /**
     * 查询通知投递任务。
     */
    @ULog("'查询通知任务'")
    @GetMapping(value = "/tasks", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public IPage<NotificationTaskAdminVO> pageTasks(PageFrom page, NotificationAdminQueryFrom params) {
        return service.pageTasks(page, params);
    }

    /**
     * 查询通知投递记录。
     */
    @ULog("'查询通知投递记录'")
    @GetMapping(value = "/deliveries", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public IPage<NotificationDeliveryAdminVO> pageDeliveries(PageFrom page, NotificationAdminQueryFrom params) {
        return service.pageDeliveries(page, params);
    }

    /**
     * 重新排队失败通知任务。
     */
    @ULog("'重试通知任务'")
    @PostMapping(value = "/tasks/{id}/retry", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:retry')")
    public void retry(@PathVariable UUID id) {
        service.retry(id);
    }

    /**
     * 取消尚未完成的通知任务。
     */
    @ULog("'取消通知任务'")
    @DeleteMapping(value = "/tasks/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'notification:admin:cancel')")
    public void cancel(@PathVariable UUID id) {
        service.cancel(id);
    }
}
