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
import com.devops00.spectra.common.notification.NotificationSystemActor;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Core 用户账号到通知收件人快照的适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class CoreNotificationRecipientDirectory implements NotificationRecipientDirectory {

    private final UserService userService;

    private final UserContactService userContactService;

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private final DepartmentService departmentService;

    private final SecurityContextAccessor securityContextAccessor;

    private final UserDepartmentMembershipMapper membershipMapper;

    @Override
    public List<NotificationRecipient> resolve(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        requireCurrentUser();
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(userId -> resolveOne(userId, false))
                .toList();
    }

    @Override
    public List<NotificationRecipient> resolveAsSystem(List<UUID> userIds, NotificationSystemActor actor) {
        requireSystemActor(actor);
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(userId -> resolveOne(userId, true))
                .toList();
    }

    @Override
    public List<NotificationRecipient> resolveByLoginNames(List<String> loginNames) {
        if (loginNames == null || loginNames.isEmpty()) {
            return List.of();
        }
        requireCurrentUser();
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

    @Override
    public List<NotificationRecipient> resolveByLoginNamesAsSystem(List<String> loginNames, NotificationSystemActor actor) {
        requireSystemActor(actor);
        if (!actor.permitsLoginNameLookup()) {
            throw new AccessDeniedException("该系统身份不能按登录名查询通知收件人");
        }
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
                .flatMap(userId -> resolveAsSystem(List.of(userId), actor).stream())
                .toList();
    }

    /**
     * 转换、解析或规范化数据（{@code resolveOne}）。
     */
    private NotificationRecipient resolveOne(UUID userId, boolean systemActor) {
        if (!systemActor && !allowedByCurrentUserScope(userId)) {
            return new NotificationRecipient(userId, null, null, false, false, null);
        }
        var user = userService.getById(userId);
        var active = user != null && UserStatus.ACTIVE.equals(user.getStatus());
        var contacts = user == null ? List.<UserContact>of() : userContactService.listActiveByUserId(userId);
        var phone = contacts.stream()
                .filter(contact -> UserContactService.PHONE.equals(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);
        var email = contacts.stream()
                .filter(contact -> UserContactService.EMAIL.equals(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);
        var verified = active && (StringUtils.hasText(phone) || StringUtils.hasText(email));
        return new NotificationRecipient(userId, phone, email, active, verified, user == null ? null : user.getTimezone());
    }

    /** 当前登录用户发起的通知必须遵守其有效数据范围。 */
    private boolean allowedByCurrentUserScope(UUID recipientUserId) {
        var currentUserId = requireCurrentUser();
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
        if (recipient == null) {
            return false;
        }
        return membershipMapper.selectDepartmentIdsForAuthorization(recipientUserId)
                .stream()
                .anyMatch(departmentId -> snapshot.canAccess("user:read", new ScopeQuery(currentUserId, recipientUserId,
                        departmentId, departmentLineage(departmentId))));
    }

    private UUID requireCurrentUser() {
        var currentUserId = securityContextAccessor.currentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("通知收件人查询缺少认证身份");
        }
        return currentUserId;
    }

    private void requireSystemActor(NotificationSystemActor actor) {
        if (actor == null) {
            throw new AccessDeniedException("通知收件人查询缺少系统运行身份");
        }
    }

    /**
     * 处理内部业务逻辑（{@code departmentLineage}）。
     */
    private Set<UUID> departmentLineage(UUID departmentId) {
        var lineage = new LinkedHashSet<UUID>();
        var current = departmentId;
        while (current != null && lineage.add(current)) {
            var department = departmentService.getById(current);
            current = department == null ? null : department.getPid();
        }
        return lineage;
    }

}
