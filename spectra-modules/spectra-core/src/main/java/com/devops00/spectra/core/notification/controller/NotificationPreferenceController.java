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

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.core.notification.service.NotificationPreferenceService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 当前用户通知偏好接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification-center/preferences")
public class NotificationPreferenceController {

    /**
     * 用户通知偏好服务。
     */
    private final NotificationPreferenceService service;

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 查询当前用户用途×渠道偏好。
     */
    @Audit("'查询通知偏好'")
    @GetMapping(version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationUserPreferenceEntity> list() {
        return service.list(currentUserId());
    }

    /**
     * 保存当前用户可选通知偏好。
     */
    @Audit("'更新通知偏好'")
    @PutMapping(version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public void save(@RequestParam String purpose, @RequestParam String channel, @RequestParam boolean enabled,
                     @RequestParam(defaultValue = "false") boolean doNotDisturb) {
        service.save(currentUserId(), purpose, channel, enabled, doNotDisturb);
    }

    /**
     * 获取当前登录用户 ID。
     */
    private UUID currentUserId() {
        var userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前用户未登录");
        }
        return userId;
    }
}
