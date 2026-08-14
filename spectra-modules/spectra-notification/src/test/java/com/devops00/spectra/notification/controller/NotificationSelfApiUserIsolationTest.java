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

package com.devops00.spectra.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationBatchDeleteFrom;
import com.devops00.spectra.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.service.NotificationInboxService;
import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Self API 的真实用户 A/B 上下文隔离回归测试。
 *
 * <p>测试直接切换生产使用的 {@link SecUtil} 持有者，覆盖 Controller 到 Service 的完整用户绑定边界；
 * 数据库层的收件人谓词由 {@code NotificationInboxServiceImplTest} 和 PostgreSQL 集成测试继续验证。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/13
 */
class NotificationSelfApiUserIsolationTest {

    private static final UUID USER_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    private static final UUID USER_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private static final UUID MESSAGE_ID = UUID.fromString("00000000-0000-0000-0000-00000000000c");

    private SecHolderStrategy security;

    @BeforeEach
    void setUpSecurityHolder() {
        security = mock(SecHolderStrategy.class);
        SecUtil.setHolder(security);
    }

    @AfterEach
    void clearSecurityHolder() {
        SecUtil.setHolder(null);
    }

    @Test
    void shouldBindEverySelfOperationToTheActiveUser() {
        var service = mock(NotificationInboxService.class);
        var controller = new NotificationController(service);
        when(security.getCurrentUserId()).thenReturn(USER_A);
        var page = new Page<com.devops00.spectra.notification.javabean.vo.NotificationInboxVO>();
        var params = new NotificationQueryFrom();
        var batch = new NotificationBatchDeleteFrom();
        batch.setIds(List.of(MESSAGE_ID));
        doNothing().when(service).markAsRead(MESSAGE_ID, USER_A);
        doNothing().when(service).markAllAsRead(USER_A);
        doNothing().when(service).deleteById(MESSAGE_ID, USER_A);
        doNothing().when(service).batchDelete(List.of(MESSAGE_ID), USER_A);

        controller.list(new PageFrom(), params);
        controller.unreadCount();
        controller.detail(MESSAGE_ID);
        controller.markAsRead(MESSAGE_ID);
        controller.markAllAsRead();
        controller.deleteById(MESSAGE_ID);
        controller.batchDelete(batch);

        verify(service).page(org.mockito.ArgumentMatchers.any(PageFrom.class), eq(USER_A), eq(params));
        verify(service).unreadCount(USER_A);
        verify(service).detail(MESSAGE_ID, USER_A);
        verify(service).markAsRead(MESSAGE_ID, USER_A);
        verify(service).markAllAsRead(USER_A);
        verify(service).deleteById(MESSAGE_ID, USER_A);
        verify(service).batchDelete(List.of(MESSAGE_ID), USER_A);
    }

    @Test
    void shouldSwitchIsolationWhenTheAuthenticatedUserChanges() {
        var service = mock(NotificationInboxService.class);
        var controller = new NotificationController(service);
        var page = new Page<com.devops00.spectra.notification.javabean.vo.NotificationInboxVO>();
        when(service.page(org.mockito.ArgumentMatchers.any(PageFrom.class), eq(USER_A),
                org.mockito.ArgumentMatchers.any(NotificationQueryFrom.class))).thenReturn(page);
        when(service.page(org.mockito.ArgumentMatchers.any(PageFrom.class), eq(USER_B),
                org.mockito.ArgumentMatchers.any(NotificationQueryFrom.class))).thenReturn(page);

        when(security.getCurrentUserId()).thenReturn(USER_A);
        controller.list(new PageFrom(), new NotificationQueryFrom());
        when(security.getCurrentUserId()).thenReturn(USER_B);
        controller.list(new PageFrom(), new NotificationQueryFrom());
        controller.detail(MESSAGE_ID);

        verify(service).page(org.mockito.ArgumentMatchers.any(PageFrom.class), eq(USER_A),
                org.mockito.ArgumentMatchers.any(NotificationQueryFrom.class));
        verify(service).page(org.mockito.ArgumentMatchers.any(PageFrom.class), eq(USER_B),
                org.mockito.ArgumentMatchers.any(NotificationQueryFrom.class));
        verify(service).detail(MESSAGE_ID, USER_B);
    }

    @Test
    void shouldRejectSelfApiWhenNoUserIsAuthenticated() {
        var service = mock(NotificationInboxService.class);
        var controller = new NotificationController(service);
        when(security.getCurrentUserId()).thenReturn(null);

        assertThrows(RuntimeException.class, controller::unreadCount);
    }
}
