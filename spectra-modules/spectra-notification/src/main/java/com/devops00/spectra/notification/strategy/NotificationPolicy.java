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

package com.devops00.spectra.notification.strategy;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 通知用途策略；安全用途的强制性不能由用户偏好覆盖。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
public class NotificationPolicy {

    /**
     * 必须投递的认证和安全用途。
     */
    private static final Set<NotificationPurpose> MANDATORY = EnumSet.of(NotificationPurpose.LOGIN_CODE,
            NotificationPurpose.BIND_PHONE_CODE, NotificationPurpose.BIND_EMAIL_CODE,
            NotificationPurpose.RESET_PASSWORD_CODE, NotificationPurpose.SECURITY_ALERT);

    /**
     * 计算请求允许的显式渠道。
     */
    public List<NotificationChannel> resolve(NotificationPurpose purpose, List<NotificationChannel> requested) {
        if (purpose == null) {
            throw new DataSaveException("通知用途不能为空");
        }
        var channels = requested == null || requested.isEmpty()
                ? List.of(NotificationChannel.IN_APP)
                : requested.stream().distinct().toList();
        if (purpose == NotificationPurpose.SECURITY_ALERT && !channels.contains(NotificationChannel.IN_APP)) {
            throw new DataSaveException("安全告警必须包含站内信渠道");
        }
        if ((purpose == NotificationPurpose.BIND_PHONE_CODE
                || purpose == NotificationPurpose.LOGIN_CODE
                || purpose == NotificationPurpose.BIND_EMAIL_CODE
                || purpose == NotificationPurpose.RESET_PASSWORD_CODE)
                && channels.stream().noneMatch(channel -> channel == NotificationChannel.SMS || channel == NotificationChannel.EMAIL)) {
            throw new DataSaveException("验证码通知必须使用短信或邮件渠道");
        }
        return channels;
    }

    /**
     * 直接地址只允许用于认证和安全通知，避免普通业务绕过用户目录。
     */
    public boolean allowsDirectAddress(NotificationPurpose purpose) {
        return purpose == NotificationPurpose.LOGIN_CODE
                || purpose == NotificationPurpose.BIND_PHONE_CODE
                || purpose == NotificationPurpose.BIND_EMAIL_CODE
                || purpose == NotificationPurpose.RESET_PASSWORD_CODE;
    }

    /**
     * 强制安全用途始终投递，不受 enabled 或免打扰影响。
     */
    public boolean mandatory(NotificationPurpose purpose) {
        return MANDATORY.contains(purpose);
    }
}
