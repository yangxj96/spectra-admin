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

package com.devops00.spectra.notification.inbox.controller;

import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.notification.inbox.javabean.from.NotificationBatchDeleteFrom;
import com.devops00.spectra.notification.inbox.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.inbox.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.notification.inbox.service.NotificationInboxService;
import com.devops00.spectra.security.base.holder.SecUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前用户消息中心接口；所有查询和写操作均由服务层强制绑定当前收件人。 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/notification", "/notification-center/inbox"})
public class NotificationController {

    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    private final NotificationInboxService service;

    /** 查询当前用户消息列表。 */
    @ULog("'查询消息列表'")
    @GetMapping(value = "/list", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:QUERY')")
    public IPage<NotificationInboxVO> list(PageFrom page, NotificationQueryFrom params) {
        return service.page(page, SYSTEM_TENANT_ID, currentUserId(), params);
    }

    /** 查询当前用户消息详情。 */
    @ULog("'查询消息详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:QUERY')")
    public NotificationInboxVO detail(@PathVariable UUID id) {
        return service.detail(id, SYSTEM_TENANT_ID, currentUserId());
    }

    /** 查询当前用户未读数量。 */
    @ULog("'获取未读消息数'")
    @GetMapping(value = "/unread-count", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:QUERY')")
    public long unreadCount() {
        return service.unreadCount(SYSTEM_TENANT_ID, currentUserId());
    }

    /** 标记单条消息已读。 */
    @ULog("'标记消息已读'")
    @PutMapping(value = "/{id}/read", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:UPDATE')")
    public void markAsRead(@PathVariable UUID id) {
        service.markAsRead(id, SYSTEM_TENANT_ID, currentUserId());
    }

    /** 标记当前用户全部消息已读。 */
    @ULog("'全部标记已读'")
    @PutMapping(value = "/read-all", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:UPDATE')")
    public void markAllAsRead() {
        service.markAllAsRead(SYSTEM_TENANT_ID, currentUserId());
    }

    /** 删除当前用户的一条消息。 */
    @ULog("'删除消息'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:DELETE')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id, SYSTEM_TENANT_ID, currentUserId());
    }

    /** 仅删除当前用户拥有的消息，混合用户 ID 不会影响其他收件人。 */
    @ULog("'批量删除消息'")
    @PostMapping(value = "/batch-delete", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION:DELETE')")
    public void batchDelete(@Valid @RequestBody NotificationBatchDeleteFrom from) {
        service.batchDelete(from.getIds(), SYSTEM_TENANT_ID, currentUserId());
    }

    private UUID currentUserId() {
        var userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            throw new DataNotExistException("当前用户不存在");
        }
        return userId;
    }
}
