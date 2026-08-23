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
import java.util.Locale;
import java.util.Map;
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
     * 验证码用途只能使用外部渠道，不能把验证码模板配置成仅能进入站内信的模板。
     */
    private static final Set<NotificationPurpose> VERIFICATION_CODE_PURPOSES = EnumSet.of(
            NotificationPurpose.LOGIN_CODE, NotificationPurpose.BIND_PHONE_CODE,
            NotificationPurpose.BIND_EMAIL_CODE, NotificationPurpose.RESET_PASSWORD_CODE);

    /**
     * 模板用途与渠道兼容矩阵。当前单体项目的普通通知允许三种渠道，验证码只允许短信和邮件。
     */
    private static final Map<NotificationPurpose, Set<NotificationChannel>> TEMPLATE_CHANNELS = buildTemplateChannels();

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
        if (VERIFICATION_CODE_PURPOSES.contains(purpose)
                && channels.stream().noneMatch(channel -> channel == NotificationChannel.SMS || channel == NotificationChannel.EMAIL)) {
            throw new DataSaveException("验证码通知必须使用短信或邮件渠道");
        }
        return channels;
    }

    /**
     * 解析通知用途，避免模板和运行请求分别接受不同的字符串形式。
     */
    public NotificationPurpose parsePurpose(String value) {
        if (value == null || value.isBlank()) {
            throw new DataSaveException("通知用途不能为空");
        }
        try {
            return NotificationPurpose.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("通知用途不合法");
        }
    }

    /**
     * 校验模板用途与渠道是否匹配。
     */
    public void validateTemplateChannel(NotificationPurpose purpose, NotificationChannel channel) {
        if (purpose == null) {
            throw new DataSaveException("通知用途不能为空");
        }
        if (channel == null || !templateChannels(purpose).contains(channel)) {
            throw new DataSaveException("通知模板用途与渠道不匹配");
        }
    }

    /**
     * 返回指定用途允许配置的模板渠道。
     */
    public Set<NotificationChannel> templateChannels(NotificationPurpose purpose) {
        if (purpose == null) {
            throw new DataSaveException("通知用途不能为空");
        }
        return TEMPLATE_CHANNELS.get(purpose);
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

    private static Map<NotificationPurpose, Set<NotificationChannel>> buildTemplateChannels() {
        var allChannels = Set.of(NotificationChannel.IN_APP, NotificationChannel.SMS, NotificationChannel.EMAIL);
        var externalChannels = Set.of(NotificationChannel.SMS, NotificationChannel.EMAIL);
        var matrix = new java.util.EnumMap<NotificationPurpose, Set<NotificationChannel>>(NotificationPurpose.class);
        for (var purpose : NotificationPurpose.values()) {
            matrix.put(purpose, allChannels);
        }
        for (var purpose : VERIFICATION_CODE_PURPOSES) {
            matrix.put(purpose, externalChannels);
        }
        return Map.copyOf(matrix);
    }
}
