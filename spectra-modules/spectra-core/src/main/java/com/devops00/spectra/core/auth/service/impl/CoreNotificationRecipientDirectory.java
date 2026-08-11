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

package com.devops00.spectra.core.auth.service.impl;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.core.auth.javabean.constant.AccountStatus;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Core 用户账号到通知收件人快照的适配器。 */
@Service
@RequiredArgsConstructor
public class CoreNotificationRecipientDirectory implements NotificationRecipientDirectory {

    private final AccountService accountService;

    @Override
    public List<NotificationRecipient> resolve(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .map(this::resolveOne)
                .toList();
    }

    @Override
    public List<NotificationRecipient> resolveByLoginNames(List<String> loginNames) {
        if (loginNames == null || loginNames.isEmpty()) {
            return List.of();
        }
        return loginNames.stream()
                .filter(this::hasText)
                .distinct()
                .map(accountService::getByLoginName)
                .filter(java.util.Objects::nonNull)
                .map(Account::getUserId)
                .filter(java.util.Objects::nonNull)
                .map(userId -> resolve(List.of(userId)).getFirst())
                .toList();
    }

    private NotificationRecipient resolveOne(UUID userId) {
        var accounts = accountService.listByUserId(userId);
        var active = accounts.stream().anyMatch(this::isActive);
        var verified = accounts.stream()
                .anyMatch(account -> isActive(account)
                    && Short.valueOf((short) 1).equals(account.getVerified()));
        var phone = accounts.stream()
                .filter(this::isUsable)
                .map(Account::getPhone)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
        var email = accounts.stream()
                .filter(this::isUsable)
                .map(Account::getEmail)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
        return new NotificationRecipient(userId, phone, email, active, verified);
    }

    private boolean isUsable(Account account) {
        return isActive(account) && Short.valueOf((short) 1).equals(account.getVerified());
    }

    private boolean isActive(Account account) {
        return account != null && AccountStatus.ACTIVE.getCode().equals(account.getStatus());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
