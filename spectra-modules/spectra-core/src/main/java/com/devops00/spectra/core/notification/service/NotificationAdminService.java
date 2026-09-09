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
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
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
     *
     * @param from 统计时间窗口，包含要汇总的小时数；窗口用于限定请求、任务和投递的运行指标。
     * @return 返回通知请求、任务、投递数量及失败原因组成的运行概览；统计失败时抛出异常，不返回 null。
     */
    NotificationOverviewVO overview(NotificationOverviewFrom from);

    /**
     * 查询逻辑通知请求详情摘要。
     *
     * @param requestId 通知请求的唯一标识。
     * @return 返回通知请求的幂等键、用途、收件人数、渠道和当前状态摘要；请求不存在时抛出业务异常，不返回 null。
     */
    NotificationRequestAdminVO getRequest(UUID requestId);

    /**
     * 查询通知投递任务详情摘要。
     *
     * @param taskId 通知投递任务的唯一标识。
     * @return 返回通知任务的渠道、状态、重试次数和计划时间摘要；任务不存在时抛出业务异常，不返回 null。
     */
    NotificationTaskAdminVO getTask(UUID taskId);

    /**
     * 查询通知投递记录详情摘要。
     *
     * @param deliveryId 通知投递记录的唯一标识。
     * @return 返回通知投递记录的收件地址脱敏值、渠道、投递状态和失败原因摘要；记录不存在时抛出业务异常，不返回 null。
     */
    NotificationDeliveryAdminVO getDelivery(UUID deliveryId);

    /**
     * 查询通知渠道是否已配置并可投递。
     *
     * @param channel 通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @return 返回指定渠道是否已配置且可投递的状态；渠道未配置或不可用时返回对应的不可用状态，不返回 null。
     */
    NotificationChannelAvailability availability(NotificationChannel channel);

    /**
     * 查询逻辑通知请求。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 请求时间、收件用户、状态、渠道、用途和业务关联键等通知请求筛选条件。
     * @return 返回通知请求的幂等键、用途、收件人数、渠道和状态分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationRequestAdminVO> pageRequests(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 查询投递任务。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 请求时间、渠道、状态和业务关联键等通知任务筛选条件。
     * @return 返回通知任务的渠道、状态、重试次数和计划时间分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationTaskAdminVO> pageTasks(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 查询渠道投递记录。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 请求时间、投递任务、收件用户、渠道、状态和业务关联键等投递记录筛选条件。
     * @return 返回投递地址脱敏值、渠道、投递状态和失败原因分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NotificationDeliveryAdminVO> pageDeliveries(PageFrom page, NotificationAdminQueryFrom params);

    /**
     * 显式重试失败或未知任务。
     *
     * @param taskId 通知投递任务的唯一标识。
     */
    void retry(UUID taskId);

    /**
     * 取消尚未完成的任务。
     *
     * @param taskId 通知投递任务的唯一标识。
     */
    void cancel(UUID taskId);
}
