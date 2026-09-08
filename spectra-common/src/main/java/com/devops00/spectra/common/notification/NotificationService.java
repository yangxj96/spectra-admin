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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 业务模块使用的快捷通知服务。
 *
 * <p>调用方只需要组装模板、渠道、收件人和参数；通知模块负责转换为统一网关请求并执行后续投递流程。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public interface NotificationService {

    /**
     * 按用户和多个渠道快捷发送通知；单渠道和多渠道统一走此入口。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  收件用户 ID
     * @param channels          投递渠道，可传一个或多个
     * @param templateGroupCode 模板组编码
     * @param parameters        用于渲染标题、正文和链接的普通模板参数，不应放入密码、验证码等敏感值。
     * @return 返回包含幂等键和请求标识的通知入队回执；已存在相同幂等键时返回原有回执，校验或入队失败时抛出异常，不返回 null。
     */
    default NotificationReceipt sendToUsers(String idempotencyKey, NotificationPurpose purpose,
                                            Collection<UUID> recipientUserIds,
                                            Collection<NotificationChannel> channels, String templateGroupCode,
                                            Map<String, ?> parameters) {
        return send(NotificationSendRequest.builder()
                .idempotencyKey(idempotencyKey)
                .purpose(purpose)
                .recipientUserIds(recipientUserIds)
                .channels(channels)
                .templateGroupCode(templateGroupCode)
                .parameters(parameters)
                .build());
    }

    /**
     * 快捷发送站内信。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  收件用户 ID
     * @param templateGroupCode 模板组编码
     * @param parameters        用于渲染站内信标题、正文和链接的普通模板参数，不应放入敏感值。
     * @return 返回站内信的通知入队回执；已存在相同幂等键时返回原有回执，校验或入队失败时抛出异常，不返回 null。
     */
    default NotificationReceipt sendInApp(String idempotencyKey, NotificationPurpose purpose,
                                          Collection<UUID> recipientUserIds, String templateGroupCode,
                                          Map<String, ?> parameters) {
        return sendToUsers(idempotencyKey, purpose, recipientUserIds, List.of(NotificationChannel.IN_APP),
                templateGroupCode, parameters);
    }

    /**
     * 快捷发送短信给已登记并通过验证的用户。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  收件用户 ID
     * @param templateGroupCode 模板组编码
     * @param parameters        用于渲染短信正文的普通模板参数，不应放入未受控的敏感值。
     * @return 返回短信通知的入队回执；收件人没有已验证手机号或入队失败时抛出异常，不返回 null。
     */
    default NotificationReceipt sendSms(String idempotencyKey, NotificationPurpose purpose,
                                        Collection<UUID> recipientUserIds, String templateGroupCode,
                                        Map<String, ?> parameters) {
        return sendToUsers(idempotencyKey, purpose, recipientUserIds, List.of(NotificationChannel.SMS),
                templateGroupCode, parameters);
    }

    /**
     * 快捷发送邮件给已登记并通过验证的用户。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  收件用户 ID
     * @param templateGroupCode 模板组编码
     * @param parameters        用于渲染邮件标题和正文的普通模板参数，不应放入未受控的敏感值。
     * @return 返回邮件通知的入队回执；收件人没有已验证邮箱或入队失败时抛出异常，不返回 null。
     */
    default NotificationReceipt sendEmail(String idempotencyKey, NotificationPurpose purpose,
                                          Collection<UUID> recipientUserIds, String templateGroupCode,
                                          Map<String, ?> parameters) {
        return sendToUsers(idempotencyKey, purpose, recipientUserIds, List.of(NotificationChannel.EMAIL),
                templateGroupCode, parameters);
    }

    /**
     * 按多个直接地址快捷发送安全通知，例如验证码。
     *
     * @param idempotencyKey      业务幂等键
     * @param purpose             通知用途
     * @param directAddresses     渠道与直接地址，可传一个或多个
     * @param templateGroupCode   模板组编码
     * @param parameters          用于渲染标题、正文和链接的普通模板参数。
     * @param sensitiveParameters 仅供受控模板渲染使用的敏感参数；不会作为普通通知字段记录或写入日志。
     * @return 返回按直接地址生成的通知入队回执；地址或模板校验失败时抛出异常，不返回 null。
     */
    default NotificationReceipt sendDirect(String idempotencyKey, NotificationPurpose purpose,
                                           Collection<NotificationDirectAddress> directAddresses,
                                           String templateGroupCode, Map<String, ?> parameters,
                                           Map<String, ?> sensitiveParameters) {
        return send(NotificationSendRequest.direct(idempotencyKey, purpose, directAddresses, templateGroupCode)
                .parameters(parameters)
                .sensitiveParameters(sensitiveParameters)
                .build());
    }

    /**
     * 发送通知并返回入队回执。
     *
     * @param request 快捷通知请求
     * @return 返回统一通知网关生成的入队回执，包含请求标识和幂等处理结果；请求校验或入队失败时抛出异常，不返回 null。
     */
    NotificationReceipt send(NotificationSendRequest request);
}
