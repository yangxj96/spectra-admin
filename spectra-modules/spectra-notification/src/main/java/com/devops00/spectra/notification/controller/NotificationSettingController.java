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

import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.notification.service.NotificationPreferenceService;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 旧消息中心设置 API 的兼容门面。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/setting")
public class NotificationSettingController {

    /**
     * 独立通知模块使用的系统租户。
     */
    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    /**
     * 用户通知偏好服务。
     */
    private final NotificationPreferenceService service;

    /**
     * 查询旧消息中心设置。
     */
    @ULog("'查询消息设置'")
    @GetMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:QUERY')")
    public NotificationSettingVO getSetting() {
        return service.legacy(SYSTEM_TENANT_ID, currentUserId());
    }

    /**
     * 更新旧消息中心设置。
     */
    @ULog("'更新消息设置'")
    @PutMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:UPDATE')")
    public void updateSetting(@RequestBody NotificationSettingFrom from) {
        service.saveLegacy(SYSTEM_TENANT_ID, currentUserId(), from);
    }

    /**
     * 获取当前登录用户 ID。
     */
    private UUID currentUserId() {
        return SecUtil.getCurrentUserId();
    }
}
