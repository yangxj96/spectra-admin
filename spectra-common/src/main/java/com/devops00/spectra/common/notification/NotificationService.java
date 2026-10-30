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

package com.devops00.spectra.common.notification;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * 业务模块使用的同步通知服务。
 *
 * <p>调用方提供模板编码、渠道、收件人和模板参数，通知模块同步完成渲染、发送和历史记录。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationService {

    /**
     * 同步发送通知并返回逐目标结果。
     *
     * @param request 通知请求
     * @return 发送回执
     */
    NotificationReceipt send(NotificationSendRequest request);

    /**
     * 向用户发送站内信。
     *
     * @param templateCode     模板编码
     * @param recipientUserIds 收件用户 ID
     * @param parameters       模板参数
     * @return 发送回执
     */
    default NotificationReceipt sendInApp(String templateCode, Collection<UUID> recipientUserIds,
                                          Map<String, ?> parameters) {
        return send(NotificationSendRequest.inApp(templateCode, recipientUserIds)
                .parameters(parameters)
                .build());
    }

    /**
     * 向用户发送短信。
     *
     * @param templateCode     模板编码
     * @param recipientUserIds 收件用户 ID
     * @param parameters       模板参数
     * @return 发送回执
     */
    default NotificationReceipt sendSms(String templateCode, Collection<UUID> recipientUserIds,
                                        Map<String, ?> parameters) {
        return send(NotificationSendRequest.builder()
                .templateCode(templateCode)
                .channels(NotificationChannel.SMS)
                .recipientUserIds(recipientUserIds)
                .parameters(parameters)
                .build());
    }

    /**
     * 向用户发送邮件。
     *
     * @param templateCode     模板编码
     * @param recipientUserIds 收件用户 ID
     * @param parameters       模板参数
     * @return 发送回执
     */
    default NotificationReceipt sendEmail(String templateCode, Collection<UUID> recipientUserIds,
                                          Map<String, ?> parameters) {
        return send(NotificationSendRequest.builder()
                .templateCode(templateCode)
                .channels(NotificationChannel.EMAIL)
                .recipientUserIds(recipientUserIds)
                .parameters(parameters)
                .build());
    }

    /**
     * 向用户发送多个渠道的通知。
     *
     * @param templateCode     模板编码
     * @param channels         发送渠道
     * @param recipientUserIds 收件用户 ID
     * @param parameters       模板参数
     * @return 发送回执
     */
    default NotificationReceipt sendToUsers(String templateCode, Collection<NotificationChannel> channels,
                                            Collection<UUID> recipientUserIds, Map<String, ?> parameters) {
        return send(NotificationSendRequest.builder()
                .templateCode(templateCode)
                .channels(channels)
                .recipientUserIds(recipientUserIds)
                .parameters(parameters)
                .build());
    }

    /**
     * 向直接地址发送通知，未登录安全场景使用。
     *
     * @param templateCode        模板编码
     * @param directAddresses     直接地址
     * @param parameters          普通模板参数
     * @param sensitiveParameters 敏感模板参数
     * @return 发送回执
     */
    default NotificationReceipt sendDirect(String templateCode,
                                           Collection<NotificationDirectAddress> directAddresses,
                                           Map<String, ?> parameters, Map<String, ?> sensitiveParameters) {
        return send(NotificationSendRequest.direct(templateCode, directAddresses)
                .parameters(parameters)
                .sensitiveParameters(sensitiveParameters)
                .build());
    }
}
