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

package com.devops00.spectra.core.notification.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.mapper.NotificationSendPreviewMapper;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * 受控发送 Service 的操作权限必须使用调用时的有效授权快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
@ExtendWith(MockitoExtension.class)
class NotificationControlledSendAuthorizationTest {

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private AuthorizationSnapshotProvider authorizationSnapshotProvider;

    @Mock
    private NotificationSendPreviewMapper previewMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private NotificationControlledSendServiceImpl service;

    @Test
    void missingIdentityIsRejectedBeforeSnapshotOrPreviewRead() {
        assertThrows(AccessDeniedException.class, () -> service.preview(null));
        assertThrows(AccessDeniedException.class, () -> service.apply(applyFrom()));
        verifyNoInteractions(authorizationSnapshotProvider, previewMapper, userMapper);
    }

    @Test
    void permissionlessOperatorCannotPreviewOrApply() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(authorizationSnapshotProvider.load(operatorId)).thenReturn(AuthorizationSnapshot.of(List.of()));
        when(userMapper.selectActiveByIdsForUpdate(List.of(operatorId))).thenReturn(List.of(mock(User.class)));

        assertThrows(AccessDeniedException.class, () -> service.preview(null));
        assertThrows(AccessDeniedException.class, () -> service.apply(applyFrom()));
        verify(authorizationSnapshotProvider, times(2)).load(operatorId);
        verifyNoInteractions(previewMapper);
    }

    @Test
    void applyLoadsCurrentPermissionAgainAfterRoleContraction() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(authorizationSnapshotProvider.load(operatorId)).thenReturn(rootSnapshot(), AuthorizationSnapshot.of(List.of()));
        when(userMapper.selectActiveByIdsForUpdate(List.of(operatorId))).thenReturn(List.of(mock(User.class)));
        var from = applyFrom();

        assertThrows(DataNotExistException.class, () -> service.apply(from),
                "有效 Root 必须通过权限边界并进入 Preview 所有者检查");
        assertThrows(AccessDeniedException.class, () -> service.apply(from),
                "角色收缩后不得读取 Preview 或重放既有 Apply 结果");
        verify(previewMapper, times(1)).selectById(from.getPreviewId());
        verify(authorizationSnapshotProvider, times(2)).load(operatorId);
    }

    @Test
    void previewPermissionIsCheckedSeparatelyAndUnknownStateFailsClosed() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(authorizationSnapshotProvider.load(operatorId)).thenReturn(rootSnapshot(), null);

        assertThrows(DataException.class, () -> service.preview(null),
                "有效 Root 必须先通过权限检查，再按业务参数拒绝");
        assertThrows(AccessDeniedException.class, () -> service.preview(null),
                "无法确认授权快照时不得进入业务读取");
        verify(authorizationSnapshotProvider, times(2)).load(operatorId);
        verifyNoInteractions(previewMapper);
    }

    @Test
    void snapshotReadFailureRejectsBeforePreviewLookup() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userMapper.selectActiveByIdsForUpdate(List.of(operatorId))).thenReturn(List.of(mock(User.class)));
        when(authorizationSnapshotProvider.load(operatorId)).thenThrow(new IllegalStateException("authorization unavailable"));

        assertThrows(IllegalStateException.class, () -> service.apply(applyFrom()));
        verifyNoInteractions(previewMapper);
    }

    @Test
    void missingOrUnavailableOperatorRowRejectsBeforeAuthorizationRead() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userMapper.selectActiveByIdsForUpdate(List.of(operatorId))).thenReturn(List.of());

        assertThrows(AccessDeniedException.class, () -> service.apply(applyFrom()));
        verifyNoInteractions(authorizationSnapshotProvider, previewMapper);
    }

    @Test
    void operatorLockFailureRejectsBeforeAuthorizationRead() {
        UUID operatorId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userMapper.selectActiveByIdsForUpdate(List.of(operatorId)))
                .thenThrow(new IllegalStateException("operator row unavailable"));

        assertThrows(IllegalStateException.class, () -> service.apply(applyFrom()));
        verifyNoInteractions(authorizationSnapshotProvider, previewMapper);
    }

    private static AuthorizationSnapshot rootSnapshot() {
        return AuthorizationSnapshot.of(List.of(new AuthorizationAssignment(UUID.randomUUID(),
                RootAuthorizationPolicy.ROOT_ROLE, 1_000, Map.of(), Map.of())));
    }

    private static NotificationControlledSendApplyFrom applyFrom() {
        var from = new NotificationControlledSendApplyFrom();
        from.setPreviewId(UUID.randomUUID());
        return from;
    }
}
