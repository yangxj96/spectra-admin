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
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import com.devops00.spectra.security.base.authorization.ScopeQuery;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Core 用户账号到通知收件人快照的适配器。
 */
@Service
@RequiredArgsConstructor
public class CoreNotificationRecipientDirectory implements NotificationRecipientDirectory {

    private final UserService userService;

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private final DepartmentService departmentService;

    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public List<NotificationRecipient> resolve(List<UUID> userIds) {
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
    public List<NotificationRecipient> resolveByLoginNames(List<String> loginNames) {
        if (loginNames == null || loginNames.isEmpty()) {
            return List.of();
        }
        return loginNames.stream()
                .filter(this::hasText)
                .distinct()
                .map(userService::getByEmail)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .flatMap(userId -> resolve(List.of(userId)).stream())
                .toList();
    }

    private NotificationRecipient resolveOne(UUID userId) {
        if (!allowedByCurrentUserScope(userId)) {
            return new NotificationRecipient(userId, null, null, false, false, null);
        }
        var user = userService.getById(userId);
        var active = user != null && UserStatus.ACTIVE.equals(user.getStatus());
        var phone = user == null ? null : user.getPhone();
        var email = user == null ? null : user.getEmail();
        var verified = active && (hasText(phone) || hasText(email));
        return new NotificationRecipient(userId, phone, email, active, verified, user == null ? null : user.getTimezone());
    }

    /**
     * 当前登录用户发起的通知必须遵守其有效数据范围；无登录上下文的定时任务由服务身份负责授权。
     */
    private boolean allowedByCurrentUserScope(UUID recipientUserId) {
        var currentUserId = securityContextAccessor.currentUserId();
        if (currentUserId == null) {
            return true;
        }
        AuthorizationSnapshot snapshot = authorizationSnapshotProvider.load(currentUserId);
        var boundaries = snapshot.accessBoundaries("user:read");
        if (!boundaries.isEmpty()
                && boundaries.stream().allMatch(boundary -> boundary.scope().mode() == ScopeMode.SELF)
                && !currentUserId.equals(recipientUserId)) {
            return false;
        }
        if (snapshot.canAccess("user:read", new ScopeQuery(currentUserId, recipientUserId, null, Set.of()))) {
            return true;
        }
        var recipient = userService.getById(recipientUserId);
        if (recipient == null || recipient.getDepartmentId() == null) {
            return false;
        }
        return snapshot.canAccess("user:read", new ScopeQuery(currentUserId, recipientUserId,
                recipient.getDepartmentId(), departmentLineage(recipient.getDepartmentId())));
    }

    private Set<UUID> departmentLineage(UUID departmentId) {
        var lineage = new LinkedHashSet<UUID>();
        var current = departmentId;
        while (current != null && lineage.add(current)) {
            var department = departmentService.getById(current);
            current = department == null ? null : department.getPid();
        }
        return lineage;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
