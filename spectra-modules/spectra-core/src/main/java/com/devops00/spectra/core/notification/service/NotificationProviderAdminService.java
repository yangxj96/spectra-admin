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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationProviderVO;

import java.util.List;

/**
 * 通知 Provider 配置管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationProviderAdminService {

    /**
     * 查询所有渠道的脱敏配置。
     *
     * @return 返回符合查询条件的通知 Provider 配置列表；无匹配时返回空列表，不返回 null。
     */
    List<NotificationProviderVO> list();

    /**
     * 查询指定渠道的脱敏配置。
     *
     * @param channel 通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @return 返回符合条件的通知 Provider 配置详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    NotificationProviderVO get(NotificationChannel channel);

    /**
     * 读取 Provider 运行时配置；Secret 仅供 Provider 内部使用。
     *
     * @param channel 通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @return 返回指定渠道的运行时 Provider 配置；配置不存在或渠道未启用时抛出业务异常，Secret 字段可能为 null，且不返回管理端脱敏视图。
     */
    NotificationProviderConfiguration resolve(NotificationChannel channel);

    /**
     * 保存指定渠道配置。
     *
     * @param channel 通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @param params  渠道启用状态、发件人/签名、Endpoint、模板配置和密钥等 Provider 字段；密钥不会写入日志或管理端响应。
     * @return 返回保存后的渠道脱敏配置，包含可用状态而不包含明文 Secret；校验或写入失败时抛出业务异常，不返回 null。
     */
    NotificationProviderVO modify(NotificationChannel channel, NotificationProviderSaveFrom params);
}
