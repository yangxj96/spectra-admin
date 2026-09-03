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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.notification.javabean.from.NotificationBatchDeleteFrom;
import com.devops00.spectra.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.notification.service.NotificationInboxService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
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

import java.util.UUID;

/**
 * 当前用户消息中心接口；所有查询和写操作均由服务层强制绑定当前收件人。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification")
public class NotificationController {

    /**
     * 当前用户消息中心服务。
     */
    private final NotificationInboxService service;

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 查询当前用户消息列表。
     */
    @Audit("'查询消息列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public IPage<NotificationInboxVO> list(PageFrom page, NotificationQueryFrom params) {
        return service.page(page, currentUserId(), params);
    }

    /**
     * 查询当前用户消息详情。
     */
    @Audit("'查询消息详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public NotificationInboxVO detail(@PathVariable UUID id) {
        return service.detail(id, currentUserId());
    }

    /**
     * 查询当前用户未读数量。
     */
    @Audit("'获取未读消息数'")
    @GetMapping(value = "/unread-count", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public long unreadCount() {
        return service.unreadCount(currentUserId());
    }

    /**
     * 标记单条消息已读。
     */
    @Audit("'标记消息已读'")
    @PutMapping(value = "/{id}/read", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:update')")
    public void markAsRead(@PathVariable UUID id) {
        service.markAsRead(id, currentUserId());
    }

    /**
     * 标记当前用户全部消息已读。
     */
    @Audit("'全部标记已读'")
    @PutMapping(value = "/read-all", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:update')")
    public void markAllAsRead() {
        service.markAllAsRead(currentUserId());
    }

    /**
     * 删除当前用户的一条消息。
     */
    @Audit("'删除消息'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:delete')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id, currentUserId());
    }

    /**
     * 仅删除当前用户拥有的消息，混合用户 ID 不会影响其他收件人。
     */
    @Audit("'批量删除消息'")
    @PostMapping(value = "/batch-delete", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:delete')")
    public void batchDelete(@Valid @RequestBody NotificationBatchDeleteFrom from) {
        service.batchDelete(from.getIds(), currentUserId());
    }

    /**
     * 获取当前登录用户 ID，拒绝匿名访问消息数据。
     */
    private UUID currentUserId() {
        var userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new DataNotExistException("当前用户不存在");
        }
        return userId;
    }
}
