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

package com.devops00.spectra.core.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.core.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationOverviewFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationOverviewVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTaskAdminVO;

import java.util.UUID;

/**
 * 通知管理端查询和运维服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationAdminService {

    /**
     * 查询通知运行概览。
     */
    NotificationOverviewVO overview(NotificationOverviewFrom from);

    /**
     * 查询逻辑通知请求详情摘要。
     */
    NotificationRequestAdminVO getRequest(UUID requestId);

    /**
     * 查询通知投递任务详情摘要。
     */
    NotificationTaskAdminVO getTask(UUID taskId);

    /**
     * 查询通知投递记录详情摘要。
     */
    NotificationDeliveryAdminVO getDelivery(UUID deliveryId);

    /**
     * 查询通知渠道是否已配置并可投递。
     */
    NotificationChannelAvailability availability(NotificationChannel channel);

    /**
     * 查询逻辑通知请求。
     */
    IPage<NotificationRequestAdminVO> pageRequests(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 查询投递任务。
     */
    IPage<NotificationTaskAdminVO> pageTasks(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 查询渠道投递记录。
     */
    IPage<NotificationDeliveryAdminVO> pageDeliveries(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 显式重试失败或未知任务。
     */
    void retry(UUID taskId);

    /**
     * 取消尚未完成的任务。
     */
    void cancel(UUID taskId);
}
