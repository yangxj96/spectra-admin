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
import com.devops00.spectra.core.notification.javabean.from.NotificationHistoryQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationMessageVO;
import com.devops00.spectra.core.notification.service.NotificationHistoryService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 通知发送历史管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification/admin/messages")
public class NotificationHistoryAdminController {

    private final NotificationHistoryService service;

    /**
     * 分页查询发送历史。
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return 历史分页
     */
    @Audit("'查询通知发送历史'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public IPage<NotificationMessageVO> page(PageFrom page, NotificationHistoryQueryFrom params) {
        return service.page(page, params);
    }

    /**
     * 查询发送历史详情。
     *
     * @param id 历史 ID
     * @return 历史详情
     */
    @Audit("'查询通知发送历史详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:admin:read')")
    public NotificationMessageVO detail(@PathVariable UUID id) {
        return service.detail(id);
    }
}
