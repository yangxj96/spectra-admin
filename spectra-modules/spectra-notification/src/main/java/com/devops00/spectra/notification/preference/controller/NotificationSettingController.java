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

package com.devops00.spectra.notification.preference.controller;

import java.util.UUID;

import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.notification.preference.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.notification.preference.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.notification.preference.service.NotificationPreferenceService;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 旧消息中心设置 API 的兼容门面。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/setting")
public class NotificationSettingController {

    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    private final NotificationPreferenceService service;

    @ULog("'查询消息设置'")
    @GetMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:QUERY')")
    public NotificationSettingVO getSetting() {
        return service.legacy(SYSTEM_TENANT_ID, currentUserId());
    }

    @ULog("'更新消息设置'")
    @PutMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:UPDATE')")
    public void updateSetting(@RequestBody NotificationSettingFrom from) {
        service.saveLegacy(SYSTEM_TENANT_ID, currentUserId(), from);
    }

    private UUID currentUserId() {
        return SecUtil.getCurrentUserId();
    }
}
