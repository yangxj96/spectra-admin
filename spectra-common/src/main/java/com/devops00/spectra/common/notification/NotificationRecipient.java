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

import java.util.UUID;

/**
 * 通知模块使用的最小收件人快照，隔离 Core 账号实体和其他业务字段。
 *
 * @param userId   系统用户 ID
 * @param phone    用于短信投递的手机号；未绑定时为空
 * @param email    用于邮件投递的邮箱地址；未绑定时为空
 * @param active   用户是否存在可用的活跃账号
 * @param verified 收件地址是否已通过账号验证
 * @param timezone 用户时区 ID；为空时由通知模块按 UTC 处理
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record NotificationRecipient(UUID userId, String phone, String email, boolean active, boolean verified,
                                    String timezone) {

    /**
     * 返回指定外部渠道的已验证地址。用户不可用、地址未验证或渠道为站内信时返回空。
     *
     * @param channel 待解析地址的通知渠道
     * @return 手机号或邮箱地址；没有可用地址时返回 {@code null}
     */
    public String addressFor(NotificationChannel channel) {
        if (!active || !verified || channel == null) {
            return null;
        }
        return switch (channel) {
            case SMS -> phone;
            case EMAIL -> email;
            case IN_APP -> null;
        };
    }
}
