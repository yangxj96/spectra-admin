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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Core 用户账号到通知收件人快照的适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class CoreNotificationRecipientDirectory implements NotificationRecipientDirectory {

    private final UserService userService;

    private final UserContactService userContactService;

    @Override
    public List<NotificationRecipient> resolve(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(this::resolveOne)
                .toList();
    }

    @Override
    public List<NotificationRecipient> resolveByLoginNames(Collection<String> loginNames) {
        if (loginNames == null || loginNames.isEmpty()) {
            return List.of();
        }
        return loginNames.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .map(userService::getByUsername)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .flatMap(userId -> resolve(List.of(userId)).stream())
                .toList();
    }

    /**
     * 解析单个用户的通知收件人快照。
     *
     * @param userId 用户 ID
     * @return 收件人快照
     */
    private NotificationRecipient resolveOne(UUID userId) {
        var user = userService.getById(userId);
        var active = user != null && UserStatus.ACTIVE.equals(user.getStatus());
        var contacts = user == null ? List.<UserContact>of() : userContactService.listActiveByUserId(userId);
        var phone = contactValue(contacts, UserContactService.PHONE);
        var email = contactValue(contacts, UserContactService.EMAIL);
        var verified = active && (StringUtils.hasText(phone) || StringUtils.hasText(email));
        return new NotificationRecipient(userId, phone, email, active, verified, user == null ? null : user.getTimezone());
    }

    /**
     * 读取指定类型的联系人。
     *
     * @param contacts    联系人集合
     * @param contactType 联系人类型
     * @return 联系人值；不存在时返回 {@code null}
     */
    private String contactValue(List<UserContact> contacts, String contactType) {
        return contacts.stream()
                .filter(contact -> contactType.equals(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);
    }
}
