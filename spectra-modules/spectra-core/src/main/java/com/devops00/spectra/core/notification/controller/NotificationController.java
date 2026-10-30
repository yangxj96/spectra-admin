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

package com.devops00.spectra.core.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.notification.javabean.from.NotificationBatchDeleteFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.core.notification.service.NotificationInboxService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
 * 当前用户消息中心接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationInboxService service;

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 查询当前用户消息列表。
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return 消息分页
     */
    @Audit("'查询消息列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public IPage<NotificationInboxVO> list(PageFrom page, NotificationQueryFrom params) {
        return service.page(page, currentUserId(), params);
    }

    /**
     * 查询当前用户消息详情。
     *
     * @param id 消息 ID
     * @return 消息详情
     */
    @Audit("'查询消息详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public NotificationInboxVO detail(@PathVariable UUID id) {
        return service.detail(id, currentUserId());
    }

    /**
     * 查询当前用户未读数量。
     *
     * @return 未读数量
     */
    @Audit("'获取未读消息数'")
    @GetMapping(value = "/unread-count", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:read')")
    public long unreadCount() {
        return service.unreadCount(currentUserId());
    }

    /**
     * 标记单条消息已读。
     *
     * @param id 消息 ID
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
     *
     * @param id 消息 ID
     */
    @Audit("'删除消息'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:delete')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id, currentUserId());
    }

    /**
     * 批量删除当前用户消息。
     *
     * @param from 批量删除参数
     */
    @Audit("'批量删除消息'")
    @PostMapping(value = "/batch-delete", version = "1.0.0")
    @PreAuthorize("hasPermission(null ,'notification:delete')")
    public void batchDelete(@Validated @RequestBody NotificationBatchDeleteFrom from) {
        service.batchDelete(from.getIds(), currentUserId());
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前用户 ID
     */
    private UUID currentUserId() {
        var userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new DataNotExistException("当前用户不存在");
        }
        return userId;
    }
}
